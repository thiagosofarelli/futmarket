package com.ar.edu.unq.futmarket.e2e;

import com.ar.edu.unq.futmarket.controllers.dto.PlayerDTO;
import com.ar.edu.unq.futmarket.controllers.dto.QuoteDTO;
import com.ar.edu.unq.futmarket.controllers.response.AuthResponse;
import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;
import com.ar.edu.unq.futmarket.model.enums.ValuationStrategy;
import com.ar.edu.unq.futmarket.repositories.QuoteRepository;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("e2e")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PlayerAndQuoteE2ETest {

    @LocalServerPort
    private int port;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private QuoteRepository quoteRepository;

    private String authToken;
    private Long firstPlayerId;

    @BeforeEach
    void setup() {
        restTemplate.postForEntity(bootstrapUrl(), null, BootstrapResponse.class);

        Map<String, String> loginReq = new HashMap<>();
        loginReq.put("username", "carla");
        loginReq.put("password", "demo1234");
        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity(loginUrl(), loginReq, AuthResponse.class);
        authToken = loginResp.getBody().getToken();

        List<PlayerDTO> players = listPlayers(0, 1);
        firstPlayerId = players.get(0).id;
    }

    @Test
    void getPlayer_returnsCorrectPlayer() {
        ResponseEntity<PlayerDTO> response = restTemplate.exchange(
            playerUrl(firstPlayerId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            PlayerDTO.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        PlayerDTO player = response.getBody();
        assertThat(player).isNotNull();
        assertThat(player.id).isEqualTo(firstPlayerId);
        assertThat(player.name).isNotBlank();
        assertThat(player.playerPosition).isNotNull();
        assertThat(player.currentTokenPrice).isNotNull().isPositive();
        assertThat(player.availableTokens).isBetween(0, 100);
    }

    @Test
    void getPlayers_withPositionFilter_returnsOnlyMatchingPlayers() {
        String url = playersUrl() + "?playerPosition=FORWARD&page=0&size=20";
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url,
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
        content.forEach(item -> {
            PlayerDTO player = objectMapper.convertValue(item, PlayerDTO.class);
            assertThat(player.playerPosition.name()).isEqualTo("FORWARD");
        });
    }

    @Test
    void getRanking_returnsPlayersOrderedByPrice() {
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            rankingUrl(),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
        List<PlayerDTO> ranking = content.stream()
            .map(item -> objectMapper.convertValue(item, PlayerDTO.class))
            .toList();
        assertThat(ranking).isNotEmpty();

        for (PlayerDTO player : ranking) {
            assertThat(player.id).isNotNull();
            assertThat(player.name).isNotBlank();
            assertThat(player.currentTokenPrice).isNotNull().isPositive();
        }

        for (int i = 0; i < ranking.size() - 1; i++) {
            assertThat(ranking.get(i).currentTokenPrice)
                .isGreaterThanOrEqualTo(ranking.get(i + 1).currentTokenPrice);
        }
    }

    @Test
    void getPlayerQuotes_returnsQuoteHistory() {
        ResponseEntity<List<QuoteDTO>> response = restTemplate.exchange(
            playerQuotesUrl(firstPlayerId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<QuoteDTO> quotes = response.getBody();
        assertThat(quotes).isNotEmpty();
        quotes.forEach(quote -> {
            assertThat(quote.currentTokenPrice).isPositive();
            assertThat(quote.strategy).isNotNull();
            assertThat(quote.calculatedAt).isNotNull();
        });
    }

    @Test
    void recalculateQuotes_withGeneralPerformance_createsNewQuotes() {
        long quoteCountBefore = quoteRepository.count();

        Map<String, String> request = new HashMap<>();
        request.put("strategy", ValuationStrategy.GENERAL_PERFORMANCE.name());

        ResponseEntity<Void> response = restTemplate.exchange(
            recalculateUrl(),
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders()),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(quoteRepository.count()).isGreaterThan(quoteCountBefore);
    }

    @Test
    void recalculateQuotes_withPositionWeighted_createsNewQuotes() {
        long quoteCountBefore = quoteRepository.count();

        Map<String, String> request = new HashMap<>();
        request.put("strategy", ValuationStrategy.POSITION_WEIGHTED.name());

        ResponseEntity<Void> response = restTemplate.exchange(
            recalculateUrl(),
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders()),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(quoteRepository.count()).isGreaterThan(quoteCountBefore);
    }

    @Test
    void getQuotesByPlayer_returnsQuotesForPlayer() {
        ResponseEntity<List<QuoteDTO>> response = restTemplate.exchange(
            quotesByPlayerUrl(firstPlayerId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<QuoteDTO> quotes = response.getBody();
        assertThat(quotes).isNotEmpty();
        quotes.forEach(quote -> {
            assertThat(quote.currentTokenPrice).isPositive();
            assertThat(quote.strategy).isNotNull();
        });
    }

    @Test
    void recalculateThenGetQuotes_reflectsNewStrategy() {
        Map<String, String> request = new HashMap<>();
        request.put("strategy", ValuationStrategy.POSITION_WEIGHTED.name());

        restTemplate.exchange(
            recalculateUrl(),
            HttpMethod.POST,
            new HttpEntity<>(request, authHeaders()),
            Void.class
        );

        ResponseEntity<List<QuoteDTO>> response = restTemplate.exchange(
            quotesByPlayerUrl(firstPlayerId),
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );

        List<QuoteDTO> quotes = response.getBody();
        assertThat(quotes).isNotEmpty();
        boolean hasPositionWeighted = quotes.stream()
            .anyMatch(q -> q.strategy == ValuationStrategy.POSITION_WEIGHTED);
        assertThat(hasPositionWeighted).isTrue();
    }

    private List<PlayerDTO> listPlayers(int page, int size) {
        String url = playersUrl() + "?page=" + page + "&size=" + size;
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
            url, HttpMethod.GET, new HttpEntity<>(authHeaders()),
            new ParameterizedTypeReference<>() {}
        );
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
        return content.stream()
            .map(item -> objectMapper.convertValue(item, PlayerDTO.class))
            .toList();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken);
        return headers;
    }

    private String bootstrapUrl()                  { return "http://localhost:" + port + "/admin/bootstrap/demo-data"; }
    private String loginUrl()                      { return "http://localhost:" + port + "/auth/login"; }
    private String playersUrl()                    { return "http://localhost:" + port + "/players"; }
    private String playerUrl(Long id)              { return "http://localhost:" + port + "/players/" + id; }
    private String rankingUrl()                    { return "http://localhost:" + port + "/players/ranking"; }
    private String playerQuotesUrl(Long id)        { return "http://localhost:" + port + "/players/" + id + "/quotes"; }
    private String recalculateUrl()                { return "http://localhost:" + port + "/quotes/recalculate"; }
    private String quotesByPlayerUrl(Long id)      { return "http://localhost:" + port + "/quotes/player/" + id; }
}
