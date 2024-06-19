package com.endside.sms.service;


import com.endside.sms.constants.SmsType;
import org.springframework.stereotype.Service;

@Service
public class SmsContentBuilder {

    public String buildContentOtp(SmsType smsType, int otp){
        String contentsString = switch (smsType) {
            case OTP_TO_JOIN -> "Your Verification Code is ";
            case OTP_TO_PASSWORD -> "Here is the code for reset password ";
            default -> "";
        };
        return contentsString + "[" + otp + "]";
    }
}
