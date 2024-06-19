package com.endside.config.error.exception;


import com.endside.config.error.ErrorCode;

public class InvalidAuthTokenException extends RestException {
    public InvalidAuthTokenException() {
        super();
    }

    public InvalidAuthTokenException(ErrorCode errorCode ){
        super(errorCode);
    }
}
