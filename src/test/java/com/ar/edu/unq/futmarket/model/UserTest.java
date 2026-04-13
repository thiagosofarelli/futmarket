package com.ar.edu.unq.futmarket.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    // --- Constructor (username only) ---

    @Test
    void constructor_setsUsernameAndDefaults() {
        User user = new User("leandro");
        assertEquals("leandro", user.getUsername());
        assertEquals(0, BigDecimal.ZERO.compareTo(user.getBalance()));
        assertFalse(user.isSuperuser());
    }

    @Test
    void constructor_createsPortfolioAutomatically() {
        User user = new User("leandro");
        assertNotNull(user.getPortfolio());
        assertSame(user, user.getPortfolio().getUser());
    }

    @Test
    void constructor_trimsUsername() {
        User user = new User("  leandro  ");
        assertEquals("leandro", user.getUsername());
    }

    @Test
    void constructor_nullUsername_throws() {
        assertThrows(IllegalArgumentException.class, () -> new User(null));
    }

    @Test
    void constructor_blankUsername_throws() {
        assertThrows(IllegalArgumentException.class, () -> new User("   "));
    }

    // --- Constructor (username, balance, superuser) ---

    @Test
    void constructor_fullArgs_setsAllFields() {
        User user = new User("admin", new BigDecimal("1000.00"), true);
        assertEquals("admin", user.getUsername());
        assertEquals(0, new BigDecimal("1000.00").compareTo(user.getBalance()));
        assertTrue(user.isSuperuser());
    }

    @Test
    void constructor_negativeBalance_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("user", new BigDecimal("-1"), false));
    }

    @Test
    void constructor_nullBalance_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new User("user", null, false));
    }

    // --- setUsername ---

    @Test
    void setUsername_updatesAndTrims() {
        User user = new User("leandro");
        user.setUsername("  juan  ");
        assertEquals("juan", user.getUsername());
    }

    @Test
    void setUsername_null_throws() {
        User user = new User("leandro");
        assertThrows(IllegalArgumentException.class, () -> user.setUsername(null));
    }

    @Test
    void setUsername_blank_throws() {
        User user = new User("leandro");
        assertThrows(IllegalArgumentException.class, () -> user.setUsername(""));
    }

    // --- addBalance ---

    @Test
    void addBalance_increasesBalance() {
        User user = new User("leandro");
        user.addBalance(new BigDecimal("500.00"));
        assertEquals(0, new BigDecimal("500.00").compareTo(user.getBalance()));
    }

    @Test
    void addBalance_accumulatesMultipleCalls() {
        User user = new User("leandro");
        user.addBalance(new BigDecimal("300.00"));
        user.addBalance(new BigDecimal("200.00"));
        assertEquals(0, new BigDecimal("500.00").compareTo(user.getBalance()));
    }

    @Test
    void addBalance_null_throws() {
        User user = new User("leandro");
        assertThrows(IllegalArgumentException.class, () -> user.addBalance(null));
    }

    @Test
    void addBalance_zero_throws() {
        User user = new User("leandro");
        assertThrows(IllegalArgumentException.class, () -> user.addBalance(BigDecimal.ZERO));
    }

    @Test
    void addBalance_negative_throws() {
        User user = new User("leandro");
        assertThrows(IllegalArgumentException.class, () -> user.addBalance(new BigDecimal("-100")));
    }

    // --- subBalance ---

    @Test
    void subBalance_decreasesBalance() {
        User user = new User("leandro", new BigDecimal("1000.00"), false);
        user.subBalance(new BigDecimal("300.00"));
        assertEquals(0, new BigDecimal("700.00").compareTo(user.getBalance()));
    }

    @Test
    void subBalance_exactBalance_leavesZero() {
        User user = new User("leandro", new BigDecimal("500.00"), false);
        user.subBalance(new BigDecimal("500.00"));
        assertEquals(0, BigDecimal.ZERO.compareTo(user.getBalance()));
    }

    @Test
    void subBalance_moreThanBalance_clampsAtZero() {
        User user = new User("leandro", new BigDecimal("100.00"), false);
        user.subBalance(new BigDecimal("9999.00"));
        assertEquals(0, BigDecimal.ZERO.compareTo(user.getBalance()));
    }

    @Test
    void subBalance_null_throws() {
        User user = new User("leandro", new BigDecimal("1000.00"), false);
        assertThrows(IllegalArgumentException.class, () -> user.subBalance(null));
    }

    @Test
    void subBalance_negative_throws() {
        User user = new User("leandro", new BigDecimal("1000.00"), false);
        assertThrows(IllegalArgumentException.class, () -> user.subBalance(new BigDecimal("-100")));
    }
}
