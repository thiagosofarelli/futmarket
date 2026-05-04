package com.ar.edu.unq.futmarket.adapters;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FootballDataClientTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private FootballDataClient client;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "apiKey", "test-key");
        ReflectionTestUtils.setField(client, "maxCallsPerMinute", 600);
    }

    // -----------------------------------------------------------------------
    // guard: api key vacío
    // -----------------------------------------------------------------------

    @Test
    void syncPlayers_blankApiKey_neverCallsApi() {
        ReflectionTestUtils.setField(client, "apiKey", "");
        client.syncPlayers();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void syncPlayers_nullApiKey_neverCallsApi() {
        ReflectionTestUtils.setField(client, "apiKey", null);
        client.syncPlayers();
        verifyNoInteractions(restTemplate);
    }

    // -----------------------------------------------------------------------
    // upsert: jugador nuevo
    // -----------------------------------------------------------------------

    @Test
    void syncPlayers_newPlayer_persistsWithCorrectData() {
        stubFirstLeagueOnly(teamResponse("Manchester City", playerData(10L, "Haaland", "Centre-Forward")));
        when(playerRepository.findPlayerByExternalId(10L)).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        client.syncPlayers();

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, atLeastOnce()).save(captor.capture());
        Player saved = captor.getAllValues().get(0);
        assertThat(saved.getName()).isEqualTo("Haaland");
        assertThat(saved.getTeam()).isEqualTo("Manchester City");
        assertThat(saved.getExternalId()).isEqualTo(10L);
        assertThat(saved.getPlayerPosition()).isEqualTo(PlayerPosition.FORWARD);
    }

    // -----------------------------------------------------------------------
    // upsert: jugador existente
    // -----------------------------------------------------------------------

    @Test
    void syncPlayers_existingPlayer_updatesFields() {
        Player existing = Player.builder()
                .name("Old Name").team("Old Team").league("Old League")
                .playerPosition(PlayerPosition.MIDFIELDER).externalId(42L).build();

        stubFirstLeagueOnly(teamResponse("New Team", playerData(42L, "New Name", "Goalkeeper")));
        when(playerRepository.findPlayerByExternalId(42L)).thenReturn(Optional.of(existing));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        client.syncPlayers();

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, atLeastOnce()).save(captor.capture());
        Player saved = captor.getAllValues().get(0);
        assertThat(saved.getName()).isEqualTo("New Name");
        assertThat(saved.getTeam()).isEqualTo("New Team");
        assertThat(saved.getPlayerPosition()).isEqualTo(PlayerPosition.GOALKEEPER);
    }

    // -----------------------------------------------------------------------
    // manejo de errores
    // -----------------------------------------------------------------------

    @Test
    void syncPlayers_nullResponseBody_savesNoPlayers() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.syncPlayers();

        verify(playerRepository, never()).save(any());
    }

    @Test
    void syncPlayers_restClientException_skipsLeagueAndContinues() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenThrow(new RestClientException("timeout"))
                .thenReturn(ResponseEntity.ok(null));

        client.syncPlayers();

        verify(playerRepository, never()).save(any());
    }

    @Test
    void syncPlayers_playerWithoutId_isSkipped() {
        Map<String, Object> body = Map.of("teams", List.of(
                Map.of("name", "Team", "squad", List.of(Map.of("name", "Ronaldo")))
        ));
        stubFirstLeagueOnly(body);

        client.syncPlayers();

        verify(playerRepository, never()).save(any());
    }

    @Test
    void syncPlayers_playerWithoutName_isSkipped() {
        Map<String, Object> playerWithoutName = new HashMap<>();
        playerWithoutName.put("id", 99);

        Map<String, Object> body = Map.of("teams", List.of(
                Map.of("name", "Team", "squad", List.of(playerWithoutName))
        ));
        stubFirstLeagueOnly(body);

        client.syncPlayers();

        verify(playerRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // mapPosition — verificado vía sincronización
    // -----------------------------------------------------------------------

    @Test
    void syncPlayers_positionGoalkeeper_mapsToGoalkeeper() {
        assertPositionMapping("Goalkeeper", PlayerPosition.GOALKEEPER);
    }

    @Test
    void syncPlayers_positionDefence_mapsToDefender() {
        assertPositionMapping("Defence", PlayerPosition.DEFENDER);
    }

    @Test
    void syncPlayers_positionCentreBack_mapsToDefender() {
        assertPositionMapping("Centre-Back", PlayerPosition.DEFENDER);
    }

    @Test
    void syncPlayers_positionLeftBack_mapsToDefender() {
        assertPositionMapping("Left-Back", PlayerPosition.DEFENDER);
    }

    @Test
    void syncPlayers_positionOffence_mapsToForward() {
        assertPositionMapping("Offence", PlayerPosition.FORWARD);
    }

    @Test
    void syncPlayers_positionCentreForward_mapsToForward() {
        assertPositionMapping("Centre-Forward", PlayerPosition.FORWARD);
    }

    @Test
    void syncPlayers_positionLeftWinger_mapsToForward() {
        assertPositionMapping("Left Winger", PlayerPosition.FORWARD);
    }

    @Test
    void syncPlayers_positionNull_mapsToMidfielder() {
        assertPositionMapping(null, PlayerPosition.MIDFIELDER);
    }

    @Test
    void syncPlayers_positionUnknown_mapsToMidfielder() {
        assertPositionMapping("Unknown Role", PlayerPosition.MIDFIELDER);
    }

    // -----------------------------------------------------------------------
    // minimumIntervalMillis: maxCallsPerMinute <= 0 usa valor por defecto
    // -----------------------------------------------------------------------

    @Test
    void syncPlayers_zeroMaxCallsPerMinute_doesNotDivideByZero() {
        ReflectionTestUtils.setField(client, "maxCallsPerMinute", 0);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(null));

        client.syncPlayers();
    }

    // -----------------------------------------------------------------------
    // helpers
    // -----------------------------------------------------------------------

    private void stubFirstLeagueOnly(Map<String, Object> firstLeagueBody) {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(firstLeagueBody))
                .thenReturn(ResponseEntity.ok(null));
    }

    private Map<String, Object> teamResponse(String teamName, Map<String, Object> player) {
        return Map.of("teams", List.of(Map.of("name", teamName, "squad", List.of(player))));
    }

    private Map<String, Object> playerData(long id, String name, String position) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", (int) id);
        data.put("name", name);
        if (position != null) {
            data.put("position", position);
        }
        return data;
    }

    private void assertPositionMapping(String apiPosition, PlayerPosition expected) {
        reset(playerRepository, restTemplate);
        ReflectionTestUtils.setField(client, "maxCallsPerMinute", 600);

        stubFirstLeagueOnly(teamResponse("Team", playerData(1L, "Player", apiPosition)));
        when(playerRepository.findPlayerByExternalId(1L)).thenReturn(Optional.empty());
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        client.syncPlayers();

        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        verify(playerRepository, atLeastOnce()).save(captor.capture());
        assertThat(captor.getAllValues().get(0).getPlayerPosition()).isEqualTo(expected);
    }
}
