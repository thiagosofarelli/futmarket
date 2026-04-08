package com.futmarket.futmarket.orders;

import com.futmarket.futmarket.users.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByBuyerOrderByCreatedAtDesc(User buyer);

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Long buyerId);

    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);

    List<Order> findByPlayerIdOrderByCreatedAtDesc(Long playerId);
}
