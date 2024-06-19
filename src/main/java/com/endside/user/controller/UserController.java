package com.endside.user.controller;

import com.endside.user.param.PasswordEmailParam;
import com.endside.user.param.PasswordParam;
import com.endside.user.param.PasswordSmsParam;
import com.endside.user.param.UserLeaveParam;
import com.endside.user.service.JwtAuthenticationService;
import com.endside.user.service.UserLeaveService;
import com.endside.user.model.MemberInfo;
import com.endside.user.service.UserService;
import com.endside.config.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@CrossOrigin
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;
    private final UserLeaveService userLeaveService;
    private final JwtAuthenticationService jwtAuthenticationService;

    public UserController(UserService userService, UserLeaveService userLeaveService, JwtAuthenticationService jwtAuthenticationService) {
        this.userService = userService;
        this.userLeaveService = userLeaveService;
        this.jwtAuthenticationService = jwtAuthenticationService;
    }

    @DeleteMapping("/leave")
    @ResponseBody
    public ResponseEntity<?> leave( @RequestBody UserLeaveParam userLeaveParam,
                                    @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userLeaveParam.setUserId(userPrincipal.getUserId());
        userLeaveParam.setLoginType(userPrincipal.getLoginAddInfo().getLoginType());
        userLeaveService.leaveUser(userLeaveParam);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/logout")
    @ResponseBody
    public ResponseEntity<?> logout( @RequestHeader(name = "Authorization") String authToken, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        userLeaveService.logoutUser(userPrincipal.getUserId(), authToken);
        return ResponseEntity.ok().build();
    }

    // 로그인 상태에서 패스워드 업데이트
    @PutMapping("/v1/user/password")
    @ResponseBody
    public ResponseEntity<?> updatePassword(@RequestBody PasswordParam passwordParam, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        passwordParam.setUserId(userPrincipal.getUserId());
        userService.updatePwd(passwordParam);
        return ResponseEntity.ok().build();
    }

    // 비로그인 상태에서 패스워드 업데이트
    @PutMapping("/v1/user/password/noauth/email")
    @ResponseBody
    public ResponseEntity<?> updatePasswordWithEmail(@RequestBody PasswordEmailParam passwordEmailParam) {
        userService.checkTokenByMailToUpdatePwd(passwordEmailParam);
        return ResponseEntity.ok().build();
    }

    // 비로그인 상태에서 패스워드 업데이트
    @PutMapping("/v1/user/password/noauth/sms")
    @ResponseBody
    public ResponseEntity<?> updatePasswordWithSms(@RequestBody PasswordSmsParam passwordSmsParam) {
        userService.checkTokenBySmsToUpdatePwd(passwordSmsParam);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/v1/user/info")
    @ResponseBody
    public ResponseEntity<?> info(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        MemberInfo userInfo = jwtAuthenticationService.getUserInfo(userPrincipal.getUserId());
        return ResponseEntity.ok(userInfo);
    }

}
