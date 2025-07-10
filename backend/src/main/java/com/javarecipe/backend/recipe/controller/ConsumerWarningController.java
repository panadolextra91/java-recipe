package com.javarecipe.backend.recipe.controller;

import com.javarecipe.backend.admin.dto.ConsumerWarningDTO;
import com.javarecipe.backend.admin.service.AdminConsumerWarningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumer-warnings")
public class ConsumerWarningController {

    private final AdminConsumerWarningService adminConsumerWarningService;

    @Autowired
    public ConsumerWarningController(AdminConsumerWarningService adminConsumerWarningService) {
        this.adminConsumerWarningService = adminConsumerWarningService;
    }

    /**
     * Get all consumer warnings (public endpoint for users)
     * This allows users to see available warnings when creating/editing recipes
     */
    @GetMapping
    public ResponseEntity<List<ConsumerWarningDTO>> getAllConsumerWarnings() {
        List<ConsumerWarningDTO> warnings = adminConsumerWarningService.getAllConsumerWarnings();
        return ResponseEntity.ok(warnings);
    }

    /**
     * Get consumer warning by ID (public endpoint)
     */
    @GetMapping("/{id}")
    public ResponseEntity<ConsumerWarningDTO> getConsumerWarningById(@PathVariable Long id) {
        try {
            ConsumerWarningDTO warning = adminConsumerWarningService.getConsumerWarningById(id);
            return ResponseEntity.ok(warning);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
