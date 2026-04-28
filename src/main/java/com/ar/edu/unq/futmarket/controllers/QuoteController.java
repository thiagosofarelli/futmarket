package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.dto.QuoteDTO;
import com.ar.edu.unq.futmarket.controllers.request.RecalculateRequest;
import com.ar.edu.unq.futmarket.model.Quote;
import com.ar.edu.unq.futmarket.services.impl.QuoteServiceImpl;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteServiceImpl quoteService;
    private final ModelMapper modelMapper;

    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculate(@RequestBody RecalculateRequest request) {
        quoteService.recalculateAll(request.getStrategy());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{playerId}")
    public ResponseEntity<List<QuoteDTO>> getQuotesByPlayer(@PathVariable Long playerId) {
        List<Quote> quotes = quoteService.findByPlayerId(playerId);
        List<QuoteDTO> dtos = quotes.stream()
                .map(q -> modelMapper.map(q, QuoteDTO.class))
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
