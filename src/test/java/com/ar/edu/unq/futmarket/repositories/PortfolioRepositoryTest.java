package com.ar.edu.unq.futmarket.repositories;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.model.Position;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PortfolioRepositoryTest {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    private User alice;
    private Player messi;

    @BeforeEach
    void setUp() {
        alice = user("alice");
        userRepository.saveAndFlush(alice);

        messi = player("Messi", PlayerPosition.FORWARD, new BigDecimal("120.00"));
        playerRepository.saveAndFlush(messi);
    }

    @Test
    void save_user_createsPortfolioAutomatically() {
        assertThat(alice.getPortfolio()).isNotNull();
        assertThat(portfolioRepository.findByUser(alice)).isPresent();
        assertThat(portfolioRepository.findByUserId(alice.getId())).isPresent();
    }

    @Test
    void portfolio_canStorePositions_andCalculateDerivedValues() {
        Portfolio portfolio = alice.getPortfolio();
        portfolio.addOrUpdatePosition(messi, 10, new BigDecimal("100.00"));
        portfolioRepository.saveAndFlush(portfolio);

        Portfolio found = portfolioRepository.findByUser(alice).orElseThrow();
        Position position = found.getPosition(messi).orElseThrow();

        assertThat(position.getTokensAcquired()).isEqualTo(10);
        assertThat(position.getAveragePurchasePrice()).isEqualByComparingTo("100.0000");
        assertThat(position.getInvestedAmount()).isEqualByComparingTo("1000.0000");
        assertThat(position.getCurrentValue()).isEqualByComparingTo("1200.0000");
        assertThat(position.getProfitLoss()).isEqualByComparingTo("200.0000");
    }

    @Test
    void portfolio_registerSale_removesPositionWhenBalanceReachesZero() {
        Portfolio portfolio = alice.getPortfolio();
        portfolio.addOrUpdatePosition(messi, 5, new BigDecimal("100.00"));
        portfolio.registerSale(messi, 5);
        portfolioRepository.saveAndFlush(portfolio);

        Portfolio found = portfolioRepository.findByUser(alice).orElseThrow();
        assertThat(found.getPosition(messi)).isEmpty();
        assertThat(found.getPositions()).isEmpty();
    }

    @Test
    void version_incrementsOnPositionUpdate() {
        Portfolio portfolio = alice.getPortfolio();
        portfolio.addOrUpdatePosition(messi, 8, new BigDecimal("100.00"));
        portfolioRepository.saveAndFlush(portfolio);

        Portfolio found = portfolioRepository.findByUser(alice).orElseThrow();
        Position position = found.getPosition(messi).orElseThrow();
        Long initialVersion = position.getVersion();

        found.addOrUpdatePosition(messi, 2, new BigDecimal("110.00"));
        Portfolio updated = portfolioRepository.saveAndFlush(found);

        Position updatedPosition = updated.getPosition(messi).orElseThrow();
        assertThat(updatedPosition.getVersion()).isGreaterThan(initialVersion);
    }

    // --- helpers ---

    private User user(String username) {
        User u = new User();
        u.setUsername(username);
        u.setBalance(BigDecimal.ZERO);
        return u;
    }

    private Player player(String name, PlayerPosition playerPosition, BigDecimal currentTokenPrice) {
        Player p = new Player();
        p.setName(name);
        p.setTeam("Team A");
        p.setLeague("League A");
        p.setPlayerPosition(playerPosition);
        p.setCurrentTokenPrice(currentTokenPrice);
        return p;
    }
}
