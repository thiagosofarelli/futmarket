package com.ar.edu.unq.futmarket.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "portfolio_id", nullable = false, unique = true)
    private Portfolio portfolio;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false)
    private boolean superuser = false;

    @Version
    private Long version;

    public User(String username) {
        this(username, BigDecimal.ZERO, false);
    }

    public User(String username, BigDecimal balance, boolean superuser) {
        validateUsername(username);
        validateNonNegativeBalance(balance);
        this.username = username.trim();
        this.balance = balance;
        this.superuser = superuser;
        this.portfolio = new Portfolio();
        portfolio.setUser(this);
    }

    public void setUsername(String username) {
        validateUsername(username);
        this.username = username.trim();
    }

    public void setBalance(BigDecimal balance) {
        validateNonNegativeBalance(balance);
        this.balance = balance;
    }

    public void addBalance(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount must be > 0");
        }
        balance = balance.add(amount);
    }

    public void subBalance(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("amount must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("amount must be >= 0");
        }

        balance = balance.subtract(amount).max(BigDecimal.ZERO);
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("username must not be blank");
        }
    }

    private void validateNonNegativeBalance(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("balance must not be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("balance must be >= 0");
        }
    }
}
