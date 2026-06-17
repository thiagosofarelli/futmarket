package com.ar.edu.unq.futmarket.controllers;

import org.springframework.data.domain.Page;

import java.util.List;

import com.ar.edu.unq.futmarket.model.enums.League;
import com.ar.edu.unq.futmarket.services.PlayerService;
import com.ar.edu.unq.futmarket.services.QuoteService;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ar.edu.unq.futmarket.controllers.dto.PlayerDTO;
import com.ar.edu.unq.futmarket.controllers.dto.QuoteDTO;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final QuoteService quoteService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<Page<PlayerDTO>> getPlayers(
            @RequestParam(required = false) League league,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) PlayerPosition playerPosition,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerDTO> dtos = playerService.findByFilters(league, team, playerPosition, pageable)
                .map(player -> modelMapper.map(player, PlayerDTO.class));
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/ranking")
    public ResponseEntity<Page<PlayerDTO>> getRanking(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PlayerDTO> dtos = playerService.getRanking(pageable)
                .map(player -> modelMapper.map(player, PlayerDTO.class));
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDTO> getPlayer(@PathVariable Long id) {
        Player player = playerService.findById(id);
        PlayerDTO dto = modelMapper.map(player, PlayerDTO.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/quotes")
    public ResponseEntity<List<QuoteDTO>> getPlayerQuotes(@PathVariable Long id) {
        playerService.findById(id);
        return ResponseEntity.ok(quoteService.findByPlayerId(id).stream()
                .map(q -> modelMapper.map(q, QuoteDTO.class))
                .toList());
    }

    @PostMapping("/sync")
    public ResponseEntity<Void> syncPlayers() {
        playerService.syncPlayers();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stats/sync")
    public ResponseEntity<Void> syncStats() {
        playerService.syncPlayersStatsForAllLeagues();
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/stats/sync/{league}") // Send PL/BL1/FL1/PD/SA
    public ResponseEntity<Void> syncPlayersStatsByLeague(@PathVariable League league) {
        playerService.syncPlayersStatsByLeague(league);
        return ResponseEntity.accepted().build();
    }
}
