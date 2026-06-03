package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Order;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface OrderService {

    Order buy(UserDetails buyerDetails, Long playerId, int quantity);

    Order sell(UserDetails sellerDetails, Long playerId, int quantity);

    List<Order> getTransactionsByUserId(Long userId);

}
