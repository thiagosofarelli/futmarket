package com.ar.edu.unq.futmarket.services.impl;

import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import com.ar.edu.unq.futmarket.repositories.OrderRepository;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.repositories.QuoteRepository;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import com.ar.edu.unq.futmarket.services.BootstrapService;
import com.ar.edu.unq.futmarket.services.OrderService;
import com.ar.edu.unq.futmarket.services.QuoteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BootstrapServiceImpl implements BootstrapService {

    private static final String SUPERUSER_USERNAME = "SUPERUSER";
    private static final String SUPERUSER_PASSWORD = "superuser123";
    private static final String DEMO_PASSWORD = "demo1234";

    private final UserRepository userRepository;
    private final PlayerRepository playerRepository;
    private final QuoteRepository quoteRepository;
    private final OrderRepository orderRepository;
    private final QuoteService quoteService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public BootstrapResponse initializeDemoData() {
        boolean superuserCreated = ensureSuperuser();
        int usersCreated = ensureDemoUsers();
        int playersCreated = ensurePlayers();
        int quotesCreated = ensureQuotes(playersCreated > 0);
        int ordersCreated = ensureOrders();

        return new BootstrapResponse(superuserCreated, usersCreated, playersCreated, ordersCreated, quotesCreated);
    }

    private boolean ensureSuperuser() {
        if (userRepository.findBySuperuserTrue().isPresent()) {
            return false;
        }

        User superuser = User.builder()
                .username(SUPERUSER_USERNAME)
                .balance(BigDecimal.ZERO)
                .superuser(true)
                .build();
        superuser.setPassword(passwordEncoder.encode(SUPERUSER_PASSWORD));
        userRepository.save(superuser);
        return true;
    }

    private int ensureDemoUsers() {
        int created = 0;
        created += createUserIfMissing("alice", new BigDecimal("1000.00"));
        created += createUserIfMissing("bob", new BigDecimal("850.00"));
        created += createUserIfMissing("carla", new BigDecimal("1200.00"));
        return created;
    }

    private int createUserIfMissing(String username, BigDecimal balance) {
        if (userRepository.findByUsername(username).isPresent()) {
            return 0;
        }

        User user = User.builder()
                .username(username)
                .balance(balance)
                .superuser(false)
                .build();
        user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        userRepository.save(user);
        return 1;
    }

    private int ensurePlayers() {
        if (playerRepository.count() > 0) {
            return 0;
        }

        List<Player> players = List.of(
                Player.builder()
                        .name("Lionel Messi")
                        .team("Inter Miami")
                        .league("Major League Soccer")
                        .playerPosition(PlayerPosition.FORWARD)
                        .externalId(1001L)
                        .build(),
                Player.builder()
                        .name("Lautaro Martinez")
                        .team("Inter")
                        .league("Serie A")
                        .playerPosition(PlayerPosition.FORWARD)
                        .externalId(1002L)
                        .build(),
                Player.builder()
                        .name("Virgil van Dijk")
                        .team("Liverpool")
                        .league("Premier League")
                        .playerPosition(PlayerPosition.DEFENDER)
                        .externalId(1003L)
                        .build(),
                Player.builder()
                        .name("Thibaut Courtois")
                        .team("Real Madrid")
                        .league("La Liga")
                        .playerPosition(PlayerPosition.GOALKEEPER)
                        .externalId(1004L)
                        .build()
        );

        playerRepository.saveAll(players);
        return players.size();
    }

    private int ensureQuotes(boolean playersWereCreated) {
        if (!playersWereCreated && quoteRepository.count() > 0) {
            return 0;
        }

        List<Player> players = playerRepository.findAll();
        if (players.isEmpty()) {
            return 0;
        }

        long initialCount = quoteRepository.count();
        quoteService.recalculateAll(ValuationStrategy.GENERAL_PERFORMANCE);
        return (int) (quoteRepository.count() - initialCount);
    }

    private int ensureOrders() {
        if (userRepository.findByUsername("alice").isEmpty() || userRepository.findByUsername("bob").isEmpty()) {
            return 0;
        }
        if (orderRepository.count() > 0) {
            return 0;
        }

        List<Player> players = playerRepository.findAll();
        if (players.isEmpty()) {
            return 0;
        }

        User alice = userRepository.findByUsername("alice").orElseThrow();
        User bob = userRepository.findByUsername("bob").orElseThrow();

        int createdOrders = 0;
        createdOrders += createBuyOrder(alice, players.get(0), 5);
        if (players.size() > 1) {
            createdOrders += createBuyOrder(bob, players.get(1), 3);
        }
        if (players.size() > 2) {
            createdOrders += createBuyOrder(alice, players.get(2), 2);
        }
        return createdOrders;
    }

    private int createBuyOrder(User buyer, Player player, int quantity) {
        UserDetails buyerDetails = org.springframework.security.core.userdetails.User.builder()
                .username(buyer.getUsername())
                .password("")
                .roles("USER")
                .build();

        orderService.buy(buyerDetails, player.getId(), quantity);
        return 1;
    }
}
