package com.endside.user.param;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordEmailParam {
    private String token;
    private String email;
    private String password;
}
