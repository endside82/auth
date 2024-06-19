package com.endside.email.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtpMailCheck {
    long userId;
    String email;
    String id;
    private String uniqueId;
    boolean checkOnly;
    int otp;
}
