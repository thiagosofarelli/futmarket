package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.dto.PlayerDTO;
import com.ar.edu.unq.futmarket.model.Player;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.model.enums.PlayerPosition;
import com.ar.edu.unq.futmarket.services.PlayerService;
import com.ar.edu.unq.futmarket.services.QuoteService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final QuoteService quoteService;
    private final ModelMapper modelMapper;

    @GetMapping
    public ResponseEntity<List<PlayerDTO>> getPlayers(
            @RequestParam(required = false) String league,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) PlayerPosition position) {
        List<Player> players = playerService.findByFilters(league, team, position);
        List<PlayerDTO> dtos = players.stream()
                .map(player -> modelMapper.map(player, PlayerDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/ranking")
    public ResponseEntity<List<PlayerDTO>> getRanking() {
        List<Player> players = playerService.getRanking();
        List<PlayerDTO> dtos = players.stream()
                .map(player -> modelMapper.map(player, PlayerDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlayerDTO> getPlayer(@PathVariable Long id) {
        Player player = playerService.findById(id);
        PlayerDTO dto = modelMapper.map(player, PlayerDTO.class);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}/quotes")
    public ResponseEntity<List<Quote>> getPlayerQuotes(@PathVariable Long id) {
        playerService.findById(id);
        return ResponseEntity.ok(quoteService.findByPlayerId(id));
    }
}
