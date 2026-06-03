package com.ar.edu.unq.futmarket.exception;

public class SuperuserDoesntHaveThatPositionException extends RuntimeException {
    public SuperuserDoesntHaveThatPositionException() {
        super("Superuser doesn't have that position.");
    }
}
