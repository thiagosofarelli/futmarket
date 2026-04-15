package com.ar.edu.unq.futmarket.exception;

public class SuperUserNotFoundException extends RuntimeException {
    public SuperUserNotFoundException() {
        super("SuperUser not found.");
    }
}
