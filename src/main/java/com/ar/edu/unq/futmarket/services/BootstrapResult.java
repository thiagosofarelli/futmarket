package com.ar.edu.unq.futmarket.services;

public record BootstrapResult(
        boolean superuserCreated,
        int usersCreated,
        int playersCreated,
        int ordersCreated,
        int quotesCreated
) {}
