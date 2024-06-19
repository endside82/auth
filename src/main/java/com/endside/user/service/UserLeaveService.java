package com.endside.user.service;

import com.endside.config.db.repository.JwtTokenIssueRecordRepository;
import com.endside.config.db.repository.RefreshTokenRepository;
import com.endside.config.error.exception.RestException;
import com.endside.user.constants.UserStatus;
import com.endside.user.model.Users;
import com.endside.config.db.redis.RedisUserRepositoryImpl;
import com.endside.user.repository.*;
import com.endside.event.service.UserEventService;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.BlackStatus;
import com.endside.config.error.exception.LeaveFailureException;
import com.endside.user.model.DropOutUser;
import com.endside.user.param.UserLeaveParam;
import com.endside.config.error.ErrorCode;
import com.endside.config.security.constants.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserLeaveService {
    private final UserRepository userRepository;
    private final RedisUserRepositoryImpl redisUserRepository;
    private final JwtTokenIssueRecordRepository jwtTokenIssueRecordRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DropOutUserRepository dropOutUserRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final DeviceRepository deviceRepository;
    private final UserEventService userEventService;
    private final SocialLoginRepository socialLoginRepository;
    private final IdentityVerificationRepository identityVerificationRepository;
    private final AgreementRepository agreementRepository;

    @Transactional(rollbackFor = {Exception.class})
    public void leaveUser(UserLeaveParam userLeaveParam) {
        Users users = userRepository.findByUserId(userLeaveParam.getUserId()).orElseThrow(
                () -> new RestException(ErrorCode.NOT_EXIST_USER)
        );
        long userId = users.getUserId();
        // 정상유저일때에만 탈퇴를 진행한다.
        if (users.getStatus() != UserStatus.NORMAL) {
            throw new LeaveFailureException(ErrorCode.USER_ALREADY_LEAVE_STATUS);
        }

        // 이메일 로그인 타입이 아닐 경우에 패스워드가 맞는 지 확인
        if (userLeaveParam.getLoginType() == LoginType.EMAIL) {
            if (!bCryptPasswordEncoder.matches(userLeaveParam.getPassword(), users.getPassword())) {
                throw new LeaveFailureException(ErrorCode.PASSWORD_MISMATCH);
            }
        // 소셜 로그인 타입일 경우 소셜 로그인 정보 삭제
        } else if (userLeaveParam.getLoginType() == LoginType.SOCIAL) {
            socialLoginRepository.deleteByUserId(userId);
        }
        identityVerificationRepository.deleteByUserId(userId);
        deviceRepository.deleteByUserId(userId);
        agreementRepository.deleteByUserId(userId);
        refreshTokenRepository.deleteAllByUserId(userId);

        // 사용자 탈퇴 히스토리 저장
        saveUserHistory(userId, users.getEmail(), users.getMobile());
        // 사용자 정보 초기화
        deleteUserData(userId);
        // 현재 최대 auth token 유효기간
        Date expireDate = new Date(System.currentTimeMillis() + JwtProperties.EXPIRATION_TIME_3DAY_MILLI);
        redisUserRepository.setBlackListUserWithExpire(userId, BlackStatus.EXIT.getStatus(), expireDate);
        //탈퇴자 jwtToken history 삭제
        jwtTokenIssueRecordRepository.deleteAllByUserId(userId);
        String extData = userEventService.getSimpleExtData(userId);
    }

    // userId로 찾아서 로그아웃
    public void logoutUser(long userId, String authToken) {
        // auth token 에서 부모 토큰을 삭제하고 refresh token을 반환한다.
        long refreshTokenId = jwtAuthenticationService.getRefreshTokenId(authToken);
        refreshTokenRepository.deleteById(refreshTokenId);
        // 유저 정보 찾아서 로그아웃 등록
        userRepository.findByUserId(userId).ifPresent(preUser -> {
            preUser.setStatus(UserStatus.LOGOUT);
            preUser.setLogoutAt(LocalDateTime.now());
            userRepository.save(preUser);
        });
        // auth token add blacklist
        jwtAuthenticationService.addBlackListByUserIdAndRefreshTokenId( BlackStatus.LOGOUT, refreshTokenId);
    }

    private void saveUserHistory(long userId, String email, String mobile) {
        byte[] targetBytes = mobile.getBytes(StandardCharsets.UTF_8);
        byte[] encodedBytes = Base64.getEncoder().encode(targetBytes);
        dropOutUserRepository
                .save(DropOutUser.builder()
                .userId(userId)
                .email(email)
                .mobile(encodedBytes)
                .dropAt(LocalDateTime.now()).build());
    }

    private void deleteUserData(long userId) {
        userRepository.findByUserId(userId).ifPresent(leaveUser -> {
            leaveUser.setStatus(UserStatus.EXIT);
            leaveUser.setEmail("");
            leaveUser.setMobile("");
            leaveUser.setPassword("");
            leaveUser.setLoginAt(null);
            leaveUser.setLogoutAt(null);
            leaveUser.setUpdatedAt(LocalDateTime.now());
            userRepository.save(leaveUser);
        });
    }

}



