package com.endside.social.exception;


public class TossAccessTokenException extends RuntimeException {
    public TossAccessTokenException() {
        super();
    }

    public TossAccessTokenException(String msg ){
        super(msg);
    }
}
