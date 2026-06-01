package com.ar.edu.unq.futmarket.e2e;

import com.ar.edu.unq.futmarket.controllers.dto.OrderDTO;
import com.ar.edu.unq.futmarket.controllers.dto.PlayerDTO;
import com.ar.edu.unq.futmarket.controllers.dto.PortfolioDTO;
import com.ar.edu.unq.futmarket.controllers.dto.UserDTO;
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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserProfileE2ETest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private UserRepository userRepository;

    private String authToken;
    private Long testUserId;

    @BeforeEach
    void setup() {
        restTemplate.postForEntity(bootstrapUrl(), null, BootstrapResponse.class);

        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("username", "carla");
        loginReq.put("password", "demo1234");
        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity(loginUrl(), loginReq, AuthResponse.class);
        authToken = loginResp.getBody().getToken();

        User user = userRepository.findByUsername("carla").orElseThrow();
        testUserId = user.getId();
    }

    @Test
    void getUser_returnsCorrectUserData() {
        ResponseEntity<UserDTO> response = restTemplate.exchange(
            userUrl(testUserId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            UserDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserDTO user = response.getBody();
        assertThat(user).isNotNull();
        assertThat(user.id).isEqualTo(testUserId);
        assertThat(user.username).isEqualTo("carla");
        assertThat(user.balance).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(user.superuser).isFalse();
    }

    @Test
    void getUserPortfolio_returnsPortfolioViaUserEndpoint() {
        ResponseEntity<PortfolioDTO> response = restTemplate.exchange(
            userPortfolioUrl(testUserId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            PortfolioDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PortfolioDTO portfolio = response.getBody();
        assertThat(portfolio).isNotNull();
        assertThat(portfolio.currentValue).isGreaterThanOrEqualTo(BigDecimal.ZERO);
    }

    @Test
    void getUserTransactions_returnsTransactionList() {
        ResponseEntity<List<OrderDTO>> response = restTemplate.exchange(
            userTransactionsUrl(testUserId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void afterBuy_userPortfolioReflectsNewPosition() {
        Long playerId = getFirstAvailablePlayerId();

        // Buy tokens
        Map<String, Object> buyReq = new HashMap<>();
        buyReq.put("playerId", playerId);
        buyReq.put("quantity", 3);
        restTemplate.exchange(buyUrl(), HttpMethod.POST,
            new HttpEntity<>(buyReq, authHeaders()), OrderDTO.class);

        // Verify via /users/{id}/portfolio — returns Portfolio entity (player is nested object)
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            userPortfolioUrl(testUserId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> portfolio = response.getBody();
        assertThat(portfolio).isNotNull();
        List<Map<String, Object>> positions = (List<Map<String, Object>>) portfolio.get("positions");
        assertThat(positions).isNotEmpty();
        boolean hasExpectedPlayer = positions.stream().anyMatch(pos -> {
            Map<String, Object> player = (Map<String, Object>) pos.get("player");
            return player != null && playerId.equals(((Number) player.get("id")).longValue());
        });
        assertThat(hasExpectedPlayer).isTrue();
    }

    @Test
    void afterBuy_userTransactionsContainOrder() {
        Long playerId = getFirstAvailablePlayerId();
        int quantity = 2;

        Map<String, Object> buyReq = new HashMap<>();
        buyReq.put("playerId", playerId);
        buyReq.put("quantity", quantity);
        restTemplate.exchange(buyUrl(), HttpMethod.POST,
            new HttpEntity<>(buyReq, authHeaders()), OrderDTO.class);

        ResponseEntity<List<OrderDTO>> response = restTemplate.exchange(
            userTransactionsUrl(testUserId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        List<OrderDTO> transactions = response.getBody();
        assertThat(transactions).isNotEmpty();
        assertThat(transactions).anySatisfy(order -> {
            assertThat(order.tokenQuantity).isEqualTo(quantity);
            assertThat(order.status).isEqualTo(OrderStatus.COMPLETED);
        });
    }

    @Test
    void getUserBalance_decreasesAfterBuy() {
        UserDTO userBefore = restTemplate.exchange(
            userUrl(testUserId), HttpMethod.GET, new HttpEntity<>(authHeaders()), UserDTO.class
        ).getBody();
        BigDecimal balanceBefore = userBefore.balance;

        Long playerId = getFirstAvailablePlayerId();
        Map<String, Object> buyReq = new HashMap<>();
        buyReq.put("playerId", playerId);
        buyReq.put("quantity", 1);
        restTemplate.exchange(buyUrl(), HttpMethod.POST, new HttpEntity<>(buyReq, authHeaders()), OrderDTO.class);

        UserDTO userAfter = restTemplate.exchange(
            userUrl(testUserId), HttpMethod.GET, new HttpEntity<>(authHeaders()), UserDTO.class
        ).getBody();

        assertThat(userAfter.balance).isLessThan(balanceBefore);
    }

    // ========== Helpers ==========

    private Long getFirstAvailablePlayerId() {
        String url = playersUrl() + "?page=0&size=1";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
        PlayerDTO player = objectMapper.convertValue(content.get(0), PlayerDTO.class);
        return player.id;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return headers;
    }

    private String bootstrapUrl()                  { return "http://localhost:" + port + "/admin/bootstrap/demo-data"; }
    private String loginUrl()                      { return "http://localhost:" + port + "/auth/login"; }
    private String userUrl(Long id)                { return "http://localhost:" + port + "/users/" + id; }
    private String userPortfolioUrl(Long id)       { return "http://localhost:" + port + "/users/" + id + "/portfolio"; }
    private String userTransactionsUrl(Long id)    { return "http://localhost:" + port + "/users/" + id + "/transactions"; }
    private String playersUrl()                    { return "http://localhost:" + port + "/players"; }
    private String buyUrl()                        { return "http://localhost:" + port + "/orders/buy"; }
}
