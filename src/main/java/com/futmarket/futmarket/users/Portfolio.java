package com.futmarket.futmarket.users;

import com.futmarket.futmarket.players.Player;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    /** Number of tokens currently held by this user for this player. */
    @Column(nullable = false)
    private int tokenQuantity;

    /** Optimistic locking to handle concurrent buy/sell on same holding. */
    @Version
    private Long version;
}
