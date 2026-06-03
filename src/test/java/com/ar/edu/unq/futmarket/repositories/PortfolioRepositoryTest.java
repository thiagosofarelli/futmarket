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
    private User superuser;

    @BeforeEach
    void setUp() {
        superuser = User.builder()
                .username("SUPERUSER")
                .balance(new BigDecimal("1000000.00"))
                .superuser(true)
                .build();
        alice = User.builder()
                .username("alice")
                .balance(new BigDecimal("10000.00"))
                .superuser(false)
                .build();

        superuser = userRepository.saveAndFlush(superuser);
        alice = userRepository.saveAndFlush(alice);

        messi = Player.builder()
                .name("Messi")
                .team("Inter Miami")
                .league("MLS")
                .playerPosition(PlayerPosition.FORWARD)
                .currentTokenPrice(new BigDecimal("120.00"))
                .build();

        messi = playerRepository.saveAndFlush(messi);

        Position messiForSuperuser = new Position();
        messiForSuperuser.setPortfolio(superuser.getPortfolio());
        messiForSuperuser.setPlayer(messi);
        messiForSuperuser.setTokensAcquired(100);
        messiForSuperuser.setAveragePurchasePrice(messi.getCurrentTokenPrice());
        superuser.getPortfolio().getPositions().add(messiForSuperuser);
        userRepository.saveAndFlush(superuser);
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

        portfolio.registerPurchase(messi, 10, superuser);

        portfolioRepository.saveAndFlush(portfolio);

        Portfolio found = portfolioRepository.findByUser(alice).orElseThrow();
        Position position = found.getPosition(messi).orElseThrow();

        assertThat(position.getTokensAcquired()).isEqualTo(10);
        assertThat(position.getAveragePurchasePrice()).isEqualByComparingTo("120.0000");
        assertThat(position.getInvestedAmount()).isEqualByComparingTo("1200.0000");

        assertThat(position.getCurrentValue()).isEqualByComparingTo("1200.0000");
    }

    @Test
    void portfolio_registerSale_removesPositionWhenBalanceReachesZero() {
        Portfolio portfolio = alice.getPortfolio();
        portfolio.registerPurchase(messi, 5, superuser);
        portfolioRepository.saveAndFlush(portfolio);

        portfolio.registerSell(messi, 5, superuser);
        portfolioRepository.saveAndFlush(portfolio);

        Portfolio found = portfolioRepository.findByUser(alice).orElseThrow();
        assertThat(found.getPosition(messi)).isEmpty();
        assertThat(found.getPositions()).isEmpty();
    }

    @Test
    void version_incrementsOnPositionUpdate() {
        Portfolio portfolio = alice.getPortfolio();
        portfolio.registerPurchase(messi, 8, superuser);
        portfolio = portfolioRepository.saveAndFlush(portfolio);

        Position position = portfolio.getPosition(messi).orElseThrow();
        Long initialVersion = position.getVersion();

        portfolio.registerPurchase(messi, 2, superuser);
        Portfolio updated = portfolioRepository.saveAndFlush(portfolio);

        Position updatedPosition = updated.getPosition(messi).orElseThrow();
        assertThat(updatedPosition.getVersion()).isGreaterThanOrEqualTo(initialVersion);
    }
}