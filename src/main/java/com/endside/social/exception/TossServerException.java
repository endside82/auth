package com.endside.social.exception;


public class TossServerException extends RuntimeException {
    public TossServerException() {
        super();
    }

    public TossServerException(String msg ){
        super(msg);
    }
}
