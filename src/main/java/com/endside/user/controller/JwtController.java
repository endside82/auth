package com.endside.user.controller;

import com.endside.user.model.UserSimple;
import com.endside.user.service.JwtAuthenticationService;
import com.endside.util.WebUtil;
import com.endside.user.param.RefreshTokenParam;
import com.endside.config.security.constants.JwtProperties;
import com.endside.config.security.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class JwtController {

    private final JwtAuthenticationService jwtAuthenticationService;

    private final WebUtil webUtil;

    public JwtController(JwtAuthenticationService jwtAuthenticationService, WebUtil webUtil) {
        this.jwtAuthenticationService = jwtAuthenticationService;
        this.webUtil = webUtil;
    }

    // auth token refresh
    @RequestMapping(value = {"/token/refresh"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<?> storeClientSeverKey(
            @RequestHeader(name = "Authorization") String authToken,
            @RequestBody RefreshTokenParam refreshTokenParam,
            HttpServletRequest request,
            HttpServletResponse response) {
        // 토큰 리프레쉬 재발급
        String ip = webUtil.getClientIp(request);
        Map<String, String> newTokens = jwtAuthenticationService.refreshToken(authToken, refreshTokenParam.getRefreshToken(), ip);
        // auth 토큰
        response.addHeader(JwtProperties.HEADER_AUTH, newTokens.get(JwtProperties.RESULT_MAP_AUTH));
        // refresh 토큰
        if (newTokens.containsKey(JwtProperties.RESULT_MAP_REFRESH)) {
            response.addHeader(JwtProperties.REFRESH_HEADER_STRING, newTokens.get(JwtProperties.RESULT_MAP_REFRESH));
        }
        return ResponseEntity.ok().build();
    }


    // auth token 동작 확인 (내부 서버 호출용)
    @RequestMapping(value = {"/jwt/hello"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<?> jwtConfirm(
            @RequestHeader(name = "Authorization") String authToken,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok().build();
    }

    // auth token이 동작하는지 확인
    @RequestMapping(value = {"/token/check"}, method = {RequestMethod.POST})
    @ResponseBody
    public ResponseEntity<?> tokenCheck(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity
                .ok(UserSimple.builder()
                        .userId(userPrincipal.getUserId())
                        .email(userPrincipal.getEmail())
                        .loginType(userPrincipal.getLoginAddInfo().getLoginType()).build());
    }
}
