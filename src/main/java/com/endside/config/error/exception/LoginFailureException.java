package com.endside.config.error.exception;


import com.endside.config.error.ErrorCode;

public class LoginFailureException extends RestException {
    public LoginFailureException() {
        super();
    }

    public LoginFailureException(ErrorCode errorCode ){
        super(errorCode);
    }
}
