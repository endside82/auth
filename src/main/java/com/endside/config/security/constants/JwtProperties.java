package com.endside.config.security.constants;

public class JwtProperties {
    public static final String AUDIENCE = "com.endside.auth";
    public static final String CLAIM_LOGIN_TYPE = "login_type";
    public static final int EXPIRATION_TIME_3DAY_MILLI = 259_200_000; // 3 days
    public static final int EXPIRATION_TIME_3DAY_SECOND = 259_200; // 3 days
    public static final int REFRESH_TOKEN_NEED_REISSUE = 4; // 4 days
    //public static final int EXPIRATION_TIME_1HOUR = 3600000;
    //public static final int EXPIRATION_TIME_24HOUR = 86400000;
    public static final int REFRESH_TOKEN_EXPIRATION_DATE = 30; // refreshToken 30 day
    public static final String HEADER_AUTH = "Authorization";
    public static final String REFRESH_HEADER_STRING = "ReAuthentication";
    public static final String RESULT_MAP_AUTH = "auth";
    public static final String RESULT_MAP_REFRESH = "refresh";
    public static final String REFRESH_TOKEN_Id_KEY = "refresh";

    public static final String SPLITTER = ",";
}
