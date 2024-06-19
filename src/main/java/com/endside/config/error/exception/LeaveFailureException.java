package com.endside.config.error.exception;


import com.endside.config.error.ErrorCode;

public class LeaveFailureException extends RestException {
    public LeaveFailureException() {
        super();
    }

    public LeaveFailureException(ErrorCode errorCode ){
        super(errorCode);
    }
}
