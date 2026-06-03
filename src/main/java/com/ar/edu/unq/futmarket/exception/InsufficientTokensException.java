package com.ar.edu.unq.futmarket.exception;

public class InsufficientTokensException extends RuntimeException {
    public InsufficientTokensException() {
        super("Insufficient tokens.");
    }
}
