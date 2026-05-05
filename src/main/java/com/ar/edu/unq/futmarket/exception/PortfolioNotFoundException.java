package com.ar.edu.unq.futmarket.exception;

public class PortfolioNotFoundException extends RuntimeException {
    public PortfolioNotFoundException() {
        super("Portfolio not found.");
    }
}
