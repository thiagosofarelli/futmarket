package com.ar.edu.unq.futmarket.exception;

public class PositionNotFoundException extends RuntimeException {
    public PositionNotFoundException() {
        super("The portfolio does not have that position.");
    }
}
