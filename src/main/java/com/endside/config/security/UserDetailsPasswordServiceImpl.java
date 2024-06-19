package com.endside.config.security;

import com.endside.user.model.LoginAddInfo;
import com.endside.user.model.Users;
import com.endside.user.service.LoginAttemptService;
import com.endside.config.error.exception.LoginFailureException;
import com.endside.user.repository.UserRepository;
import com.endside.config.error.ErrorCode;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service("userDetailsService")
public class UserDetailsPasswordServiceImpl implements UserDetailsService {

    private UserRepository userRepository;           // 계정 리포지토리
    private LoginAttemptService loginAttemptService; // 로그인 시도 횟수 검증
    private final int USER_STATUS_STOP = 2;
    private final int USER_STATUS_BAN = 3;
    private final int USER_STATUS_TRYEXIT = 4;
    private final int USER_STATUS_EXIT = 5;


    // 로그인을 위한 유저 정보 취득
    @Override
    public UserDetails loadUserByUsernameAndDomain(String email, LoginAddInfo loginAddInfo) throws UsernameNotFoundException {
        if (loginAttemptService.isBlocked(email)) {
            throw new LoginFailureException(ErrorCode.BLOCKED_USER);
        }
        Users users = this.userRepository.findByEmail(email).orElseThrow(
                () -> new LoginFailureException(ErrorCode.LOGIN_FAILURE_NO_EXIST_USER)
        );
        int status = getStatus(users);
        // 로그인 추가 정보는 전달
        return UserPrincipal.builder()
                .userId(users.getUserId())
                .email(users.getEmail())
                .password(users.getPassword())
                .status(status)
                .mobile(users.getMobile())
                .loginAddInfo(loginAddInfo)
                .build();

    }

    private int getStatus(Users users) {
        int status = users.getStatus().getStatus();
        switch (status) {
            // 1은 로그아웃
            case USER_STATUS_STOP -> throw new LoginFailureException(ErrorCode.LOGIN_FAILURE_USER_STATUS_STOP);    // 이용정지됨
            case USER_STATUS_BAN -> throw new LoginFailureException(ErrorCode.LOGIN_FAILURE_USER_STATUS_BAN);     // 강제탈퇴
            case USER_STATUS_TRYEXIT -> throw new LoginFailureException(ErrorCode.LOGIN_FAILURE_USER_STATUS_TRYEXIT); // 탈퇴진행
            case USER_STATUS_EXIT -> throw new LoginFailureException(ErrorCode.LOGIN_FAILURE_USER_STATUS_EXIT); // 탈퇴
            default -> {}
        }
        return status;
    }
}
