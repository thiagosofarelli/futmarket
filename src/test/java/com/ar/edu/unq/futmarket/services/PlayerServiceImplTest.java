package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.exception.PlayerNotFoundException;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.repositories.PlayerRepository;
import com.ar.edu.unq.futmarket.services.impl.PlayerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@EnableCaching
class PlayerServiceImplTest {

    @Autowired
    private PlayerServiceImpl playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private QuoteService quoteService;

    private Player haaland;
    private Player lautaro;
    private Player saliba;
    private Player courtois;

    @BeforeEach
    void setUp() {
        if (cacheManager.getCache("rankings") != null) {
            cacheManager.getCache("rankings").clear();
        }

        haaland = save(Player.builder()
                .name("Haaland")
                .team("Manchester City")
                .league(League.PL.getFullName())
                .playerPosition(PlayerPosition.FORWARD)
                .currentTokenPrice(new BigDecimal("50.00"))
                .build());
        lautaro = save(Player.builder()
                .name("Lautaro")
                .team("Inter")
                .league(League.SA.getFullName())
                .playerPosition(PlayerPosition.FORWARD)
                .currentTokenPrice(new BigDecimal("40.00"))
                .build());
        saliba = save(Player.builder()
                .name("Saliba")
                .team("Arsenal FC")
                .league(League.PL.getFullName())
                .playerPosition(PlayerPosition.DEFENDER)
                .currentTokenPrice(new BigDecimal("30.00"))
                .build());
        courtois = save(Player.builder()
                .name("Courtois")
                .team("Real Madrid")
                .league(League.PD.getFullName())
                .playerPosition(PlayerPosition.GOALKEEPER)
                .currentTokenPrice(new BigDecimal("20.00"))
                .build());
    }

    @Test
    void findAll_returnsAllPlayers() {
        assertThat(playerService.findAll()).hasSize(4);
    }

    @Test
    void findById_found_returnsPlayer() {
        Player found = playerService.findById(haaland.getId());
        assertThat(found.getName()).isEqualTo("Haaland");
    }

    @Test
    void findById_notFound_throwsPlayerNotFoundException() {
        assertThatThrownBy(() -> playerService.findById(-1L))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    private static final Pageable ALL = PageRequest.of(0, 100);

    @Test
    void findByFilters_noFilters_returnsAll() {
        assertThat(playerService.findByFilters(null, null, null, ALL).getContent()).hasSize(4);
    }

    @Test
    void findByFilters_byLeague_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters(League.PL, null, null, ALL).getContent();
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Haaland", "Saliba");
    }

    @Test
    void findByFilters_byTeam_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters(null, "Inter", null, ALL).getContent();
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Lautaro");
    }

    @Test
    void findByFilters_byPosition_returnsMatchingPlayers() {
        List<Player> result = playerService.findByFilters(null, null, PlayerPosition.FORWARD, ALL).getContent();
        assertThat(result).hasSize(2)
                .extracting(Player::getName)
                .containsExactlyInAnyOrder("Haaland", "Lautaro");
    }

    @Test
    void findByFilters_byLeagueAndTeam_narrowsDown() {
        List<Player> result = playerService.findByFilters(League.PL, "Arsenal FC", null, ALL).getContent();
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Saliba");
    }

    @Test
    void findByFilters_byLeagueAndPosition_narrowsDown() {
        List<Player> result = playerService.findByFilters(League.PL, null, PlayerPosition.DEFENDER, ALL).getContent();
        assertThat(result).hasSize(1)
                .extracting(Player::getName)
                .containsExactly("Saliba");
    }

    @Test
    void findByFilters_unknownLeague_returnsEmpty() {
        assertThat(playerService.findByFilters(League.BL1, null, null, ALL).getContent()).isEmpty();
    }

    @Test
    void getRanking_returnsSortedByCurrentTokenPriceDesc() {
        Page<Player> ranking = playerService.getRanking(ALL);
        assertThat(ranking.getContent()).extracting(Player::getName)
                .containsExactly("Haaland", "Lautaro", "Saliba", "Courtois");
    }

    @Test
    void getRanking_priceUpdated_reflectsNewOrder() {
        // Note: For this test to succeed, cache is cleared in setUp() but within this test, 
        // if we just update the repository directly, we must clear the cache manually since 
        // the cache is not aware of direct DB updates.
        courtois.setCurrentTokenPrice(new BigDecimal("999.00"));
        playerRepository.save(courtois);
        cacheManager.getCache("rankings").clear();

        Page<Player> ranking = playerService.getRanking(ALL);
        assertThat(ranking.getContent().get(0).getName()).isEqualTo("Courtois");
    }

    @Test
    void getRanking_isCachedAndEvictedOnRecalculateAll() {
        // 1. Initial call to getRanking (caches the result)
        Page<Player> initialRanking = playerService.getRanking(ALL);
        assertThat(initialRanking.getContent()).extracting(Player::getName)
                .containsExactly("Haaland", "Lautaro", "Saliba", "Courtois");

        // 2. Modify player directly in repository
        courtois.setCurrentTokenPrice(new BigDecimal("999.00"));
        playerRepository.save(courtois);

        // 3. getRanking should still return cached result
        Page<Player> cachedRanking = playerService.getRanking(ALL);
        assertThat(cachedRanking.getContent().get(0).getName()).isEqualTo("Haaland");

        // 4. Run QuoteService.recalculateAll (which evicts the cache)
        quoteService.recalculateAll(com.ar.edu.unq.futmarket.model.enums.ValuationStrategy.GENERAL_PERFORMANCE);

        // 5. getRanking should now return the fresh updated ranking
        Page<Player> evictedRanking = playerService.getRanking(ALL);
        assertThat(evictedRanking.getContent()).isNotSameAs(cachedRanking.getContent());
    }

    @Test
    void getRanking_pagination_returnsCorrectPage() {
        Page<Player> firstPage = playerService.getRanking(PageRequest.of(0, 2));
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(4);
        assertThat(firstPage.getTotalPages()).isEqualTo(2);
        assertThat(firstPage.getContent()).extracting(Player::getName)
                .containsExactly("Haaland", "Lautaro");
    }

    private Player save(Player p) {
        return playerRepository.save(p);
    }
}
