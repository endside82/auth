package com.endside.config.error.exception;


import com.endside.config.error.ErrorCode;

public class InvalidParameterException extends RestException {
    public InvalidParameterException() {
        super();
    }

    public InvalidParameterException(ErrorCode errorCode ){
        super(errorCode);
    }
}