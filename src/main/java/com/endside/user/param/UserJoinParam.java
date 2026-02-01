package com.endside.user.param;

import com.endside.user.constants.AuthType;
import com.endside.user.constants.LoginType;
import com.endside.user.constants.Os;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserJoinParam {
    @JsonIgnore
    private Long userId;
    private String loginId;
    private String mobile;
    private String password;
    private String uniqueId;
    private Os os;
    private LoginType loginType;
    private String email;
    private String version;
    private boolean isAgreeMarketing;
    private boolean isAgreeParentAlarm;
    private boolean isPa;
    @JsonIgnore
    private AuthType authType;
    @JsonFormat(pattern="yyyyMMdd")
    private LocalDate birthDate;
}
