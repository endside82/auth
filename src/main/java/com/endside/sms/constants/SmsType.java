package com.endside.sms.constants;

import lombok.Getter;

@Getter
public enum SmsType {
    OTP_TO_JOIN("J"),
    OTP_TO_PASSWORD("P"),
    OTP_TO_MOBILE("M");

    final String strCode;
    SmsType(String strCode){
        this.strCode = strCode;
    }
}
