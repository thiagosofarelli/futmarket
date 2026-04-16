package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.InvalidClassException;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleUserNotFound(UserNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<ApiError> handlePlayerNotFound(PlayerNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(PortfolioNotFoundException.class)
    public ResponseEntity<ApiError> handlePortfolioNotFound(PortfolioNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ApiError> handleOrderNotFound(OrderNotFoundException ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidClassException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SuperUserNotFoundException.class)
    public ResponseEntity<ApiError> handleSuperUserNotFound(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleUsernameAlreadyExists(Exception ex) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleMissingRequestBody() {
        return buildErrorResponse(new InvalidRequestBodyException(), HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<ApiError> buildErrorResponse(Exception ex, HttpStatus status) {
        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage()
        );
        return new ResponseEntity<>(error, status);
    }
}