package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.OrderStatus;
import com.ar.edu.unq.futmarket.model.OrderType;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Position;
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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private User alice;
    private User superuser;
    private Player messi;
    private Player ramos;

    @BeforeEach
    void setUp() {
        alice = user("alice");
        superuser = user("SUPERUSER");
        superuser.setSuperuser(true);
        userRepository.saveAll(List.of(alice, superuser));

        messi = player("Messi", Position.FORWARD);
        ramos = player("Ramos", Position.DEFENDER);
        playerRepository.saveAll(List.of(messi, ramos));
    }

    @Test
    void save_persistsOrder_withAutoTimestamp() {
        Order order = buyOrder(alice, superuser, messi, 5, new BigDecimal("2.0000"));
        orderRepository.save(order);

        Order found = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getTotalAmount()).isEqualByComparingTo("10.0000");
    }

    @Test
    void findByBuyer_returnsOrdersDesc() {
        Order o1 = buyOrder(alice, superuser, messi, 3, new BigDecimal("2.0000"));
        o1.setCreatedAt(LocalDateTime.now().minusDays(2));

        Order o2 = buyOrder(alice, superuser, ramos, 1, new BigDecimal("1.5000"));
        o2.setCreatedAt(LocalDateTime.now().minusDays(1));

        orderRepository.saveAll(List.of(o1, o2));

        List<Order> result = orderRepository.findByBuyerOrderByCreatedAtDesc(alice);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPlayer().getName()).isEqualTo("Ramos");
    }

    @Test
    void findByBuyerId_returnsOnlyAliceOrders() {
        orderRepository.save(buyOrder(alice, superuser, messi, 2, new BigDecimal("3.0000")));
        orderRepository.save(buyOrder(superuser, alice, ramos, 1, new BigDecimal("1.0000")));

        List<Order> result = orderRepository.findByBuyerIdOrderByCreatedAtDesc(alice.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBuyer().getUsername()).isEqualTo("alice");
    }

    @Test
    void findBySellerId_returnsOnlyAliceAsSeller() {
        orderRepository.save(buyOrder(superuser, alice, messi, 5, new BigDecimal("2.0000")));
        orderRepository.save(buyOrder(alice, superuser, ramos, 2, new BigDecimal("1.0000")));

        List<Order> result = orderRepository.findBySellerIdOrderByCreatedAtDesc(alice.getId());
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSeller().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByPlayerId_returnsAllOrdersForPlayer() {
        orderRepository.save(buyOrder(alice, superuser, messi, 3, new BigDecimal("2.0000")));
        orderRepository.save(buyOrder(alice, superuser, messi, 2, new BigDecimal("2.5000")));
        orderRepository.save(buyOrder(alice, superuser, ramos, 1, new BigDecimal("1.0000")));

        List<Order> result = orderRepository.findByPlayerIdOrderByCreatedAtDesc(messi.getId());
        assertThat(result).hasSize(2);
        result.forEach(o -> assertThat(o.getPlayer().getName()).isEqualTo("Messi"));
    }

    @Test
    void findByBuyerId_noOrders_returnsEmpty() {
        assertThat(orderRepository.findByBuyerIdOrderByCreatedAtDesc(alice.getId())).isEmpty();
    }

    @Test
    void order_totalAmount_isCorrect() {
        Order order = buyOrder(alice, superuser, messi, 7, new BigDecimal("3.5000"));
        orderRepository.save(order);

        Order found = orderRepository.findById(order.getId()).orElseThrow();
        assertThat(found.getTotalAmount()).isEqualByComparingTo("24.5000");
    }

    // --- helpers ---

    private User user(String username) {
        User u = new User();
        u.setUsername(username);
        u.setBalance(BigDecimal.ZERO);
        return u;
    }

    private Player player(String name, Position position) {
        Player p = new Player();
        p.setName(name);
        p.setTeam("Team A");
        p.setLeague("League A");
        p.setPosition(position);
        return p;
    }

    private Order buyOrder(User buyer, User seller, Player player, int quantity, BigDecimal pricePerToken) {
        Order o = new Order();
        o.setBuyer(buyer);
        o.setSeller(seller);
        o.setPlayer(player);
        o.setType(OrderType.BUY);
        o.setTokenQuantity(quantity);
        o.setPricePerToken(pricePerToken);
        o.setTotalAmount(pricePerToken.multiply(BigDecimal.valueOf(quantity)));
        return o;
    }
}

