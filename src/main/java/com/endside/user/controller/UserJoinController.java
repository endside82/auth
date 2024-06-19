package com.endside.user.controller;

import com.endside.user.model.UserSimple;
import com.endside.user.param.EmailCheckParam;
import com.endside.user.service.JwtAuthenticationService;
import com.endside.user.service.join.*;
import com.endside.user.constants.LoginType;
import com.endside.util.WebUtil;
import com.endside.user.param.UserJoinParam;
import com.endside.user.param.MobileCheckParam;
import com.endside.config.security.constants.JwtProperties;

import com.endside.user.param.SocialUserJoinParam;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;


@Slf4j
@RestController
@RequestMapping("/auth")
public class UserJoinController {

    private final TestUserJoinService testUserJoinService;
    private final MobileUserJoinService mobileUserJoinService;
    private final EmailUserJoinService emailUserJoinService;
    private final UserJoinCommonService userJoinCommonService;
    private final SocialUserJoinService socialUserJoinService;
    private final JwtAuthenticationService jwtAuthenticationService;
    private final WebUtil webUtil;

    public UserJoinController(TestUserJoinService testUserJoinService, MobileUserJoinService mobileUserJoinService, EmailUserJoinService emailUserJoinService, UserJoinCommonService userJoinCommonService, JwtAuthenticationService jwtAuthenticationService,
                              WebUtil webUtil, SocialUserJoinService socialUserJoinService) {
        this.testUserJoinService = testUserJoinService;
        this.mobileUserJoinService = mobileUserJoinService;
        this.emailUserJoinService = emailUserJoinService;
        this.userJoinCommonService = userJoinCommonService;
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.webUtil = webUtil;
        this.socialUserJoinService = socialUserJoinService;
    }

    // 회원 가입
    @PostMapping("/join/email")
    @ResponseBody
    public ResponseEntity<?> joinEmail(@RequestBody UserJoinParam userJoinParam,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
                UserSimple userSimple = emailUserJoinService.joinUser(userJoinParam);
        HashMap<String, String> newTokens = jwtAuthenticationService.createTokensAfterJoin(userSimple.getEmail(), userSimple.getUserId(), webUtil.getClientIp(request), LoginType.EMAIL, userJoinParam.getOs());
        response.addHeader(JwtProperties.HEADER_AUTH, newTokens.get(JwtProperties.RESULT_MAP_AUTH));
        response.addHeader(JwtProperties.REFRESH_HEADER_STRING, newTokens.get(JwtProperties.RESULT_MAP_REFRESH));  // refresh 토큰
        return ResponseEntity.ok(userSimple);
    }
    @PostMapping("/join/mobile")
    @ResponseBody
    public ResponseEntity<?> joinMobile(@RequestBody UserJoinParam userJoinParam,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        UserSimple userSimple = mobileUserJoinService.joinUser(userJoinParam);
        HashMap<String, String> newTokens = jwtAuthenticationService.createTokensAfterJoin(userSimple.getEmail(), userSimple.getUserId(), webUtil.getClientIp(request), LoginType.EMAIL, userJoinParam.getOs());
        response.addHeader(JwtProperties.HEADER_AUTH, newTokens.get(JwtProperties.RESULT_MAP_AUTH));
        response.addHeader(JwtProperties.REFRESH_HEADER_STRING, newTokens.get(JwtProperties.RESULT_MAP_REFRESH));  // refresh 토큰
        return ResponseEntity.ok(userSimple);
    }

    @PostMapping("/join/social")
    public ResponseEntity<?> joinSocial(@RequestBody SocialUserJoinParam socialUserJoinParam,
                                     HttpServletRequest request, HttpServletResponse response) throws Exception {
        UserSimple userSimple = socialUserJoinService.joinUser(socialUserJoinParam);
        HashMap<String, String> newTokens = jwtAuthenticationService.createTokensAfterJoin(userSimple.getEmail(), userSimple.getUserId(), webUtil.getClientIp(request), LoginType.SOCIAL, socialUserJoinParam.getOs());
        response.addHeader(JwtProperties.HEADER_AUTH, newTokens.get(JwtProperties.RESULT_MAP_AUTH));
        response.addHeader(JwtProperties.REFRESH_HEADER_STRING, newTokens.get(JwtProperties.RESULT_MAP_REFRESH));  // refresh 토큰
        return ResponseEntity.ok(userSimple);
    }

    // 회원 가입 간략 TEST 버전
    @PostMapping("/join/test")
    @ResponseBody
    public ResponseEntity<?> joinTest(@RequestBody UserJoinParam userJoinParam,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        UserSimple userSimple = testUserJoinService.joinUser(userJoinParam);
        HashMap<String, String> newTokens = jwtAuthenticationService.createTokensAfterJoin(userSimple.getEmail(), userSimple.getUserId(), webUtil.getClientIp(request), LoginType.EMAIL, userJoinParam.getOs());
        response.addHeader(JwtProperties.HEADER_AUTH, newTokens.get(JwtProperties.RESULT_MAP_AUTH));
        response.addHeader(JwtProperties.REFRESH_HEADER_STRING, newTokens.get(JwtProperties.RESULT_MAP_REFRESH));  // refresh 토큰
        return ResponseEntity.ok(userSimple);
    }

    // 이메일 사용 가능 여부 체크
    @PostMapping("/v1/email/available")
    @ResponseBody
    public ResponseEntity<?> checkEmailIsAvailable(@RequestBody EmailCheckParam emailCheckParam) {
        userJoinCommonService.checkHasSameEmail(emailCheckParam.getEmail());
        return ResponseEntity.ok().build();
    }

    // 전화번호 사용 가능 여부 체크
    @PostMapping("/v1/mobile/available")
    @ResponseBody
    public ResponseEntity<?> checkMobileIsAvailable(@RequestBody MobileCheckParam mobileCheckParam) {
        userJoinCommonService.checkHasSameMobile(mobileCheckParam.getMobile());
        return ResponseEntity.ok().build();
    }
}
