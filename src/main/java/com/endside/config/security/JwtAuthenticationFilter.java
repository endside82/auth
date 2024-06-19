package com.endside.config.security;

import com.endside.config.error.ErrorCode;
import com.endside.config.error.ResponseConstants;
import com.endside.config.error.exception.RestException;
import com.endside.config.security.constants.JwtProperties;
import com.endside.user.constants.BlackStatus;
import com.endside.user.constants.LoginType;
import com.endside.user.model.LoginAddInfo;
import com.endside.user.model.LoginViewModel;
import com.endside.user.model.RefreshToken;
import com.endside.user.model.UserSimple;
import com.endside.user.service.JwtAuthenticationService;
import com.endside.user.service.LoginAttemptService;
import com.endside.user.service.LoginSuccessAfterService;
import com.endside.util.WebUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;

/**
 * 로그인 후 인증 토큰을 발급하는 필터
 */
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final LoginAttemptService loginAttemptService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final LoginSuccessAfterService loginSuccessAfterService;
    private final WebUtil webUtil;

    // constructor
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, LoginAttemptService loginAttemptService, JwtAuthenticationService jwtAuthenticationService, LoginSuccessAfterService loginSuccessAfterService, WebUtil webUtil) {
        this.authenticationManager = authenticationManager;
        this.loginAttemptService = loginAttemptService;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.loginSuccessAfterService = loginSuccessAfterService;
        this.webUtil = webUtil;
        this.setFilterProcessesUrl("/auth/login"); // 로그인 경로
    }

    /**
     * 로그인 시도
     *
     * @param request
     * @param response
     * @return
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        // 로그인 입력 정보
        LoginViewModel credentials;
        try {
            credentials = new ObjectMapper().readValue(request.getInputStream(), LoginViewModel.class);
        } catch (IOException e) { // 입력정보를 파싱하지 못함
            errorResponse(response, ErrorCode.LOGIN_FAILURE_NO_CREDENTIAL, 0);
            return null;
        }

        // 필수 입력정보가 없음
        if (credentials == null || !StringUtils.hasText(credentials.getEmail()) || !StringUtils.hasText(credentials.getPassword())) {
            errorResponse(response, ErrorCode.LOGIN_FAILURE_REQUIRED_PARAMETER, 0);
            return null;
        }

        // 유저 인증을 위한 Access token
        LoginAddInfo loginAddInfo = new LoginAddInfo();
        loginAddInfo.setLoginType(LoginType.EMAIL);
        loginAddInfo.setOs(credentials.getOs());

        // Create login token
        CustomAuthenticationToken authenticationToken = new CustomAuthenticationToken(credentials.getEmail(), credentials.getPassword(), loginAddInfo); // authority
        Authentication authentication;
        try {
            // 인증 시행 CustomUserDetailsAuthenticationProvider
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            Object cause = e.getCause();
            // RestException 이면 (즉 비지니스 로직상 정의된 예외일 경우)
            if (cause instanceof RestException) {
                ErrorCode errorCode = ((RestException) cause).getErrorCode();
                errorResponse(response, errorCode, 0);
            } else {
                // 패스워드 틀림 등 인증 정보 불일치 ( 스프링 시큐리티에서 발생한 에러 처리 )
                if (e instanceof BadCredentialsException) {
                    int count = loginAttemptService.getCount(credentials.getEmail());
                    errorResponse(response, ErrorCode.LOGIN_FAILURE_BAD_CREDENTIAL, count);
                } else {
                    errorResponse(response, ErrorCode.LOGIN_FAILURE_BAD_CREDENTIAL, 0);
                }
            }
            return null;
        }
        return authentication;
    }

    // 로그인 성공시 1st
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) {
        UserPrincipal principal = (UserPrincipal) authResult.getPrincipal();
        LoginAddInfo loginAddInfo = principal.getLoginAddInfo(); // 로그인 추가 정보
        long userId = principal.getUserId();
        loginSuccessAfterService.updateUserLoggedInStatus(userId);
        jwtAuthenticationService.addBlackListByUserIdAndOs(userId, BlackStatus.LOGOUT, loginAddInfo.getOs());
        // Issue RefreshToken
        String realIp = webUtil.getClientIp(request);
        RefreshToken refreshToken = jwtAuthenticationService.issueRefreshToken(userId, loginAddInfo.getOs());
        // create JWT token
        response.addHeader(JwtProperties.HEADER_AUTH,
                jwtAuthenticationService.createJwtToken(principal.getEmail(), userId, realIp, refreshToken.getId(), LoginType.EMAIL));
        if (refreshToken.getRefreshToken() != null) {
            response.addHeader(JwtProperties.REFRESH_HEADER_STRING, refreshToken.getRefreshToken());  // refreshToken
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            String result = mapper.writeValueAsString(UserSimple.builder()
                    .userId(userId)
                    .email(principal.getEmail())
                    .loginType(LoginType.EMAIL)
                    .providerType(loginAddInfo.getProviderType())
                    .build());
            okResultResponse(response, result);
        } catch (Exception e) {
            // nothing to do
            log.error("ok response fail");
        }
    }

    /**
     * 로그인 에러 메시지 송출
     *
     * @param response  응답객체
     * @param errorCode 정의된 에러 코드
     * @param count     로그인 시도 에러 횟수
     */
    private void errorResponse(HttpServletResponse response, ErrorCode errorCode, int count) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put(ResponseConstants.ERROR_CODE, errorCode.getCode());
        resultMap.put(ResponseConstants.ERROR_MESSAGE, errorCode.getMessage());
        resultMap.put(ResponseConstants.ERROR_TIMESTAMP, ResponseConstants.DATE_FORMAT.format(new Date()));
        if (count > 0) {
            resultMap.put(ResponseConstants.ERROR_COUNT, count);
        }
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
