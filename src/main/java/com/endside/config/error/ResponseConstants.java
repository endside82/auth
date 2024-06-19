package com.endside.config.error;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ResponseConstants {
    public static final String ERROR_CODE = "error_code";
    public static final String ERROR_COUNT = "error_count";
    public static final String ERROR_MESSAGE = "message";
    public static final String ERROR_TIMESTAMP = "timestamp";
    public static final String SOCIAL_VALIDATION_CODE = "validationCode";
    public static final String SOCIAL_AUTH_URL = "authUrl";
    public static final String SOCIAL_TX_ID = "txId";

    public static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
}
