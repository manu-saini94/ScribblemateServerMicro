package com.scribblemate.exceptions;

public class UsersFetchException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String message;

    public UsersFetchException() {
        super();
    }

    public UsersFetchException(String message) {
        this.message = message;
    }

    public UsersFetchException(Throwable cause) {
        super(cause);
    }
}