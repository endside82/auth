package com.endside.util;

import org.springframework.stereotype.Component;

@Component
public class RedisKeyGen {
    public static final String COUNT = "CNT";
    public static final String OTP = "OTP";
    public static final String AUTH = "AUTH";
    private static final String SAP = ":";

    public String generateKeyId(String type, String typeStrCode, String id, String value) {
        return type + SAP + typeStrCode + SAP + id + SAP + value;
    }
}
