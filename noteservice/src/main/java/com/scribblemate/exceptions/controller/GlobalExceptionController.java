package com.scribblemate.exceptions.controller;

import com.scribblemate.common.exceptions.TokenExpiredException;
import com.scribblemate.common.exceptions.TokenMissingOrInvalidException;
import com.scribblemate.common.exceptions.UserNotFoundException;
import com.scribblemate.common.responses.ErrorResponse;
import com.scribblemate.common.utility.ResponseErrorUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.scribblemate.exceptions.notes.CollaboratorAlreadyExistException;
import com.scribblemate.exceptions.notes.CollaboratorDoesNotExistException;
import com.scribblemate.exceptions.notes.CollaboratorNotAddedException;
import com.scribblemate.exceptions.notes.CollaboratorNotDeletedException;
import com.scribblemate.exceptions.notes.NoteNotFoundException;
import com.scribblemate.exceptions.notes.NoteNotPersistedException;
import com.scribblemate.exceptions.notes.NoteNotUpdatedException;
import com.scribblemate.exceptions.notes.NotesNotFoundException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionController {

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, ResponseErrorUtils error,
                                                             String message) {
        ErrorResponse errorResponse = new ErrorResponse(status.value(), error.name(), message);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(value = BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException exp) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ResponseErrorUtils.USERNAME_OR_PASSWORD_INCORRECT,
                exp.getMessage());
    }

    @ExceptionHandler(value = AccountStatusException.class)
    public ResponseEntity<ErrorResponse> handleAccountStatusException(AccountStatusException exp) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ResponseErrorUtils.ACCOUNT_IS_LOCKED, exp.getMessage());
    }

    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException exp) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ResponseErrorUtils.NOT_AUTHORIZED_TO_ACCESS,
                exp.getMessage());
    }

    @ExceptionHandler(value = TokenMissingOrInvalidException.class)
    public ResponseEntity<ErrorResponse> handleTokenMissingOrInvalidException(TokenMissingOrInvalidException exp) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, ResponseErrorUtils.TOKEN_MISSING_OR_INVALID,
                exp.getMessage());
    }

    @ExceptionHandler(value = TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpiredException(TokenExpiredException exp) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, ResponseErrorUtils.TOKEN_EXPIRED, exp.getMessage());
    }


    @ExceptionHandler(value = NoteNotFoundException.class)
    public ResponseEntity<ErrorResponse> noteNotFoundException(NoteNotFoundException exp) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ResponseErrorUtils.NOTE_NOT_FOUND, exp.getMessage());
    }

    @ExceptionHandler(value = NotesNotFoundException.class)
    public ResponseEntity<ErrorResponse> notesNotFoundException(NotesNotFoundException exp) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ResponseErrorUtils.NOTES_NOT_FOUND, exp.getMessage());
    }

    @ExceptionHandler(value = NoteNotPersistedException.class)
    public ResponseEntity<ErrorResponse> noteNotPersistedException(NoteNotPersistedException exp) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.NOTE_PERSIST_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = NoteNotUpdatedException.class)
    public ResponseEntity<ErrorResponse> noteNotUpdatedException(NoteNotUpdatedException exp) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.NOTE_UPDATE_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = CollaboratorNotDeletedException.class)
    public ResponseEntity<ErrorResponse> collaboratorNotDeletedException(CollaboratorNotDeletedException exp) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.COLLABORATOR_DELETE_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = CollaboratorDoesNotExistException.class)
    public ResponseEntity<ErrorResponse> collaboratorDoesNotExistException(CollaboratorDoesNotExistException exp) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ResponseErrorUtils.COLLABORATOR_DOES_NOT_EXIST_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = CollaboratorNotAddedException.class)
    public ResponseEntity<ErrorResponse> collaboratorNotAddedException(CollaboratorNotAddedException exp) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.COLLABORATOR_ADD_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = CollaboratorAlreadyExistException.class)
    public ResponseEntity<ErrorResponse> collaboratorAlreadyExistException(CollaboratorAlreadyExistException exp) {
        return buildErrorResponse(HttpStatus.CONFLICT, ResponseErrorUtils.COLLABORATOR_ALREADY_EXIST_ERROR,
                exp.getMessage());
    }

    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFoundException(UserNotFoundException exp) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, ResponseErrorUtils.USER_NOT_FOUND, exp.getMessage());
    }

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception exp) {
        log.error("An unexpected error occurred", exp);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ResponseErrorUtils.INTERNAL_SERVER_ERROR,
                exp.getMessage());
    }

}
