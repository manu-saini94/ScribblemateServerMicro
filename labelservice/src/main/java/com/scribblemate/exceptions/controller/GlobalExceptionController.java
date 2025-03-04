package com.scribblemate.exceptions.controller;

import com.scribblemate.common.exceptions.NotAuthorizedException;
import com.scribblemate.exceptions.labels.*;
import com.scribblemate.common.utility.ResponseErrorUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.scribblemate.common.responses.ErrorResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionController {

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, ResponseErrorUtils error,
                                                             String message) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), error.name(), message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exp) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS,
                exp.getMessage());
    }

    @ExceptionHandler(value = LabelNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLabelNotFoundException(LabelNotFoundException exp) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ResponseErrorUtils.LABELS_NOT_FOUND, exp.getMessage());
    }

    @ExceptionHandler(value = LabelsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLabelsNotFoundException(LabelsNotFoundException exp) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ResponseErrorUtils.LABELS_NOT_FOUND, exp.getMessage());
    }

    @ExceptionHandler(value = LabelNotPersistedException.class)
    public ResponseEntity<ErrorResponse> handleLabelNotPersistedException(LabelNotPersistedException exp) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.LABEL_PERSIST_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = LabelNotDeletedException.class)
    public ResponseEntity<ErrorResponse> handleLabelNotDeletedException(LabelNotDeletedException exp) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.LABEL_DELETE_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = LabelNotUpdatedException.class)
    public ResponseEntity<ErrorResponse> handleLabelNotUpdatedException(LabelNotUpdatedException exp) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.LABEL_UPDATE_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = LabelAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> handleLabelAlreadyExistException(LabelAlreadyExistException exp) {
        return buildErrorResponse(HttpStatus.CONFLICT, ResponseErrorUtils.LABEL_ALREADY_EXIST_ERROR, exp.getMessage());
    }

    @ExceptionHandler(value = NotAuthorizedException.class)
    public ResponseEntity<ErrorResponse> handleNotAuthorizedException(NotAuthorizedException exp) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS,
                exp.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exp) {
        log.error("An unexpected error occurred", exp);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.INTERNAL_SERVER_ERROR,
                exp.getMessage());
    }

}
