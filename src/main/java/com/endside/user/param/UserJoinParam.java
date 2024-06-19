package com.endside.user.param;

import com.endside.user.constants.AuthType;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.Os;
import com.endside.user.constants.UserType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserJoinParam {
    @JsonIgnore
    private Long userId;
    private String mobile;
    private String password;
    private String uniqueId;
    private Os os;
    private UserType userType;
    private String email;
    private String version;
    private boolean isAgreeMarketing;
    @JsonIgnore
    private LoginType loginType;
    @JsonIgnore
    private AuthType authType;
    @JsonFormat(pattern="yyyyMMdd")
    private LocalDate birthDate;
}