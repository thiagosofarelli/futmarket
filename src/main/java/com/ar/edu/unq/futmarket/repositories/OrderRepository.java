package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Order> findByPlayerIdOrderByCreatedAtDesc(Long playerId);
}

