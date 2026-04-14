package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.dto.RecalculateRequest;
import com.ar.edu.unq.futmarket.services.QuoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @PostMapping("/recalculate")
    public ResponseEntity<Void> recalculate(@RequestBody RecalculateRequest request) {
        quoteService.recalculateAll(request.getStrategy());
        return ResponseEntity.ok().build();
    }
}
