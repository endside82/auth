package com.endside.config.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Common JWT api call (1000XX)
    JWT_TOKEN_AUTH_ERROR(401,100001,"JWT_TOKEN_AUTH_ERROR"),
    JWT_TOKEN_EXPIRATION(401,100002,"JWT_TOKEN_EXPIRATION"),
    INVALID_AUTH_TOKEN(401,100003,"INVALID_AUTH_TOKEN"),
    // USER JOIN (1101XX)
    EXIST_PHONE_NO(400,110101,"EXIST_PHONE_NO"),
    MOBILE_REGEX_VALIDATION(400,110102,"MOBILE_REGEX_VALIDATION"),
    EMAIL_REGEX_VALIDATION(400,110103, "EMAIL_REGEX_VALIDATION"),
    EXIST_EMAIL(400,10104,"EXIST_EMAIL"),
    FAILED_TO_JOIN_BY_INVALID_EMAIL_AUTH(403,  110105 , "FAILED_TO_JOIN_BY_INVALID_EMAIL_AUTH"),
    FAILED_TO_JOIN_BY_INVALID_MOBILE_AUTH(403,  110106 , "FAILED_TO_JOIN_BY_INVALID_MOBILE_AUTH"),
    FAILED_TO_JOIN_SAVE_USER(401,  110107 , "FAILED_TO_JOIN_SAVE_USER"),
    FAILED_TO_JOIN_NO_SOCIAL_VALIDATION_CODE(400, 110108, "FAILED_TO_JOIN_NO_SOCIAL_VALIDATION_CODE"),
    FAILED_TO_JOIN_ALREADY_JOIN_USER(401, 110109, "FAILED_TO_JOIN_ALREADY_JOIN_USER"),
    FAILED_TO_JOIN_VALIDATION_CODE_NOT_EXISTS(403, 110110, "FAILED_TO_JOIN_VALIDATION_CODE_NOT_EXISTS"),
    FAILED_GENERATE_SOCIAL_JOIN_VALIDATION_CODE(500, 110111,"FAILED_GENERATE_SOCIAL_JOIN_VALIDATION_CODE"),
    FAILED_TO_SOCIAL_JOIN_TOSS_REQUEST(400, 110112,"FAILED_TO_SOCIAL_JOIN_TOSS_REQUEST"),
    FAILED_GET_TOSS_ACCESS_TOKEN(500, 110113 ,"FAILED_GET_TOSS_ACCESS_TOKEN" ),
    // USER LOGIN (102XX)
    LOGIN_FAILURE_NO_CREDENTIAL(401,110201,"LOGIN_FAILURE_NO_CREDENTIAL"),
    LOGIN_FAILURE_REQUIRED_PARAMETER(400,110202,"LOGIN_FAILURE_REQUIRED_PARAMETER"),
    LOGIN_FAILURE_NO_EXIST_USER(403,110203,"LOGIN_FAILURE_NO_EXIST_USER"),
    BLOCKED_USER(403,110204,"BLOCKED_USER"),                                             // 로그인 시도 횟수 초과
    LOGIN_FAILURE_USER_STATUS_EXIT(403,110205,"LOGIN_FAILURE_USER_STATUS_EXIT"),         // 탈퇴
    LOGIN_FAILURE_USER_STATUS_TRYEXIT(403,110206,"LOGIN_FAILURE_USER_STATUS_TRYEXIT"),   // 탈퇴진행
    LOGIN_FAILURE_USER_STATUS_LOGOUT(403,110207,"LOGIN_FAILURE_USER_STATUS_LOGOUT"),     // 로그아웃됨
    LOGIN_FAILURE_USER_STATUS_STOP(403,110208,"LOGIN_FAILURE_USER_STATUS_STOP"),         // 이용정지됨
    LOGIN_FAILURE_USER_STATUS_BAN(403,110209,"LOGIN_FAILURE_USER_STATUS_BAN"),           // 강제탈퇴
    LOGIN_FAILURE_BAD_CREDENTIAL(403,110210,"LOGIN_FAILURE_BAD_CREDENTIAL"),
    SOCIAL_LOGIN_FAILURE_BAD_CREDENTIAL(403,110211,"SOCIAL_LOGIN_FAILURE_BAD_CREDENTIAL"),
    SOCIAL_LOGIN_FAILURE_PROVIDER_VERIFICATION_FAIL(403,110212,"SOCIAL_LOGIN_FAILURE_PROVIDER_VERIFICATION_FAIL"),
    // USER TOKEN REFRESH (103XX)
    TOKEN_REFRESH_FAIL_NO_USER(403,110301,"TOKEN_REFRESH_FAIL_NO_USER"),
    TOKEN_REFRESH_FAIL_TOKEN_EXPIRED(401,110302,"TOKEN_REFRESH_FAIL_TOKEN_EXPIRED"),
    TOKEN_REFRESH_FAIL_INVALID_REFRESH_TOKEN(401,110303,"TOKEN_REFRESH_FAIL_INVALID_REFRESH_TOKEN"),
    TOKEN_REFRESH_INVALID_AUTH_TOKEN(401,110304,"TOKEN_REFRESH_INVALID_AUTH_TOKEN"),
    // USER LOGOUT / LEAVE (1104XX)
    PASSWORD_MISMATCH(400,110401,"PASSWORD_MISMATCH"),
    USER_ALREADY_LEAVE_STATUS(400,110402,"USER_ALREADY_LEAVE_STATUS"),
    // AWS SES (2001XX)
    AWS_MAIL_SERVER(500,200101,"AWS_MAIL_SERVER"),
    ERROR_ENCODING(500,200102,"ERROR_ENCODING"),
    // AUTH EMAIL(1106XX)
    SEND_BEFORE_LIMIT_TIME(403,110601,"SEND_BEFORE_LIMIT_TIME"), // 재전송 제한 시간을 이전에 보냄
    CNT_EXCEED_DAILY(403,110602,"CNT_EXCEED_DAILY"),             // 일별 제한횟수 초과
    OTP_MISMATCH(400,110603,"OTP_MISMATCH"),                     // OTP 일치하지 않음
    NON_EXIST_OTP(400,110604,"NON_EXIST_OTP"),                   // 해당 OTP정보가 없음
    EMAIL_NOT_FOUND(403,110605,"EMAIL_NOT_FOUND"),               // 존재하지 않는 이메일
    FAILED_TO_CHANGE_PW_INVALID_TOKEN(403,110606,"FAILED_TO_CHANGE_PW_INVALID_TOKEN"),
    SAME_CURRENT_PASSWORD(400,110607,"SAME_CURRENT_PASSWORD"),
    SAME_CURRENT_EMAIL(400,110608,"SAME_CURRENT_EMAIL"),
    FAILED_TO_CHANGE_PW_REQUIRED_PARAMETER(400,110609,"FAILED_TO_CHANGE_PW_REQUIRED_PARAMETER"),
    // AUTH SMS (1107XX)
    FAILED_TO_SEND_SMS(400,10701,"FAILED_TO_SEND_SMS"),
    NOT_EXIST_USER(404,10702,"NOT_EXIST_USER")
    ;

    private final int code;
    private final int status;
    private final String message;

    ErrorCode(final int status, final int code, final String message) {
        this.status = status;
        this.message = message;
        this.code = code;
    }
}
