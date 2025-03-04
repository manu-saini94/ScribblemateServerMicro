package com.scribblemate.common.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotAuthorizedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String message;

    public NotAuthorizedException() {
        super();
    }

    public NotAuthorizedException(String message) {
        this.message = message;
    }
}
