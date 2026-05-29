package com.ar.edu.unq.futmarket.controllers;

import com.ar.edu.unq.futmarket.controllers.response.BootstrapResponse;
import com.ar.edu.unq.futmarket.services.BootstrapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/bootstrap")
@RequiredArgsConstructor
@Tag(name = "Bootstrap", description = "Seed and initialization endpoints")
public class BootstrapController {

    private final BootstrapService bootstrapService;

    @PostMapping("/demo-data")
    @Operation(summary = "Initialize demo data", description = "Creates demo users, players, quotes and purchase orders when they are missing.")
    public ResponseEntity<BootstrapResponse> initializeDemoData() {
        return ResponseEntity.ok(bootstrapService.initializeDemoData());
    }
}
