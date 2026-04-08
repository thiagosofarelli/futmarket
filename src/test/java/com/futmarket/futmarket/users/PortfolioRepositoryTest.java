package com.futmarket.futmarket.users;

import com.futmarket.futmarket.players.Player;
import com.futmarket.futmarket.players.PlayerRepository;
import com.futmarket.futmarket.players.Position;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PortfolioRepositoryTest {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private EntityManager em;

    private User alice;
    private User bob;
    private Player messi;
    private Player ramos;

    @BeforeEach
    void setUp() {
        alice = user("alice");
        bob = user("bob");
        userRepository.saveAll(List.of(alice, bob));

        messi = player("Messi", Position.FORWARD);
        ramos = player("Ramos", Position.DEFENDER);
        playerRepository.saveAll(List.of(messi, ramos));
    }

    @Test
    void save_and_findById() {
        Portfolio p = portfolio(alice, messi, 10);
        portfolioRepository.save(p);

        Portfolio found = portfolioRepository.findById(p.getId()).orElseThrow();
        assertThat(found.getTokenQuantity()).isEqualTo(10);
        assertThat(found.getVersion()).isNotNull();
    }

    @Test
    void findByUser_returnsAllPositions() {
        portfolioRepository.save(portfolio(alice, messi, 5));
        portfolioRepository.save(portfolio(alice, ramos, 3));
        portfolioRepository.save(portfolio(bob, messi, 2));

        List<Portfolio> alicePositions = portfolioRepository.findByUser(alice);
        assertThat(alicePositions).hasSize(2);
    }

    @Test
    void findByUserId_returnsCorrectPositions() {
        portfolioRepository.save(portfolio(alice, messi, 7));
        portfolioRepository.save(portfolio(bob, ramos, 1));

        assertThat(portfolioRepository.findByUserId(alice.getId())).hasSize(1);
        assertThat(portfolioRepository.findByUserId(bob.getId())).hasSize(1);
    }

    @Test
    void findByUserAndPlayer_returnsHolding() {
        portfolioRepository.save(portfolio(alice, messi, 4));

        Optional<Portfolio> found = portfolioRepository.findByUserAndPlayer(alice, messi);
        assertThat(found).isPresent();
        assertThat(found.get().getTokenQuantity()).isEqualTo(4);
    }

    @Test
    void findByUserIdAndPlayerId_returnsHolding() {
        portfolioRepository.save(portfolio(alice, ramos, 6));

        Optional<Portfolio> found = portfolioRepository.findByUserIdAndPlayerId(alice.getId(), ramos.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getTokenQuantity()).isEqualTo(6);
    }

    @Test
    void findByUserAndPlayer_noMatch_returnsEmpty() {
        assertThat(portfolioRepository.findByUserAndPlayer(alice, messi)).isEmpty();
    }

    @Test
    void userAndPlayer_uniqueConstraint_preventsduplicates() {
        portfolioRepository.saveAndFlush(portfolio(alice, messi, 5));

        Portfolio duplicate = portfolio(alice, messi, 3);
        assertThatThrownBy(() -> portfolioRepository.saveAndFlush(duplicate))
                .isInstanceOf(Exception.class);
    }

    @Test
    void version_incrementsOnUpdate() {
        Portfolio p = portfolioRepository.save(portfolio(alice, messi, 5));
        Long initialVersion = p.getVersion();

        p.setTokenQuantity(8);
        Portfolio updated = portfolioRepository.saveAndFlush(p);

        assertThat(updated.getVersion()).isGreaterThan(initialVersion);
    }

    @Test
    void optimisticLock_preventsStaleUpdate() {
        Portfolio p = portfolioRepository.saveAndFlush(portfolio(alice, messi, 10));
        em.clear(); // evict from L1 cache so the next two loads are separate Java objects

        // Load stale reader (version=0), then evict it from L1 cache
        Portfolio stale = portfolioRepository.findById(p.getId()).orElseThrow();
        em.clear(); // stale is now detached — independent Java object with version=0

        // Load fresh reader (version=0) and update it → DB version becomes 1
        Portfolio fresh = portfolioRepository.findById(p.getId()).orElseThrow();
        fresh.setTokenQuantity(15);
        portfolioRepository.saveAndFlush(fresh); // DB: version=1
        em.clear();

        // stale is detached with version=0; DB has version=1 → must fail
        stale.setTokenQuantity(20);
        assertThatThrownBy(() -> portfolioRepository.saveAndFlush(stale))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    // --- helpers ---

    private User user(String username) {
        User u = new User();
        u.setUsername(username);
        u.setBalance(BigDecimal.ZERO);
        return u;
    }

    private Player player(String name, Position position) {
        Player p = new Player();
        p.setName(name);
        p.setTeam("Team A");
        p.setLeague("League A");
        p.setPosition(position);
        return p;
    }

    private Portfolio portfolio(User user, Player player, int quantity) {
        Portfolio p = new Portfolio();
        p.setUser(user);
        p.setPlayer(player);
        p.setTokenQuantity(quantity);
        return p;
    }
}
