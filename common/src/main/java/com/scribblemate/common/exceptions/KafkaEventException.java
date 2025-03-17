package com.scribblemate.common.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KafkaEventException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String message;


    public KafkaEventException() {
        super();
    }

    public KafkaEventException(String message) {
        this.message = message;
    }
    public KafkaEventException(Throwable cause) {
        super(cause);
    }

    public KafkaEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
