package com.endside.user.service;

import com.endside.user.repository.UserRepository;
import com.endside.user.constants.UserStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

// 로그인 성공 후처리 비지니스 로직
@Service
public class LoginSuccessAfterService {

    private final UserRepository userRepository;

    public LoginSuccessAfterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 로그인 시기만 업데이트
    public void updateUserLoggedInStatus(long userId) {
        userRepository.findByUserId(userId).ifPresent(selectUser ->{
            selectUser.setLoginAt(LocalDateTime.now());
            selectUser.setStatus(UserStatus.NORMAL);
            userRepository.save(selectUser);
        });
    }


}
