package com.scribblemate.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNotUpdatedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private String message;

    public UserNotUpdatedException() {
        super();
    }

    public UserNotUpdatedException(String message) {
        this.message = message;
    }
}
