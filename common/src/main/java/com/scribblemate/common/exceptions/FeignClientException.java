package com.scribblemate.common.exceptions;

import com.scribblemate.common.utility.ResponseErrorUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class FeignClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private HttpStatus messagecode;
    private ResponseErrorUtils errorName;
    private String message;

    public FeignClientException(HttpStatus messagecode, ResponseErrorUtils errorName, String message) {
        super();
        this.messagecode = messagecode;
        this.errorName = errorName;
        this.message = message;
    }
}
