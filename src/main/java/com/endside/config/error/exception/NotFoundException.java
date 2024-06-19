package com.endside.config.error.exception;
import com.endside.config.error.ErrorCode;

public class NotFoundException extends RestException {

    public NotFoundException() {
        super();
    }

    public NotFoundException(ErrorCode errorCode ){
        super(errorCode);
    }

}
