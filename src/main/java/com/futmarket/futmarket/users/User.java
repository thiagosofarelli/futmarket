package com.futmarket.futmarket.users;

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

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    /** True for the system superuser that holds all tokens at time zero. */
    @Column(nullable = false)
    private boolean superuser = false;
}
