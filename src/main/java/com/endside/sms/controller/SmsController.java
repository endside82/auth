package com.endside.sms.controller;

import com.endside.sms.model.OtpSmsCheck;
import com.endside.sms.model.OtpSmsSend;
import com.endside.sms.service.SmsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/auth/v1/sms")
public class SmsController {

    private final SmsService smsService;

    public SmsController(SmsService smsService) {
        this.smsService = smsService;
    }

    // 1.1 회원가입시 모바일 번호 확인 OTP SMS 발송
    @PostMapping("/otp/join/send")
    public ResponseEntity<?> sendSmsOtpToJoin(@RequestBody OtpSmsSend otpSmsSend) throws Exception {
        smsService.createOtpToJoinViaSms(otpSmsSend);
        return ResponseEntity.ok().build();
    }

    // 1.2 회원가입시 전화번호 확인 SMS OTP 인증 번호 확인
    @PostMapping("/otp/join/check")
    public ResponseEntity<?> checkSmsOtpToJoin(@RequestBody OtpSmsCheck otpSmsCheck) {
        smsService.checkOtpToJoinViaSms(otpSmsCheck);
        return ResponseEntity.ok().build();
    }

    // 2.1 패스워드 변경을 위한 SMS 발송
    @PostMapping("/otp/pw/send")
    public ResponseEntity<?> sendSmsOtpToChangePw(@RequestBody OtpSmsSend otpSmsSend) throws Exception {
        smsService.createOtpToChangePwViaSms(otpSmsSend);
        return ResponseEntity.ok().build();
    }

    // 2.2 패스워드 변경 위한 SMS 인증 번호 확인
    @PostMapping("/otp/pw/check")
    public ResponseEntity<?> checkSmsOtpToChangePw(@RequestBody OtpSmsCheck otpSmsCheck) throws Exception {
        return ResponseEntity.status(HttpStatus.OK).body(smsService.checkOtpToChangePwViaSms(otpSmsCheck));
    }


}
