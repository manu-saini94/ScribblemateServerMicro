package com.scribblemate.common.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotRetryableException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private String message;


    public NotRetryableException() {
        super();
    }

    public NotRetryableException(String message) {
        super(message);
    }

    public NotRetryableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotRetryableException(Throwable cause) {
        super(cause);
    }
}
