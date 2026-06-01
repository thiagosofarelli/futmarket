package com.ar.edu.unq.futmarket.e2e;

import com.ar.edu.unq.futmarket.controllers.response.AuthResponse;
import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthE2ETest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void bootstrap() {
        restTemplate.postForEntity(bootstrapUrl(), null, BootstrapResponse.class);
    }

    @Test
    void register_returnsTokenAndCreatesUser() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "newuser");
        request.put("password", "password123");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            registerUrl(), request, AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();

        assertThat(userRepository.findByUsername("newuser")).isPresent();
    }

    @Test
    void register_withDuplicateUsername_returns4xx() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "carla");
        request.put("password", "cualquiercontraseña");

        assertThatThrownBy(() ->
            restTemplate.postForEntity(registerUrl(), request, AuthResponse.class)
        ).isInstanceOf(HttpClientErrorException.class)
         .satisfies(ex ->
             assertThat(((HttpClientErrorException) ex).getStatusCode().value()).isBetween(400, 499)
         );
    }

    @Test
    void login_withValidCredentials_returnsToken() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "carla");
        request.put("password", "demo1234");

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            loginUrl(), request, AuthResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getToken()).isNotBlank();
    }

    @Test
    void login_withWrongPassword_returns4xx() {
        Map<String, String> request = new HashMap<>();
        request.put("username", "carla");
        request.put("password", "wrongpassword");

        assertThatThrownBy(() ->
            restTemplate.postForEntity(loginUrl(), request, AuthResponse.class)
        ).isInstanceOf(HttpClientErrorException.class)
         .satisfies(ex ->
             assertThat(((HttpClientErrorException) ex).getStatusCode().value()).isBetween(400, 499)
         );
    }

    @Test
    void register_thenLogin_tokenIsUsable() {
        Map<String, String> registerReq = new HashMap<>();
        registerReq.put("username", "freshuser");
        registerReq.put("password", "securepass");

        ResponseEntity<AuthResponse> registerResponse = restTemplate.postForEntity(
            registerUrl(), registerReq, AuthResponse.class
        );
        assertThat(registerResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        String registerToken = registerResponse.getBody().getToken();
        assertThat(registerToken).isNotBlank();

        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("username", "freshuser");
        loginReq.put("password", "securepass");

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
            loginUrl(), loginReq, AuthResponse.class
        );
        assertThat(loginResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loginResponse.getBody().getToken()).isNotBlank();
    }

    private String bootstrapUrl() { return "http://localhost:" + port + "/admin/bootstrap/demo-data"; }
    private String registerUrl()  { return "http://localhost:" + port + "/auth/register"; }
    private String loginUrl()     { return "http://localhost:" + port + "/auth/login"; }
}
