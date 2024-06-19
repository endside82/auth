package com.endside.config.error.exception;


import com.endside.config.error.ErrorCode;
import lombok.Getter;

@Getter
public class EmailRestException extends RestException {
    public EmailRestException() {
        super();
    }

    public EmailRestException(ErrorCode errorCode ){
        super(errorCode);
    }
}
