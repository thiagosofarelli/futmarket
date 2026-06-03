package com.ar.edu.unq.futmarket.model;

import com.ar.edu.unq.futmarket.exception.InsufficientBalanceException;
import com.ar.edu.unq.futmarket.exception.InvalidBalanceException;
import com.ar.edu.unq.futmarket.exception.UsernameCannotBeBlankException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore
    @Column
    private String password;

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

    @Builder
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

    public void addBalance(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidBalanceException();
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBalanceException();
        }
        balance = balance.add(amount);
    }

    public void subBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBalanceException();
        }

        BigDecimal newBalance = this.balance.subtract(amount);

        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientBalanceException();
        }

        this.balance = newBalance;
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new UsernameCannotBeBlankException();
        }
    }

    private void validateNonNegativeBalance(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidBalanceException();
        }
    }
}
