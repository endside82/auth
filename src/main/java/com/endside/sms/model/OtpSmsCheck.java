package com.endside.sms.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtpSmsCheck {
    long userId;
    String uniqueId;
    String mobile;
    boolean checkOnly;
    int otp;
}
