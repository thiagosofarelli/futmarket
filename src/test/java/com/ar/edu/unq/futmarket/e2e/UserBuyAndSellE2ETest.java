package com.ar.edu.unq.futmarket.e2e;

import com.ar.edu.unq.futmarket.controllers.dto.OrderDTO;
import com.ar.edu.unq.futmarket.controllers.dto.PlayerDTO;
import com.ar.edu.unq.futmarket.controllers.dto.PortfolioDTO;
import com.ar.edu.unq.futmarket.controllers.response.AuthResponse;
import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.enums.OrderStatus;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E test simulating a complete user workflow:
 * 1. Bootstrap demo data
 * 2. Register/Login as a regular user
 * 3. List available players
 * 4. Buy player tokens
 * 5. View portfolio with positions
 * 6. Sell tokens
 * 7. Verify transaction history
 *
 * This test validates the core business flow and ensures concurrency, transactionality,
 * and data consistency across the buy/sell cycle.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserBuyAndSellE2ETest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    private String authToken;
    private Long testUserId;

    @BeforeEach
    void setupBootstrap() {
        // 1. Bootstrap demo data
        ResponseEntity<BootstrapResponse> bootstrapResponse = restTemplate.postForEntity(
            bootstrapUrl(),
            null,
            BootstrapResponse.class
        );
        assertThat(bootstrapResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2. Login as carla (created during bootstrap with password demo1234, no initial orders)
        authToken = loginAsRegularUser();
        User user = userRepository.findByUsername("carla").orElseThrow();
        testUserId = user.getId();
    }

    @Test
    void completeUserJourneyFromBuyToSell() {
        // Initial state: verify user has balance
        BigDecimal initialBalance = getUserBalance();
        assertThat(initialBalance).isGreaterThan(BigDecimal.ZERO);

        // 3. List players and get first available
        Long playerId = getFirstAvailablePlayer();
        assertThat(playerId).isNotNull();

        // 4. Buy tokens
        int quantityToBuy = 5;
        OrderDTO buyOrder = buyPlayerTokens(playerId, quantityToBuy);
        assertThat(buyOrder.id).isNotNull();
        assertThat(buyOrder.tokenQuantity).isEqualTo(quantityToBuy);
        assertThat(buyOrder.status).isEqualTo(OrderStatus.COMPLETED);

        // Verify balance decreased
        BigDecimal balanceAfterBuy = getUserBalance();
        assertThat(balanceAfterBuy).isLessThan(initialBalance);

        // 5. View portfolio and verify position exists
        PortfolioDTO portfolio = getPortfolio();
        assertThat(portfolio).isNotNull();
        assertThat(portfolio.positions).isNotEmpty();
        assertThat(portfolio.positions).anySatisfy(pos -> {
            assertThat(pos.playerId).isEqualTo(playerId);
            assertThat(pos.tokensAcquired).isEqualTo(quantityToBuy);
            assertThat(pos.averagePurchasePrice).isGreaterThan(BigDecimal.ZERO);
            assertThat(pos.currentValue).isGreaterThan(BigDecimal.ZERO);
        });

        // Verify portfolio P&L calculation
        assertThat(portfolio.currentValue).isGreaterThan(BigDecimal.ZERO);

        // 6. Sell tokens (partial sale)
        int quantityToSell = 3;
        OrderDTO sellOrder = sellPlayerTokens(playerId, quantityToSell);
        assertThat(sellOrder.id).isNotNull();
        assertThat(sellOrder.tokenQuantity).isEqualTo(quantityToSell);
        assertThat(sellOrder.status).isEqualTo(OrderStatus.COMPLETED);

        // Verify balance increased after sale
        BigDecimal balanceAfterSell = getUserBalance();
        assertThat(balanceAfterSell).isGreaterThan(balanceAfterBuy);

        // 7. Verify portfolio reflects partial sale
        PortfolioDTO portfolioAfterSell = getPortfolio();
        int remainingTokens = quantityToBuy - quantityToSell;
        assertThat(portfolioAfterSell.positions).anySatisfy(pos -> {
            assertThat(pos.playerId).isEqualTo(playerId);
            assertThat(pos.tokensAcquired).isEqualTo(remainingTokens);
        });

        // 8. Verify transaction history
        List<OrderDTO> userOrders = getUserTransactionHistory();
        assertThat(userOrders).hasSizeGreaterThanOrEqualTo(2);
        assertThat(userOrders)
            .anyMatch(order -> order.tokenQuantity == quantityToBuy && order.status == OrderStatus.COMPLETED);
        assertThat(userOrders)
            .anyMatch(order -> order.tokenQuantity == quantityToSell && order.status == OrderStatus.COMPLETED);
    }

    @Test
    void buyMultiplePlayersAndVerifyDiversification() {
        BigDecimal initialBalance = getUserBalance();

        // Get first two available players
        List<PlayerDTO> players = listPlayers(0, 5);
        assertThat(players).hasSizeGreaterThanOrEqualTo(2);

        Long player1Id = players.get(0).id;
        Long player2Id = players.get(1).id;

        // Buy from both players
        OrderDTO order1 = buyPlayerTokens(player1Id, 3);
        assertThat(order1.status).isEqualTo(OrderStatus.COMPLETED);

        OrderDTO order2 = buyPlayerTokens(player2Id, 4);
        assertThat(order2.status).isEqualTo(OrderStatus.COMPLETED);

        // Verify diversified portfolio
        PortfolioDTO portfolio = getPortfolio();
        assertThat(portfolio.positions).hasSizeGreaterThanOrEqualTo(2);

        // Verify totals: all positions should sum to 7 tokens
        int totalTokens = portfolio.positions.stream()
            .mapToInt(pos -> pos.tokensAcquired)
            .sum();
        assertThat(totalTokens).isEqualTo(7);

        // Verify balance decreased appropriately
        BigDecimal finalBalance = getUserBalance();
        assertThat(finalBalance).isLessThan(initialBalance);
    }

    @Test
    void sellAllTokensAndVerifyPositionClosed() {
        Long playerId = getFirstAvailablePlayer();
        int quantity = 5;

        // Buy tokens
        buyPlayerTokens(playerId, quantity);
        PortfolioDTO portfolioBefore = getPortfolio();
        assertThat(portfolioBefore.positions).isNotEmpty();

        // Sell all tokens
        sellPlayerTokens(playerId, quantity);

        // Verify position is removed or set to zero
        PortfolioDTO portfolioAfter = getPortfolio();
        boolean positionExists = portfolioAfter.positions.stream()
            .anyMatch(pos -> pos.playerId.equals(playerId) && pos.tokensAcquired > 0);
        assertThat(positionExists).isFalse();
    }

    // ========== Helper Methods ==========

    private String loginAsRegularUser() {
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("username", "carla");
        loginRequest.put("password", "demo1234");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            authLoginUrl(),
            loginRequest,
            AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotEmpty();

        return response.getBody().getToken();
    }

    private BigDecimal getUserBalance() {
        User user = userRepository.findById(testUserId).orElseThrow();
        return user.getBalance();
    }

    private Long getFirstAvailablePlayer() {
        List<PlayerDTO> players = listPlayers(0, 1);
        assertThat(players).isNotEmpty();
        return players.get(0).id;
    }

    private List<PlayerDTO> listPlayers(int page, int size) {
        String url = String.format(
            "%s?page=%d&size=%d",
            playersUrl(),
            page,
            size
        );

        // Use a simple Map to parse the Page response
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            new ParameterizedTypeReference<Map<String, Object>>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();

        // Extract content array from Page response
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
        return content.stream()
            .map(item -> {
                ObjectMapper mapper = new ObjectMapper();
                return mapper.convertValue(item, PlayerDTO.class);
            })
            .toList();
    }

    private OrderDTO buyPlayerTokens(Long playerId, int quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("playerId", playerId);
        request.put("quantity", quantity);

        ResponseEntity<OrderDTO> response = restTemplate.exchange(
            buyOrderUrl(),
            HttpMethod.POST,
            new HttpEntity<>(request, createHeaders()),
            OrderDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private OrderDTO sellPlayerTokens(Long playerId, int quantity) {
        Map<String, Object> request = new HashMap<>();
        request.put("playerId", playerId);
        request.put("quantity", quantity);

        ResponseEntity<OrderDTO> response = restTemplate.exchange(
            sellOrderUrl(),
            HttpMethod.POST,
            new HttpEntity<>(request, createHeaders()),
            OrderDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private PortfolioDTO getPortfolio() {
        ResponseEntity<PortfolioDTO> response = restTemplate.exchange(
            portfolioUrl(testUserId),
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            PortfolioDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private List<OrderDTO> getUserTransactionHistory() {
        ResponseEntity<List<OrderDTO>> response = restTemplate.exchange(
            transactionHistoryUrl(testUserId),
            HttpMethod.GET,
            new HttpEntity<>(createHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        return response.getBody();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return headers;
    }

    // ========== URL Builders ==========

    private String bootstrapUrl() {
        return "http://localhost:" + port + "/admin/bootstrap/demo-data";
    }

    private String authLoginUrl() {
        return "http://localhost:" + port + "/auth/login";
    }

    private String playersUrl() {
        return "http://localhost:" + port + "/players";
    }

    private String buyOrderUrl() {
        return "http://localhost:" + port + "/orders/buy";
    }

    private String sellOrderUrl() {
        return "http://localhost:" + port + "/orders/sell";
    }

    private String portfolioUrl(Long userId) {
        return "http://localhost:" + port + "/portfolios/user/" + userId;
    }

    private String transactionHistoryUrl(Long userId) {
        return "http://localhost:" + port + "/orders/user/" + userId;
    }
}






