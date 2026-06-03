package com.ar.edu.unq.futmarket.services.impl;

import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.enums.OrderStatus;
import com.ar.edu.unq.futmarket.model.enums.OrderType;
import com.ar.edu.unq.futmarket.repositories.OrderRepository;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import com.ar.edu.unq.futmarket.services.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserServiceImpl userService;
    private final PlayerServiceImpl playerService;
    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;

    @Transactional
    public Order buy(UserDetails buyerDetails, Long playerId, int quantity) {
        User buyer = userService.findByUsername(buyerDetails.getUsername());
        User superuser = userService.findSuperuser();
        Player player = playerService.findById(playerId);

        BigDecimal pricePerToken = player.getCurrentTokenPrice();

        buyer.getPortfolio().registerPurchase(player, quantity, superuser);
        userRepository.save(buyer);
        userRepository.save(superuser);
        playerRepository.save(player);

        return orderRepository.save(buildOrder(buyer, superuser, player, OrderType.BUY, quantity, pricePerToken));
    }

    @Transactional
    public Order sell(UserDetails sellerDetails, Long playerId, int quantity) {
        User seller = userService.findByUsername(sellerDetails.getUsername());
        User superuser = userService.findSuperuser();
        Player player = playerService.findById(playerId);

        BigDecimal pricePerToken = player.getCurrentTokenPrice();

        seller.getPortfolio().registerSell(player, quantity, superuser);
        userRepository.save(seller);
        userRepository.save(superuser);
        playerRepository.save(player);

        return orderRepository.save(buildOrder(superuser, seller, player, OrderType.SELL, quantity, pricePerToken));
    }

    public List<Order> getTransactionsByUserId(Long userId) {
        List<Order> transactions = new ArrayList<>();
        transactions.addAll(orderRepository.findByBuyerIdOrderByCreatedAtDesc(userId));
        transactions.addAll(orderRepository.findBySellerIdOrderByCreatedAtDesc(userId));
        transactions.sort(Comparator.comparing(Order::getCreatedAt).reversed());
        return transactions;
    }

    private Order buildOrder(User buyer, User seller, Player player,
                             OrderType type, int quantity, BigDecimal pricePerToken) {
        Order order = new Order();
        order.setBuyer(buyer);
        order.setSeller(seller);
        order.setPlayer(player);
        order.setType(type);
        order.setStatus(OrderStatus.COMPLETED);
        order.setTokenQuantity(quantity);
        order.setPricePerToken(pricePerToken);
        order.setTotalAmount(pricePerToken.multiply(BigDecimal.valueOf(quantity)));
        order.setCompletedAt(LocalDateTime.now());
        return order;
    }
}
