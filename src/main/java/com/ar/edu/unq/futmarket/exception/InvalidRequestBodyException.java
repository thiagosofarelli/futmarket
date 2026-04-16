package com.ar.edu.unq.futmarket.exception;

public class InvalidRequestBodyException extends RuntimeException {
    public InvalidRequestBodyException() {
        super("Invalid Request Body.");
    }
}
