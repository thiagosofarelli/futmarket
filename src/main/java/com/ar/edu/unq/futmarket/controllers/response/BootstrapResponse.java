package com.ar.edu.unq.futmarket.controllers.response;

public record BootstrapResponse(
        boolean superuserCreated,
        int usersCreated,
        int playersCreated,
        int ordersCreated,
        int quotesCreated
) {
}
