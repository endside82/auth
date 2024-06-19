package com.endside.user.param;

import com.endside.user.constants.Os;
import com.endside.user.constants.UserType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialUserJoinParam {
    private String validationCode;
    private String txId;
    private UserType userType;
    private String uniqueId;
    private Os os;
}
