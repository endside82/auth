package com.endside.config.error.exception;

import com.endside.config.error.ErrorCode;

public class SocialVerificationException  extends RestException {
    public SocialVerificationException() {
        super();
    }

    public SocialVerificationException(ErrorCode errorCode ){
        super(errorCode);
    }
}
