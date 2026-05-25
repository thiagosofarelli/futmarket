package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;
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
    void initializeDemoData_createsSeedData() {
        ResponseEntity<BootstrapResponse> response = restTemplate.postForEntity(
            bootstrapUrl(),
                null,
                BootstrapResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().superuserCreated()).isTrue();
        assertThat(response.getBody().usersCreated()).isGreaterThanOrEqualTo(3);
        assertThat(response.getBody().playersCreated()).isGreaterThanOrEqualTo(4);
        assertThat(response.getBody().ordersCreated()).isGreaterThanOrEqualTo(3);
        assertThat(response.getBody().quotesCreated()).isGreaterThanOrEqualTo(4);

        assertThat(userRepository.findBySuperuserTrue()).isPresent();
        assertThat(playerRepository.count()).isGreaterThan(0);
        assertThat(quoteRepository.count()).isGreaterThan(0);
        assertThat(orderRepository.count()).isGreaterThan(0);
    }

    @Test
    void initializeDemoData_isIdempotentOnSecondCall() {
        restTemplate.postForEntity(bootstrapUrl(), null, BootstrapResponse.class);
        ResponseEntity<BootstrapResponse> secondResponse = restTemplate.postForEntity(
            bootstrapUrl(),
                null,
                BootstrapResponse.class
        );

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondResponse.getBody()).isNotNull();
        assertThat(secondResponse.getBody().superuserCreated()).isFalse();
        assertThat(secondResponse.getBody().usersCreated()).isZero();
        assertThat(secondResponse.getBody().playersCreated()).isZero();
        assertThat(secondResponse.getBody().ordersCreated()).isZero();
        assertThat(secondResponse.getBody().quotesCreated()).isZero();
    }

    private String bootstrapUrl() {
        return "http://localhost:" + port + "/admin/bootstrap/demo-data";
    }
}
