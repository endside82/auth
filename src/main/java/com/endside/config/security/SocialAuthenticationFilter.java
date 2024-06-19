package com.endside.config.security;

import com.endside.config.error.ErrorCode;
import com.endside.config.error.ResponseConstants;
import com.endside.config.error.exception.RestException;
import com.endside.config.security.constants.JwtProperties;
import com.endside.social.service.GoogleVerificationService;
import com.endside.social.service.SocialValidationService;
import com.endside.social.vo.SocialUserInfo;
import com.endside.social.vo.TossIdentificationVo;
import com.endside.user.constants.BlackStatus;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.Os;
import com.endside.user.constants.ProviderType;
import com.endside.user.model.LoginAddInfo;
import com.endside.user.model.RefreshToken;
import com.endside.user.model.UserSimple;
import com.endside.user.param.SocialLoginModel;
import com.endside.user.service.JwtAuthenticationService;
import com.endside.user.service.LoginSuccessAfterService;
import com.endside.user.service.SocialUserAuthenticationService;
import com.endside.user.vo.SocialLoginVo;
import com.endside.util.WebUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.endside.social.service.TossVerificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 소셜 로그인후 인증 토큰을 발급하는 필터
 * 회원 정보가 없으면 회원 가입을 위한 본인인증 값을 전달
 */
@Slf4j
public class SocialAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final SocialUserAuthenticationService socialUserAuthenticationService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final LoginSuccessAfterService loginSuccessAfterService;
    private final GoogleVerificationService googleVerificationService;
    private final SocialValidationService socialValidationService;
    private final TossVerificationService tossVerificationService;
    private final WebUtil webUtil;

    public SocialAuthenticationFilter(AuthenticationManager authenticationManager, SocialUserAuthenticationService socialUserAuthenticationService,
                                      JwtAuthenticationService jwtAuthenticationService, LoginSuccessAfterService loginSuccessAfterService,
                                      WebUtil webUtil, GoogleVerificationService googleVerificationService,
                                      SocialValidationService socialValidationService, TossVerificationService tossVerificationService) {
        this.socialUserAuthenticationService = socialUserAuthenticationService;
        this.authenticationManager = authenticationManager;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.loginSuccessAfterService = loginSuccessAfterService;
        this.googleVerificationService = googleVerificationService;
        this.socialValidationService = socialValidationService;
        this.webUtil = webUtil;
        this.tossVerificationService = tossVerificationService;
        this.setFilterProcessesUrl("/auth/login/social");
    }

    /**
     * 소셜 로그인
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 로그인 입력 정보
        SocialLoginModel credential;
        try {
            credential = new ObjectMapper().readValue(request.getInputStream(), SocialLoginModel.class);
        } catch (IOException e) {
            errorResponse(response, ErrorCode.SOCIAL_LOGIN_FAILURE_BAD_CREDENTIAL);
            return null;
        }
        ProviderType providerType = credential.getProviderType(); // KAKAO, NAVER, GOOGLE
        String idToken = credential.getIdToken();                 // code <= credential

        /* 소셜 프로바이더에서 검증된 정보 */
        SocialUserInfo socialUserInfo;
        try {
            // social ID token 유효성 확인
            socialUserInfo = getProviderInfo(providerType, idToken);
        } catch (Exception e) {
            errorResponse(response, ErrorCode.SOCIAL_LOGIN_FAILURE_BAD_CREDENTIAL);
            return null;
        }
        String socialUniqueId = socialUserInfo.getSocialUniqueId();
        String socialEmail = socialUserInfo.getEmail();

        /* 소셜 로그인 정보 확인 */
        SocialLoginVo socialLoginVo = socialUserAuthenticationService.getSocialLogin(socialUniqueId);
        // 가입된 회원이 아님
        if (socialLoginVo == null) {
            // 본인 인증 + 회원 가입 유도
            try {
                ObjectMapper mapper = new ObjectMapper();
                String result = mapper.writeValueAsString(askToSignUpResult(providerType, socialUniqueId, socialEmail));
                okResultResponse(response, result);
                return null;
            } catch (RestException e) {
                errorResponse(response, e.getErrorCode());
                return null;
            } catch (JsonProcessingException e) {
                // nothing to do
                log.error("ok response fail");
                return null;
            }
        }

        // return authentication (user principal)
        Authentication authentication;
        try {
            authentication = getAuthentication(socialUniqueId, providerType,credential.getOs() );
        } catch (RestException e) {
            errorResponse(response, e.getErrorCode());
            return null;
        }
        return authentication;
    }

    private SocialUserInfo getProviderInfo(ProviderType providerType, String idToken) {
        return switch (providerType) {
            case NAVER -> null;
            case KAKAO -> null;
            case GOOGLE -> googleVerificationService.getProviderInfo(idToken);
        };
    }

    private Authentication getAuthentication(String subject, ProviderType providerType, Os os) {
        LoginAddInfo loginAddInfo = new LoginAddInfo();
        loginAddInfo.setLoginType(LoginType.SOCIAL);
        loginAddInfo.setProviderType(providerType);
        loginAddInfo.setOs(os);
        CustomAuthenticationToken authenticationToken = new CustomAuthenticationToken(subject, null, loginAddInfo); // authority
        return authenticationManager.authenticate(authenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chin, Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        LoginAddInfo loginAddInfo = principal.getLoginAddInfo(); // 로그인 추가 정보
        long userId = principal.getUserId();
        loginSuccessAfterService.updateUserLoggedInStatus(userId);
        jwtAuthenticationService.addBlackListByUserIdAndOs(userId, BlackStatus.LOGOUT, loginAddInfo.getOs());
        // Issue RefreshToken
        String realIp = webUtil.getClientIp(request);
        RefreshToken refreshToken = jwtAuthenticationService.issueRefreshToken(userId, loginAddInfo.getOs());
        // create JWT token
        response.addHeader(JwtProperties.HEADER_AUTH,
                jwtAuthenticationService.createJwtToken(principal.getEmail(), userId, realIp, refreshToken.getId(), LoginType.SOCIAL)); // token
        if (refreshToken.getRefreshToken() != null) {
            response.addHeader(JwtProperties.REFRESH_HEADER_STRING, refreshToken.getRefreshToken());  // refreshToken
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(UserSimple.builder()
                    .userId(userId)
                    .email(principal.getEmail())
                    .loginType(LoginType.SOCIAL)
                    .providerType(loginAddInfo.getProviderType())
                    .build());
            okResultResponse(response, result);
        } catch (Exception e) {
            // nothing to do
            log.error("ok response fail");
        }
    }

    /**
     * 본인인증 및 회원가입 요청
     *
     * @param providerType   소셜 로그인 프로바이더 타입
     * @param socialUniqueId 소셜 로그인 프로바이더 고유 아이디
     */
    private Map<String, Object> askToSignUpResult(ProviderType providerType, String socialUniqueId, String socialEmail) throws RestException {
        Map<String, Object> resultMap = new HashMap<>();
        // 회원 가입시 사용자 를 구별할 수 있는 코드
        String validationCode;
        try {
            validationCode = socialValidationService.setValidationCode(providerType, socialUniqueId, socialEmail);
        } catch (Exception e) {
            throw new RestException(ErrorCode.FAILED_GENERATE_SOCIAL_JOIN_VALIDATION_CODE);
        }
        resultMap.put(ResponseConstants.SOCIAL_VALIDATION_CODE, validationCode);

        // TOSS 인증 발급
        TossIdentificationVo tossIdentificationVo;
        try {
            tossIdentificationVo = tossVerificationService.getTossAuthUrlAndTxId();
        } catch (Exception e) {
            throw new RestException(ErrorCode.FAILED_TO_SOCIAL_JOIN_TOSS_REQUEST);
        }
        resultMap.put(ResponseConstants.SOCIAL_AUTH_URL, tossIdentificationVo.getAuthUrl());
        resultMap.put(ResponseConstants.SOCIAL_TX_ID, tossIdentificationVo.getTxId());
        return resultMap;
    }

    /**
     * 로그인  에러 메시지 송출
     * @param response  응답객체
     * @param errorCode 에러 코드
     */
    private void errorResponse(HttpServletResponse response, ErrorCode errorCode) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put(ResponseConstants.ERROR_CODE, errorCode.getCode());
        resultMap.put(ResponseConstants.ERROR_MESSAGE, errorCode.getMessage());
        resultMap.put(ResponseConstants.ERROR_TIMESTAMP, ResponseConstants.DATE_FORMAT.format(new Date()));
        ObjectMapper mapper = new ObjectMapper();
        try {
            PrintWriter out = response.getWriter();
            out.print(mapper.writeValueAsString(resultMap));
            out.flush();
        } catch (Exception e) {
            log.error("error response fail");
        }
    }

    private void okResultResponse(HttpServletResponse response, String result) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            PrintWriter out = response.getWriter();
            out.print(result);
            out.flush();
        } catch (Exception e) {
            log.error("sign up response fail");
        }
    }
}