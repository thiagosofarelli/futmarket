package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.*;
import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Position;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.enums.OrderStatus;
import com.ar.edu.unq.futmarket.model.enums.OrderType;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import com.ar.edu.unq.futmarket.services.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class OrderServiceImplTest {

    @Autowired
    private OrderServiceImpl orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private User alice;
    private User superuser;
    private Player messi;

    @BeforeEach
    void setUp() {
        User superuserBuilder = User.builder().username("SUPERUSER").balance(BigDecimal.ZERO).superuser(true).build();
        superuserBuilder.setPassword("password");
        superuser = userRepository.save(superuserBuilder);

        User aliceBuilder = User.builder().username("alice").balance(new BigDecimal("1000.00")).superuser(false).build();
        aliceBuilder.setPassword("password");
        alice = userRepository.save(aliceBuilder);
        messi = playerRepository.save(Player.builder().name("Messi").team("Team A").league("League A").playerPosition(PlayerPosition.FORWARD).currentTokenPrice(new BigDecimal("10.00")).build());

        Position superuserPosition = new Position();
        superuserPosition.setPortfolio(superuser.getPortfolio());
        superuserPosition.setPlayer(messi);
        superuserPosition.setTokensAcquired(100);
        superuser.getPortfolio().getPositions().add(superuserPosition);
        userRepository.saveAndFlush(superuser);
    }

    @Test
    void buy_returnsCompletedOrder() {
        Order order = orderService.buy(userDetailsOf(alice), messi.getId(), 5);

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getType()).isEqualTo(OrderType.BUY);
        assertThat(order.getTokenQuantity()).isEqualTo(5);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("50.0000");
        assertThat(order.getCompletedAt()).isNotNull();
    }

    @Test
    void buy_setsCorrectBuyerAndSeller() {
        Order order = orderService.buy(userDetailsOf(alice), messi.getId(), 3);

        assertThat(order.getBuyer().getUsername()).isEqualTo("alice");
        assertThat(order.getSeller().isSuperuser()).isTrue();
    }

    @Test
    void buy_deductsBuyerBalance() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);

        User updated = userRepository.findById(alice.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo("950.00");
    }


    @Test
    void buy_createsPositionInBuyerPortfolio() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);

        User updated = userRepository.findById(alice.getId()).orElseThrow();
        assertThat(updated.getPortfolio().getPosition(messi)).isPresent();
    }

    @Test
    void buy_playerNotFound_throwsPlayerNotFoundException() {
        UserDetails aliceDetails = userDetailsOf(alice);
        assertThatThrownBy(() -> orderService.buy(aliceDetails, -1L, 5))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void buy_userNotFound_throwsUserNotFoundException() {
        UserDetails unknownDetails = unknownUserDetails();
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.buy(unknownDetails, messiId, 5))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void buy_notEnoughTokens_throws() {
        UserDetails aliceDetails = userDetailsOf(alice);
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.buy(aliceDetails, messiId, 101))
                .isInstanceOf(InvalidBalanceException.class);
    }

    @Test
    void buy_notEnoughBalance_throws() {
        messi.setCurrentTokenPrice(new BigDecimal("300.00"));
        playerRepository.save(messi);

        UserDetails aliceDetails = userDetailsOf(alice);
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.buy(aliceDetails, messiId, 4))
                .isInstanceOf(InvalidBalanceException.class);
    }

    @Test
    void sell_returnsCompletedOrder() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);
        Order order = orderService.sell(userDetailsOf(alice), messi.getId(), 3);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(order.getType()).isEqualTo(OrderType.SELL);
        assertThat(order.getTokenQuantity()).isEqualTo(3);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("30.0000");
    }

    @Test
    void sell_setsCorrectBuyerAndSeller() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);
        Order order = orderService.sell(userDetailsOf(alice), messi.getId(), 3);

        assertThat(order.getSeller().getUsername()).isEqualTo("alice");
        assertThat(order.getBuyer().isSuperuser()).isTrue();
    }

    @Test
    void sell_creditsSellerBalance() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);
        orderService.sell(userDetailsOf(alice), messi.getId(), 3);

        User updated = userRepository.findById(alice.getId()).orElseThrow();
        assertThat(updated.getBalance()).isEqualByComparingTo("980.00");
    }

    @Test
    void sell_noPosition_throws() {
        UserDetails aliceDetails = userDetailsOf(alice);
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.sell(aliceDetails, messiId, 3))
                .isInstanceOf(PositionNotFoundException.class);
    }

    @Test
    void sell_moreThanHeld_throws() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);

        UserDetails aliceDetails = userDetailsOf(alice);
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.sell(aliceDetails, messiId, 10))
                .isInstanceOf(InsufficientTokensException.class);
    }

    @Test
    void getTransactionsByUserId_combinesBuyAndSellOrders() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);
        orderService.sell(userDetailsOf(alice), messi.getId(), 2);

        List<Order> transactions = orderService.getTransactionsByUserId(alice.getId());
        assertThat(transactions).hasSize(2);
    }

    @Test
    void getTransactionsByUserId_returnsSortedByCreatedAtDesc() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);
        orderService.sell(userDetailsOf(alice), messi.getId(), 2);

        List<Order> transactions = orderService.getTransactionsByUserId(alice.getId());
        assertThat(transactions.get(0).getCreatedAt())
                .isAfterOrEqualTo(transactions.get(1).getCreatedAt());
    }

    @Test
    void getTransactionsByUserId_noOrders_returnsEmpty() {
        assertThat(orderService.getTransactionsByUserId(alice.getId())).isEmpty();
    }

    private UserDetails userDetailsOf(User user) {
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password("")
                .roles("USER")
                .build();
    }

    private UserDetails unknownUserDetails() {
        return org.springframework.security.core.userdetails.User.builder()
                .username("unknown-user")
                .password("")
                .roles("USER")
                .build();
    }
}
