package com.ar.edu.unq.futmarket.e2e;

import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;
import com.ar.edu.unq.futmarket.model.Order;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.enums.OrderStatus;
import com.ar.edu.unq.futmarket.repositories.OrderRepository;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.repositories.QuoteRepository;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BootstrapControllerE2ETest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void initializeDemoData_createsSeedDataWithCorrectStructure() {
        ResponseEntity<BootstrapResponse> response = restTemplate.postForEntity(
            bootstrapUrl(),
            null,
            BootstrapResponse.class
        );

        // Validar respuesta HTTP
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        BootstrapResponse body = response.getBody();
        assertThat(body.superuserCreated()).isTrue();
        assertThat(body.usersCreated()).isGreaterThanOrEqualTo(3);
        assertThat(body.playersCreated()).isGreaterThanOrEqualTo(4);
        assertThat(body.ordersCreated()).isGreaterThanOrEqualTo(3);
        assertThat(body.quotesCreated()).isGreaterThanOrEqualTo(4);

        // Validar existencia de superusuario y sus propiedades
        Optional<User> superuser = userRepository.findBySuperuserTrue();
        assertThat(superuser).isPresent();
        assertThat(superuser.get().isSuperuser()).isTrue();
        // The bootstrap sets superuser balance to zero (holds tokens initially), so assert >= 0
        assertThat(superuser.get().getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(superuser.get().getPortfolio()).isNotNull();

        // Validar que existen suficientes jugadores
        List<Player> players = playerRepository.findAll();
        assertThat(players).hasSizeGreaterThanOrEqualTo(4);
        players.forEach(player -> {
            assertThat(player.getCurrentTokenPrice()).isGreaterThan(BigDecimal.ZERO);
            assertThat(player.getPlayerPosition()).isNotNull();
        });

        // Validar que existen quotes
        assertThat(quoteRepository.count()).isGreaterThan(0);
        quoteRepository.findAll().forEach(quote -> {
            assertThat(quote.getPlayer()).isNotNull();
            assertThat(quote.getScore()).isGreaterThanOrEqualTo(0);
            assertThat(quote.getCurrentTokenPrice()).isGreaterThan(BigDecimal.ZERO);
            assertThat(quote.getCalculatedAt()).isNotNull();
        });

        // Validar órdenes completadas
        List<Order> orders = orderRepository.findAll();
        assertThat(orders).hasSizeGreaterThanOrEqualTo(3);
        orders.forEach(order -> {
            assertThat(order.getBuyer()).isNotNull();
            assertThat(order.getSeller()).isNotNull();
            assertThat(order.getPlayer()).isNotNull();
            assertThat(order.getStatus()).isIn(OrderStatus.COMPLETED, OrderStatus.PENDING);
            assertThat(order.getTokenQuantity()).isGreaterThan(0);
            assertThat(order.getPricePerToken()).isGreaterThan(BigDecimal.ZERO);
            assertThat(order.getTotalAmount()).isGreaterThan(BigDecimal.ZERO);
            assertThat(order.getCreatedAt()).isNotNull();
        });

        // Validar que usuarios no-superusuario existen y tienen portafolios
        List<User> allUsers = userRepository.findAll();
        allUsers.stream()
            .filter(u -> !u.isSuperuser())
            .forEach(user -> {
                assertThat(user.getPortfolio()).isNotNull();
                assertThat(user.getBalance()).isGreaterThanOrEqualTo(BigDecimal.ZERO);
            });
    }

    @Test
    void initializeDemoData_isIdempotentOnSecondCall() {
        // Primera ejecución
        ResponseEntity<BootstrapResponse> firstResponse = restTemplate.postForEntity(
            bootstrapUrl(),
            null,
            BootstrapResponse.class
        );
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        long firstCountUsers = userRepository.count();
        long firstCountPlayers = playerRepository.count();
        long firstCountQuotes = quoteRepository.count();
        long firstCountOrders = orderRepository.count();

        // Segunda ejecución
        ResponseEntity<BootstrapResponse> secondResponse = restTemplate.postForEntity(
            bootstrapUrl(),
            null,
            BootstrapResponse.class
        );

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BootstrapResponse secondBody = secondResponse.getBody();
        assertThat(secondBody).isNotNull();
        assertThat(secondBody.superuserCreated()).isFalse();
        assertThat(secondBody.usersCreated()).isZero();
        assertThat(secondBody.playersCreated()).isZero();
        assertThat(secondBody.ordersCreated()).isZero();
        assertThat(secondBody.quotesCreated()).isZero();

        // Verificar que los conteos no cambiaron
        assertThat(userRepository.count()).isEqualTo(firstCountUsers);
        assertThat(playerRepository.count()).isEqualTo(firstCountPlayers);
        assertThat(quoteRepository.count()).isEqualTo(firstCountQuotes);
        assertThat(orderRepository.count()).isEqualTo(firstCountOrders);
    }

    private String bootstrapUrl() {
        return "http://localhost:" + port + "/admin/bootstrap/demo-data";
    }
}
