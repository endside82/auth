package com.endside.user.param;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordSmsParam {
    private String token;
    private String mobile;
    private String password;
}
