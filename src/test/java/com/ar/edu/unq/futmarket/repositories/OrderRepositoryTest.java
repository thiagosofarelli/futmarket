package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.OrderStatus;
import com.ar.edu.unq.futmarket.model.enums.OrderType;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class OrderRepositoryTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PlayerRepository playerRepository;

    private User alice;
    private User superuser;
    private Player messi;
    private Player ramos;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(User.builder()
                .username("alice")
                .balance(new BigDecimal("1000.00"))
                .build());

        superuser = userRepository.save(User.builder()
                .username("SUPERUSER")
                .balance(BigDecimal.ZERO)
                .superuser(true)
                .build());

        messi = playerRepository.save(Player.builder()
                .name("Messi")
                .team("PSG")
                .playerPosition(PlayerPosition.FORWARD)
                .league(League.PL.name())
                .build());

        ramos = playerRepository.save(Player.builder()
                .name("Ramos")
                .team("PSG")
                .playerPosition(PlayerPosition.DEFENDER)
                .league(League.PL.name())
                .build());
    }

    @Test
    void save_persistsOrder_withAutoTimestamp() {
        Order order = orderRepository.save(Order.builder()
                .buyer(alice)
                .seller(superuser)
                .player(messi)
                .pricePerToken(new BigDecimal("2.0000"))
                .type(OrderType.BUY)
                .tokenQuantity(5)
                .totalAmount(new BigDecimal("10.0000"))
                .build());

        Order found = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.COMPLETED);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getTotalAmount()).isEqualByComparingTo("10.0000");
    }

    @Test
    void findByBuyer_returnsOrdersDesc() {
        Order o1 = Order.builder()
                .buyer(alice).seller(superuser).player(messi)
                .pricePerToken(new BigDecimal("2.00")).type(OrderType.BUY)
                .tokenQuantity(3).totalAmount(new BigDecimal("6.00"))
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        Order o2 = Order.builder()
                .buyer(alice).seller(superuser).player(ramos)
                .pricePerToken(new BigDecimal("1.50")).type(OrderType.BUY)
                .tokenQuantity(1).totalAmount(new BigDecimal("1.50"))
                .createdAt(LocalDateTime.now())
                .build();

        orderRepository.save(o1);
        orderRepository.save(o2);

        List<Order> result = orderRepository.findByBuyerOrderByCreatedAtDesc(alice);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlayer().getName()).isEqualTo("Ramos");
    }

    @Test
    void findByBuyerId_returnsOnlyAliceOrders() {
        orderRepository.save(Order.builder()
                .buyer(alice).seller(superuser).player(messi)
                .pricePerToken(new BigDecimal("3.00")).type(OrderType.BUY)
                .tokenQuantity(2).totalAmount(new BigDecimal("6.00")).build());

        List<Order> result = orderRepository.findByBuyerIdOrderByCreatedAtDesc(alice.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBuyer().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByPlayerId_returnsAllOrdersForPlayer() {
        // Dos órdenes para Ramos
        orderRepository.save(Order.builder()
                .buyer(alice).seller(superuser).player(ramos)
                .pricePerToken(new BigDecimal("1.50")).type(OrderType.BUY)
                .tokenQuantity(1).totalAmount(new BigDecimal("1.50")).build());

        orderRepository.save(Order.builder()
                .buyer(superuser).seller(alice).player(ramos)
                .pricePerToken(new BigDecimal("1.00")).type(OrderType.BUY)
                .tokenQuantity(1).totalAmount(new BigDecimal("1.00")).build());

        List<Order> result = orderRepository.findByPlayerIdOrderByCreatedAtDesc(ramos.getId());
        assertThat(result).hasSize(2);
        result.forEach(o -> assertThat(o.getPlayer().getName()).isEqualTo("Ramos"));
    }

    @Test
    void order_totalAmount_isCorrect() {
        Order order = orderRepository.save(Order.builder()
                .buyer(alice)
                .seller(superuser)
                .player(messi)
                .type(OrderType.BUY)
                .pricePerToken(new BigDecimal("3.5000"))
                .tokenQuantity(7)
                .totalAmount(new BigDecimal("24.5000"))
                .build());

        Order found = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(found.getTotalAmount()).isEqualByComparingTo("24.5000");
    }
}

