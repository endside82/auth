package com.endside.email.constants;

import lombok.Getter;

@Getter
public enum EmailType {
    OTP_TO_JOIN("J"),
    OTP_TO_PASSWORD("P"),
    OTP_TO_EMAIL("E"),
    AUTH_MAIL("W"),
    JOIN_SUCCESS("S");

    final String strCode;


    EmailType(String strCode){
        this.strCode = strCode;
    }

}
