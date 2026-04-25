package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.PlayerNotFoundException;
import com.ar.edu.unq.futmarket.exception.UserNotFoundException;
import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.enums.OrderStatus;
import com.ar.edu.unq.futmarket.model.enums.OrderType;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
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
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private User alice;
    private User superuser;
    private Player messi;

    @BeforeEach
    void setUp() {
        superuser = userRepository.save(new User("SUPERUSER", BigDecimal.ZERO, true));
        alice     = userRepository.save(new User("alice", new BigDecimal("1000.00"), false));
        messi     = playerRepository.save(player("Messi", PlayerPosition.FORWARD, "10.00"));
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
    void buy_decrementsPlayerAvailableTokens() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);

        Player updated = playerRepository.findById(messi.getId()).orElseThrow();
        assertThat(updated.getAvailableTokens()).isEqualTo(95);
    }

    @Test
    void buy_createsPositionInBuyerPortfolio() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);

        User updated = userRepository.findById(alice.getId()).orElseThrow();
        assertThat(updated.getPortfolio().getPosition(messi)).isPresent();
        assertThat(updated.getPortfolio().getPosition(messi).get().getTokensAcquired()).isEqualTo(5);
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
    void buy_notEnoughTokens_throwsIllegalArgumentException() {
        messi.setAvailableTokens(3);
        playerRepository.save(messi);

        UserDetails aliceDetails = userDetailsOf(alice);
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.buy(aliceDetails, messiId, 5))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void buy_notEnoughBalance_throwsIllegalArgumentException() {
        messi.setCurrentTokenPrice(new BigDecimal("300.00"));
        playerRepository.save(messi);

        UserDetails aliceDetails = userDetailsOf(alice);
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.buy(aliceDetails, messiId, 4))
                .isInstanceOf(IllegalArgumentException.class);
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
    void sell_restoresPlayerAvailableTokens() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);
        orderService.sell(userDetailsOf(alice), messi.getId(), 3);

        Player updated = playerRepository.findById(messi.getId()).orElseThrow();
        assertThat(updated.getAvailableTokens()).isEqualTo(98);
    }

    @Test
    void sell_noPosition_throwsIllegalArgumentException() {
        UserDetails aliceDetails = userDetailsOf(alice);
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.sell(aliceDetails, messiId, 3))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sell_moreThanHeld_throwsIllegalArgumentException() {
        orderService.buy(userDetailsOf(alice), messi.getId(), 5);

        UserDetails aliceDetails = userDetailsOf(alice);
        Long messiId = messi.getId();
        assertThatThrownBy(() -> orderService.sell(aliceDetails, messiId, 10))
                .isInstanceOf(IllegalArgumentException.class);
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

    private Player player(String name, PlayerPosition position, String price) {
        Player p = new Player(name, "Team A", "League A", position);
        p.setCurrentTokenPrice(new BigDecimal(price));
        return p;
    }
}
