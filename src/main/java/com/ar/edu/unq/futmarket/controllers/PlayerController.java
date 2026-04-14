package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.services.PlayerService;
import com.ar.edu.unq.futmarket.services.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final QuoteService quoteService;

    @GetMapping
    public ResponseEntity<List<Player>> getPlayers(
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) PlayerPosition position) {
        return ResponseEntity.ok(playerService.findByFilters(league, team, position));
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<Player>> getRanking() {
        return ResponseEntity.ok(playerService.getRanking());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Player> getPlayer(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.findById(id));
    }

    @GetMapping("/{id}/quotes")
    public ResponseEntity<List<Quote>> getPlayerQuotes(@PathVariable Long id) {
        playerService.findById(id);
        return ResponseEntity.ok(quoteService.findByPlayerId(id));
    }
}
