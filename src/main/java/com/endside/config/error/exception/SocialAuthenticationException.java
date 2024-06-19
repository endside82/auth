package com.endside.config.error.exception;

import com.endside.config.error.ErrorCode;

public class SocialAuthenticationException extends RestException {
    public SocialAuthenticationException() {
        super();
    }

    public SocialAuthenticationException(ErrorCode errorCode ){
        super(errorCode);
    }
}