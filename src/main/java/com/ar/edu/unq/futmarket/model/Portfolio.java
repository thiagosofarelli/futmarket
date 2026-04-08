package com.ar.edu.unq.futmarket.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(
    name = "portfolios",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "player_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private List<Player> players;

    @Column(nullable = false)
    private int tokenQuantity;

    @Version
    private Long version;
}

