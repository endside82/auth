package com.endside.sms.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OtpSmsSend {
    String mobile;
    String uniqueId;
    boolean checkOnly;
}
