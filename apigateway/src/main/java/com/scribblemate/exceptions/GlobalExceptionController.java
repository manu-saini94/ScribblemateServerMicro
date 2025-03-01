package com.scribblemate.exceptions;

import com.scribblemate.common.exceptions.FeignClientException;
import com.scribblemate.common.responses.ErrorResponse;
import com.scribblemate.common.utility.ResponseErrorUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionController {

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, ResponseErrorUtils error,
                                                             String message) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), error.name(), message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(value = FeignClientException.class)
    public ResponseEntity<ErrorResponse> handleFeignClientException(FeignClientException exp) {
        return buildErrorResponse(exp.getMessagecode(), exp.getErrorName(), exp.getMessage());
    }

}
