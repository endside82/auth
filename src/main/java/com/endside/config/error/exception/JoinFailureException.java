package com.endside.config.error.exception;

import com.endside.config.error.ErrorCode;

public class JoinFailureException extends RestException {
    public JoinFailureException() {
        super();
    }

    public JoinFailureException(ErrorCode errorCode ){
        super(errorCode);
    }
}
