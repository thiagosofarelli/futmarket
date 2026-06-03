package com.ar.edu.unq.futmarket.model;

import com.ar.edu.unq.futmarket.exception.InvalidBalanceException;
import com.ar.edu.unq.futmarket.exception.UsernameCannotBeBlankException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user;
    private User user2;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("leandro")
                .balance(new BigDecimal("1000.00"))
                .superuser(false)
                .build();
        user2 = User.builder()
                .username("admin")
                .balance(new BigDecimal("1000.00"))
                .superuser(true)
                .build();
    }

    @Test
    void constructor_setsUsernameAndDefaults() {
        User user3 = User.builder()
                .username("leandro")
                .balance(BigDecimal.ZERO)
                .build();
        assertEquals("leandro", user3.getUsername());
        assertEquals(0, BigDecimal.ZERO.compareTo(user3.getBalance()));
        assertFalse(user3.isSuperuser());
    }

    @Test
    void constructor_createsPortfolioAutomatically() {
        assertNotNull(user.getPortfolio());
        assertSame(user, user.getPortfolio().getUser());
    }

    @Test
    void constructor_trimsUsername() {
        assertEquals("leandro", user.getUsername());
    }

    @Test
    void constructor_nullUsername_throws() {
        assertThrows(UsernameCannotBeBlankException.class, () -> new User(null));
    }

    @Test
    void constructor_blankUsername_throws() {
        assertThrows(UsernameCannotBeBlankException.class, () -> new User("   "));
    }


    @Test
    void constructor_fullArgs_setsAllFields() {
        assertEquals("admin", user2.getUsername());
        assertEquals(0, new BigDecimal("1000.00").compareTo(user.getBalance()));
        assertTrue(user2.isSuperuser());
    }

    @Test
    void constructor_negativeBalance_throws() {
        BigDecimal negativeBalance = new BigDecimal("-1");
        var userBuilder = User.builder()
                .username("user")
                .balance(negativeBalance);
        assertThrows(InvalidBalanceException.class, () -> userBuilder.build());
    }

    @Test
    void constructor_nullBalance_throws() {
        var userBuilder = User.builder()
                .username("user")
                .balance(null);
        assertThrows(InvalidBalanceException.class,
                () -> userBuilder.build());
    }


    @Test
    void setUsername_updatesAndTrims() {
        user.setUsername("  juan  ");
        assertEquals("juan", user.getUsername());
    }

    @Test
    void setUsername_null_throws() {
        assertThrows(UsernameCannotBeBlankException.class, () -> user.setUsername(null));
    }

    @Test
    void setUsername_blank_throws() {
        assertThrows(UsernameCannotBeBlankException.class, () -> user.setUsername(""));
    }

    // --- addBalance ---

    @Test
    void addBalance_increasesBalance() {
        user.subBalance(new BigDecimal("500.00"));
        assertEquals(0, new BigDecimal("500.00").compareTo(user.getBalance()));
    }

    @Test
    void addBalance_accumulatesMultipleCalls() {
        user.addBalance(new BigDecimal("300.00"));
        user.addBalance(new BigDecimal("200.00"));
        assertEquals(0, new BigDecimal("1500.00").compareTo(user.getBalance()));
    }

    @Test
    void addBalance_null_throws() {
        assertThrows(InvalidBalanceException.class, () -> user.addBalance(null));
    }

    @Test
    void addBalance_zero_throws() {
        assertThrows(InvalidBalanceException.class, () -> user.addBalance(BigDecimal.ZERO));
    }

    @Test
    void addBalance_negative_throws() {
        BigDecimal negativeAmount = new BigDecimal("-100");
        assertThrows(InvalidBalanceException.class, () -> user.addBalance(negativeAmount));
    }

    @Test
    void subBalance_decreasesBalance() {
        user.setBalance(new BigDecimal("1000.00"));
        user.subBalance(new BigDecimal("300.00"));
        assertEquals(0, new BigDecimal("700.00").compareTo(user.getBalance()));
    }

    @Test
    void subBalance_exactBalance_leavesZero() {
        user.setBalance(new BigDecimal("500.00"));
        user.subBalance(new BigDecimal("500.00"));
        assertEquals(0, BigDecimal.ZERO.compareTo(user.getBalance()));
    }

    @Test
    void subBalance_moreThanBalance_throws() {
        user.setBalance(new BigDecimal("500.00"));
        BigDecimal excessAmount = new BigDecimal("9999.00");
        assertThrows(InvalidBalanceException.class, () -> user.subBalance(excessAmount));
    }

    @Test
    void subBalance_null_throws() {
        assertThrows(InvalidBalanceException.class, () -> user.subBalance(null));
    }

    @Test
    void subBalance_negative_throws() {
        BigDecimal negativeAmount = new BigDecimal("-100");
        assertThrows(InvalidBalanceException.class, () -> user.subBalance(negativeAmount));
    }
}
