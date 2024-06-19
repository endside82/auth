package com.endside.user.model;

import com.endside.user.constants.Os;
import lombok.Getter;
import lombok.Setter;

/**
 * 로그인 입력값
 */
@Getter
@Setter
public class LoginViewModel {
    private String email;
    private String password;
    private Os os;
}