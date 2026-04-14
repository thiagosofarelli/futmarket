package com.ar.edu.unq.futmarket.services;

import com.ar.edu.unq.futmarket.model.Portfolio;
import com.ar.edu.unq.futmarket.model.User;
import com.ar.edu.unq.futmarket.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
class PortfolioServiceTest {

    @Autowired
    private PortfolioService portfolioService;

    @Autowired
    private UserRepository userRepository;

    private User alice;

    @BeforeEach
    void setUp() {
        alice = userRepository.save(new User("alice", new BigDecimal("500.00"), false));
    }

    @Test
    void findByUserId_found_returnsPortfolio() {
        Portfolio portfolio = portfolioService.findByUserId(alice.getId());
        assertThat(portfolio).isNotNull();
        assertThat(portfolio.getUser().getUsername()).isEqualTo("alice");
    }

    @Test
    void findByUserId_notFound_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> portfolioService.findByUserId(-1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("-1");
    }
}
