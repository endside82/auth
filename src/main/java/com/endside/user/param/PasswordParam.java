package com.endside.user.param;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordParam {
    @JsonIgnore
    private Long userId;
    private String oldPassword;
    private String newPassword;
}
