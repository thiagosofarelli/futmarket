package com.ar.edu.unq.futmarket.adapters;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.ar.edu.unq.futmarket.model.enums.League;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class FootballDataClient {

    private static final long ONE_MINUTE_MILLIS = Duration.ofMinutes(1).toMillis();

    @Value("${FOOTBALL_DATA_API_KEY:}")
    private String apiKey;

    @Value("${futmarket.football-data.max-calls-per-minute:10}")
    private int maxCallsPerMinute;

    private final PlayerRepository playerRepository;
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.football-data.org/v4";

    public void syncPlayers() {
        if (apiKey == null || apiKey.isBlank()) {
            return;
        }

        String[] leagueCodes = {"PL", "PD", "BL1", "SA", "FL1"};
        long minimumIntervalMillis = minimumIntervalMillis();
        long lastCallStartedAt = 0L;

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Token", apiKey);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        for (String code : leagueCodes) {
            waitIfNeeded(lastCallStartedAt, minimumIntervalMillis);
            lastCallStartedAt = System.currentTimeMillis();

            String url = BASE_URL + "/competitions/" + code + "/teams";
            try {
                ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
                System.out.println(response.getBody());
                Map<String, Object> body = response.getBody();
                if (body == null) {
                    continue;
                }

                List<Map<String, Object>> teams = asListOfMaps(body.get("teams"));
                List<Player> batchPlayers = new java.util.ArrayList<>();

                for (Map<String, Object> teamData : teams) {
                    String teamName = asString(teamData.get("name"));
                    List<Map<String, Object>> squad = asListOfMaps(teamData.get("squad"));

                    for (Map<String, Object> playerData : squad) {
                        Player player = upsertPlayer(playerData, teamName, code);
                        if (player != null) {
                            batchPlayers.add(player);
                        }
                    }
                }
            } catch (RestClientException ex) {
                // Silently skip if league fetch fails
            }
        }
    }

    private long minimumIntervalMillis() {
        int safeCallsPerMinute = maxCallsPerMinute <= 0 ? 10 : maxCallsPerMinute;
        return ONE_MINUTE_MILLIS / safeCallsPerMinute;
    }

    private void waitIfNeeded(long lastCallStartedAt, long minimumIntervalMillis) {
        if (lastCallStartedAt == 0L) {
            return;
        }

        long elapsed = System.currentTimeMillis() - lastCallStartedAt;
        long waitMillis = minimumIntervalMillis - elapsed;
        if (waitMillis <= 0) {
            return;
        }

        try {
            Thread.sleep(waitMillis);
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Player sync interrupted while respecting API rate limit", interruptedException);
        }
    }

    private Player upsertPlayer(Map<String, Object> playerData, String teamName, String leagueCode) {
        String leagueName = League.getFullNameByCode(leagueCode);
        Long externalId = asLong(playerData.get("id"));
        String playerName = asString(playerData.get("name"));
        if (externalId == null || playerName == null || teamName == null || leagueCode == null) {
            return null;
        }

        PlayerPosition position = mapPosition(asString(playerData.get("position")));
        Optional<Player> existingPlayer = playerRepository.findPlayerByExternalId(externalId);

        if (existingPlayer.isPresent()) {
            Player player = existingPlayer.get();
            player.setName(playerName);
            player.setTeam(teamName);
            player.setLeague(leagueName);
            player.setPlayerPosition(position);
            return playerRepository.save(player);
        }

        Player player = new Player(playerName, teamName, leagueName, position);
        player.setExternalId(externalId);
        player.setGoals(0);
        player.setAssists(0);
        player.setShots(0);
        player.setKeyPasses(0);
        player.setDribbles(0);
        player.setTackles(0);
        player.setInterceptions(0);
        player.setRating(0.0);
        return playerRepository.save(player);
    }

    private PlayerPosition mapPosition(String apiPosition) {
        if (apiPosition == null) {
            return PlayerPosition.MIDFIELDER;
        }

        return switch (apiPosition.trim().toLowerCase()) {
            case "goalkeeper" ->
                    PlayerPosition.GOALKEEPER;

            case "defence", "centre-back", "left-back", "right-back" ->
                    PlayerPosition.DEFENDER;

            case "offence", "centre-forward", "left winger", "right winger" ->
                    PlayerPosition.FORWARD;

            default ->
                    PlayerPosition.MIDFIELDER;
        };
    }

    private String asString(Object value) {
        if (!(value instanceof String text) || text.isBlank()) {
            return null;
        }
        return text.trim();
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            try {
                return Long.parseLong(text.trim());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asListOfMaps(Object value) {
        if (value instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return List.of();
    }
}
