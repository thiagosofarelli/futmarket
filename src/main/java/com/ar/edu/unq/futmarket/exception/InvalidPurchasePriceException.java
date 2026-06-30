package com.ar.edu.unq.futmarket.exception;

public class InvalidPurchasePriceException extends RuntimeException {
    public InvalidPurchasePriceException() {
        super("Purchase price is invalid. It must be a positive value.");
    }
}
