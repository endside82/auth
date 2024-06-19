package com.endside.email.controller;

import com.endside.config.security.UserPrincipal;
import com.endside.email.service.EmailService;
import com.endside.email.model.OtpMailCheck;
import com.endside.email.model.OtpMailSend;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/auth/v1/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    // 1.1 회원가입시 이메일 확인 OTP 이메일 발송
    // checkOnly : 메일 폼 확인용
    @PostMapping("/otp/join/send")
    public ResponseEntity<?> sendMailOtpToJoin(@RequestBody OtpMailSend otpMailSend) throws Exception {
        emailService.createOtpToJoinViaEmail(otpMailSend);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 1.2 회원가입시 이메일 확인 OTP 인증 번호 확인
    @PostMapping("/otp/join/check")
    public ResponseEntity<?> checkMailOtpToJoin(@RequestBody OtpMailCheck otpMailCheck) {
        emailService.checkOtpToJoinViaEmail(otpMailCheck);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 2.1 패스워드 변경을 위한 메일 폼 발송
    @PostMapping("/otp/pw/send")
    public ResponseEntity<?> sendMailOtpToChangePw(@RequestBody OtpMailSend otpMailSend) throws Exception {
        emailService.createOtpToChangePwViaEmail(otpMailSend);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 2.2 패스워드 변경 위한 인증 번호 확인
    @PostMapping("/otp/pw/check")
    public ResponseEntity<?> checkMailOtpToChangePw(@RequestBody OtpMailCheck otpMailCheck) {
        return ResponseEntity.status(HttpStatus.OK).body(emailService.checkOtpToChangePwViaEmail(otpMailCheck));
    }

    // 3.1 이메일 변경을 위한 메일 폼 발송
    @PostMapping("/otp/email/send")
    public ResponseEntity<?> sendMailOtpToChangeEmail(@RequestBody OtpMailSend otpMailSend, @AuthenticationPrincipal UserPrincipal userPrincipal) throws Exception {
        emailService.createOtpToChangeEmailViaEmail(otpMailSend, userPrincipal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    // 3.2 이메일 변경 위한 인증 번호 확인
    @PostMapping("/otp/email/check")
    public ResponseEntity<?> checkMailOtpToChangeEmail(@RequestBody OtpMailCheck otpMailCheck, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        emailService.checkOtpToChangeEmailViaEmail(otpMailCheck, userPrincipal.getUserId());
        return ResponseEntity.status(HttpStatus.OK).build();
    }


}
