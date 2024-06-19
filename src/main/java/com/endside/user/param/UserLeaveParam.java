package com.endside.user.param;

import com.endside.user.constants.LoginType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserLeaveParam {
    @JsonIgnore
    private long userId;
    private String password;     // login Type IDPASS ONLY
    private LoginType loginType;
}


















