package com.endside.config.security;

import com.endside.user.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

/**
 * 인증 관련 이벤트 리스너 ( 로그인 성공 및 실패 후처리 )
 */
@Slf4j
@Configuration
public class AuthenticationEventListener {

    private final LoginAttemptService loginAttemptService;

    public AuthenticationEventListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * login success
     * @param authorizedEvent 로그인 완료 이벤트
     */
    @EventListener
    public void authSuccessEventListener(AuthenticationSuccessEvent authorizedEvent){
        UserPrincipal userPrincipal = (UserPrincipal)authorizedEvent.getAuthentication().getPrincipal();
        loginAttemptService.loginSucceeded(userPrincipal.getUsername()); // 로그인 시도 횟수 체크 초기화
        log.info(userPrincipal.getUsername());
    }

    /**
     * login failure
     * Bad credential 에러가 발생하면이 이벤트를 호출한다.
     * @param notAuthorizedEvent 로그인 실패 이벤트
     */
    @EventListener
    public void authFailureEventListener(AuthenticationFailureBadCredentialsEvent notAuthorizedEvent){
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)notAuthorizedEvent.getSource();
        loginAttemptService.loginFailed((String) token.getPrincipal());
        log.info((String) token.getPrincipal());
    }

}
