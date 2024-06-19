package com.endside.user.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.endside.config.db.repository.JwtTokenIssueRecordQueryRepository;
import com.endside.config.error.exception.RestException;
import com.endside.user.constants.BlackStatus;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.Os;
import com.endside.user.model.JwtRecord;
import com.endside.user.model.MemberInfo;
import com.endside.user.model.RefreshToken;
import com.endside.config.db.repository.JwtTokenIssueRecordRepository;
import com.endside.config.db.repository.RefreshTokenRepository;
import com.endside.config.db.redis.RedisUserRepositoryImpl;
import com.endside.config.error.exception.InvalidAuthTokenException;
import com.endside.config.security.constants.JwtProperties;
import com.endside.user.repository.UserRepository;
import com.endside.config.error.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.xml.bind.DatatypeConverter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

// 로그인
@Slf4j
@Service
public class JwtAuthenticationService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenIssueRecordRepository jwtTokenIssueRecordRepository;
    private final JwtTokenIssueRecordQueryRepository jwtTokenIssueRecordQueryRepository;
    private final RedisUserRepositoryImpl redisUserRepository;
    private final JWTVerifier jwtVerifier;
    private static final Long DAY = 3600L * 24;
    private final String secret;

    public JwtAuthenticationService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository,
                                    JwtTokenIssueRecordRepository jwtTokenIssueRecordRepository, JwtTokenIssueRecordQueryRepository jwtTokenIssueRecordQueryRepository, RedisUserRepositoryImpl redisUserRepository,
                                    @Value("${jwt.secret}") String secret) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtTokenIssueRecordRepository = jwtTokenIssueRecordRepository;
        this.jwtTokenIssueRecordQueryRepository = jwtTokenIssueRecordQueryRepository;
        this.redisUserRepository = redisUserRepository;
        this.secret = secret;
        jwtVerifier = JWT.require(HMAC512(this.secret.getBytes()))
                .acceptExpiresAt(DAY * 365)
                .build();
    }

    // create jwt token
    public String createJwtToken(String email, long userId, String ip, long refreshTokenId, LoginType loginType) {
        String subject = email + JwtProperties.SPLITTER + userId;
        String converted = DatatypeConverter.printHexBinary(subject.getBytes());
        LocalDateTime expireDate = LocalDateTime.now().plusSeconds(JwtProperties.EXPIRATION_TIME_3DAY_SECOND);
        JwtRecord newJwtRecord = addJwtTokenHistory(userId, expireDate, ip, refreshTokenId);
        jwtTokenIssueRecordRepository.flush();
        // Create JWT Token
        return JWT.create()
                .withSubject(converted)
                .withJWTId(String.valueOf(newJwtRecord.getIssueNo()))
                .withClaim(JwtProperties.REFRESH_TOKEN_Id_KEY, refreshTokenId)
                .withClaim(JwtProperties.CLAIM_LOGIN_TYPE, loginType.toString())
                .withAudience(JwtProperties.AUDIENCE)
                .withExpiresAt(Timestamp.valueOf(expireDate))
                .sign(HMAC512(secret.getBytes()));
    }

    // jwt token history
    private JwtRecord addJwtTokenHistory(long userId, LocalDateTime expireDate, String ip, long refreshTokenId) {
        return jwtTokenIssueRecordRepository.save(JwtRecord.builder()
                .userId(userId)
                .refreshTokenId(refreshTokenId)
                .ipAddress(ip)
                .expireDatetime(expireDate)
                .build());
    }

    // refresh token 발급
    public Map<String, String> refreshToken(String token, String refreshToken, String ip) {
        HashMap<String, String> resultMap = new HashMap<>();
        // parse the token and validate it
        DecodedJWT decodedJWT = verify(token);
        String converted = decodedJWT.getSubject();

        if (converted == null) {
            throw new InvalidAuthTokenException(ErrorCode.TOKEN_REFRESH_INVALID_AUTH_TOKEN);
        }
        if (refreshToken == null) {
            throw new InvalidAuthTokenException(ErrorCode.TOKEN_REFRESH_FAIL_INVALID_REFRESH_TOKEN);
        }

        String subject = new String(DatatypeConverter.parseHexBinary(converted));
        String[] subArray = subject.split(JwtProperties.SPLITTER);
        if (subArray[0] == null) {
            log.error("failed refresh token : INVALID_AUTH_TOKEN : " + token);
            throw new InvalidAuthTokenException(ErrorCode.TOKEN_REFRESH_INVALID_AUTH_TOKEN);
        }
        long userId = Long.parseLong(subArray[1]);
        if (userId < 1L) {
            throw new InvalidAuthTokenException(ErrorCode.TOKEN_REFRESH_FAIL_NO_USER);
        }

        // 가장 최근에 발급된 refresh token을 가져온다.
        RefreshToken retrievedRefreshToken = refreshTokenRepository.findByUserIdAndRefreshToken(userId, refreshToken)
                .orElseThrow(() -> {
                    log.error("failed refresh token : TOKEN_REFRESH_FAIL_INVALID_REFRESH_TOKEN : " + userId);
                    return new InvalidAuthTokenException(ErrorCode.TOKEN_REFRESH_FAIL_INVALID_REFRESH_TOKEN);
                });
        Os os = retrievedRefreshToken.getOs();
        long refreshTokenId = retrievedRefreshToken.getId();
        LocalDateTime expireDate = retrievedRefreshToken.getExpireDatetime();
        LocalDateTime currentDate = LocalDateTime.now();
        // refresh token 만료 검사
        if (expireDate.isBefore(currentDate)) {
            log.warn("failed refresh token : TOKEN_REFRESH_FAIL_TOKEN_EXPIRED : " + userId);
            throw new InvalidAuthTokenException(ErrorCode.TOKEN_REFRESH_FAIL_TOKEN_EXPIRED);
        }

        // 토큰 만료 기간이 3일 정도 남았으면 refresh 토큰을 새로 발급 해준다.
        if (expireDate.isBefore(currentDate.plusDays(JwtProperties.REFRESH_TOKEN_NEED_REISSUE))) {
            log.warn("failed refresh token : RESULT_MAP_REFRESH : " + userId);
            RefreshToken newRefreshToken = issueRefreshToken(userId, os);
            refreshTokenId = newRefreshToken.getId();
            resultMap.put(JwtProperties.RESULT_MAP_REFRESH, newRefreshToken.getRefreshToken());
            // delete old refresh token
            refreshTokenRepository.delete(retrievedRefreshToken);
        }
        String email = userRepository.findByUserId(userId).orElseThrow(
                () -> new RestException(ErrorCode.NOT_EXIST_USER)
        ).getEmail();
        LoginType loginType = LoginType.EMAIL;

        /* 로그인이 소셜 타입인 경우
        if (ssoUserRepository.findByUserId(userId) != null) {
            loginType = LoginType.SSO;
        }
        */
        String authToken = createJwtToken(email, userId, ip, refreshTokenId, loginType);
        resultMap.put(JwtProperties.RESULT_MAP_AUTH, authToken);
        return resultMap;
    }

    private DecodedJWT verify(String token) {
        DecodedJWT decodedJWT;
        try {
            decodedJWT = jwtVerifier.verify(token); // verification
        } catch (JWTVerificationException e) {
            // refresh를 진행할 토큰이기 때문에 invalid 여부만 검사한다. (만료 기간은 보지 않음)
            log.error("failed verification auth token : " + token);
            throw new InvalidAuthTokenException(ErrorCode.TOKEN_REFRESH_INVALID_AUTH_TOKEN);
        }
        return decodedJWT;
    }

    public long getRefreshTokenId(String token) {
        DecodedJWT decodedJWT = verify(token);
        Claim claim = decodedJWT.getClaim(JwtProperties.REFRESH_TOKEN_Id_KEY);
        return claim.asLong();
    }


    public HashMap<String, String> createTokensAfterJoin(String email, long userId, String ip, LoginType loginType, Os os) {
        HashMap<String, String> tokens = new HashMap<>();
        RefreshToken refreshToken = issueRefreshToken(userId, os);
        String authToken = createJwtToken(email, userId, ip, refreshToken.getId(), loginType);
        tokens.put(JwtProperties.RESULT_MAP_AUTH, authToken);
        tokens.put(JwtProperties.RESULT_MAP_REFRESH, refreshToken.getRefreshToken());
        return tokens;
    }

    public RefreshToken deletePreviousTokenAndIssueRefreshToken(long userId, Os os) {
        refreshTokenRepository.deleteAllByUserIdAndOs(userId, os);
        return issueRefreshToken(userId, os);
    }

    // issuing refresh token
    public RefreshToken issueRefreshToken(long userId, Os os) {
        // make uuid as refresh Token
        String refreshTokenString = UUID.randomUUID().toString();
        // refresh token expire date
        LocalDateTime expireDate = LocalDateTime.now().plusDays(JwtProperties.REFRESH_TOKEN_EXPIRATION_DATE);
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setOs(os);
        refreshToken.setRefreshToken(refreshTokenString);
        refreshToken.setExpireDatetime(expireDate);
        refreshToken.setCreatedAt(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    // 토큰 블랙 리스트
    public ErrorCode checkBlackListToken(String issueNo) {
        List<String> reasons = redisUserRepository.getBlackListToken(issueNo);
        if (reasons == null || reasons.isEmpty()) {
            return null;
        }
        return checkTokenBlacklistType(reasons);
    }

    // 블랙리스트 유저인지 확인
    public ErrorCode checkBlackListUser(long userId) {
        List<String> reasons = redisUserRepository.getBlackListUser(userId);
        if (reasons == null || reasons.isEmpty()) {
            return null;
        }
        return checkUserBlacklistType(reasons);
    }

    // 블랙리스트 상태값 조회 우선 순위 고려
    private ErrorCode checkTokenBlacklistType(List<String> reasons) {
        if (hasStringInList(reasons, BlackStatus.LOGOUT.getStatus())) {
            // 유저 블랙리스트 상태값 조회 우선 순위 고려
            return ErrorCode.LOGIN_FAILURE_USER_STATUS_LOGOUT;
        }
        return null;
    }

    private ErrorCode checkUserBlacklistType(List<String> reasons) {
        if (hasStringInList(reasons, BlackStatus.EXIT.getStatus())) {
            return ErrorCode.LOGIN_FAILURE_USER_STATUS_EXIT;
        }


        if (hasStringInList(reasons, BlackStatus.BAN.getStatus())) {
            return ErrorCode.LOGIN_FAILURE_USER_STATUS_BAN;
        }
        // note 상기 두개 케이스 외에 발생하는 케이스 없음 (정의 된것은 있으나 케이스 없음)
        return null;
    }

    //
    private boolean hasStringInList(List<String> stringList, String status) {
        return stringList.stream().anyMatch(status::equals);
    }

    /**
     * 유저의 유효한 auth token을 모두 취득해서 LOGOUT 시킨다.
     *
     * @param userId
     */
    public void addBlackListByUserId(long userId, BlackStatus status) {
        if (status == null) {
            return;
        }
        // get jwt token issue no list
        List<JwtRecord> jwtTokenHistories = jwtTokenIssueRecordQueryRepository.findAllValidTokensByUser(userId, LocalDateTime.now());
        jwtTokenHistories.forEach(jwt -> {
            redisUserRepository.setBlackListTokenWithExpire(String.valueOf(jwt.getIssueNo()), status.getStatus(), Timestamp.valueOf(jwt.getExpireDatetime()));
            jwt.setLogoutAt(LocalDateTime.now());
            jwtTokenIssueRecordRepository.save(jwt);
        });
    }

    /**
     * 유저의 유효한 auth token을 모두 취득해서 LOGOUT 시킨다.
     */
    public void addBlackListByUserIdAndRefreshTokenId(BlackStatus status, long refreshTokenId) {
        if (status == null) {
            return;
        }
        // get jwt token issue no list
        List<JwtRecord> jwtTokenHistories = jwtTokenIssueRecordQueryRepository.findAllValidTokenByRefreshToken(refreshTokenId, LocalDateTime.now());
        jwtTokenHistories.forEach(jwt -> {
            redisUserRepository.setBlackListTokenWithExpire(String.valueOf(jwt.getIssueNo()), status.getStatus(), Timestamp.valueOf(jwt.getExpireDatetime()));
            jwt.setLogoutAt(LocalDateTime.now());
            jwtTokenIssueRecordRepository.save(jwt);
        });
    }

    public MemberInfo getUserInfo(long userIndex) {
        // TODO Mapper
        MemberInfo userInfo = new MemberInfo();
        userRepository.findByUserId(userIndex).ifPresent(login_user -> BeanUtils.copyProperties(login_user, userInfo));
        return userInfo;
    }

    // 특정 OS에 속한 리프레쉬 토큰을 획득하고 해당 리프레쉬 토큰에 속한 토을 전부 토큰 블랙 리스트에 등록 시킨다
    public void addBlackListByUserIdAndOs(long userId, BlackStatus status, Os os) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findByUserIdAndOsAndExpireDatetimeGreaterThanEqual(userId, os, LocalDateTime.now());
        refreshTokens.forEach(refreshToken -> {
            addBlackListByUserIdAndRefreshTokenId(status, refreshToken.getId());
            refreshTokenRepository.delete(refreshToken);
        });
    }
}

