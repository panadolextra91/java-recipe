package com.javarecipe.backend.admin.controller;

import com.javarecipe.backend.admin.dto.ConsumerWarningDTO;
import com.javarecipe.backend.admin.dto.ConsumerWarningRequest;
import com.javarecipe.backend.admin.service.AdminConsumerWarningService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/consumer-warnings")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminConsumerWarningController {

    private final AdminConsumerWarningService adminConsumerWarningService;

    @Autowired
    public AdminConsumerWarningController(AdminConsumerWarningService adminConsumerWarningService) {
        this.adminConsumerWarningService = adminConsumerWarningService;
    }

    /**
     * Get all consumer warnings with pagination
     */
    @GetMapping
    public ResponseEntity<Page<ConsumerWarningDTO>> getAllConsumerWarnings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ConsumerWarningDTO> warnings = adminConsumerWarningService.getAllConsumerWarnings(pageable);
        return ResponseEntity.ok(warnings);
    }

    /**
     * Get all consumer warnings without pagination (for dropdowns)
     */
    @GetMapping("/all")
    public ResponseEntity<List<ConsumerWarningDTO>> getAllConsumerWarningsNoPagination() {
        List<ConsumerWarningDTO> warnings = adminConsumerWarningService.getAllConsumerWarnings();
        return ResponseEntity.ok(warnings);
    }

    /**
     * Get consumer warning by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getConsumerWarningById(@PathVariable Long id) {
        try {
            ConsumerWarningDTO warning = adminConsumerWarningService.getConsumerWarningById(id);
            return ResponseEntity.ok(warning);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create new consumer warning
     */
    @PostMapping
    public ResponseEntity<?> createConsumerWarning(@Valid @RequestBody ConsumerWarningRequest warningRequest) {
        try {
            ConsumerWarningDTO createdWarning = adminConsumerWarningService.createConsumerWarning(warningRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Consumer warning created successfully");
            response.put("warning", createdWarning);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to create consumer warning: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update existing consumer warning
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateConsumerWarning(@PathVariable Long id, 
                                                 @Valid @RequestBody ConsumerWarningRequest warningRequest) {
        try {
            ConsumerWarningDTO updatedWarning = adminConsumerWarningService.updateConsumerWarning(id, warningRequest);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Consumer warning updated successfully");
            response.put("warning", updatedWarning);
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to update consumer warning: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Delete consumer warning
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteConsumerWarning(@PathVariable Long id) {
        try {
            adminConsumerWarningService.deleteConsumerWarning(id);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Consumer warning deleted successfully");
            
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Failed to delete consumer warning: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Check if consumer warning name exists
     */
    @GetMapping("/check-name")
    public ResponseEntity<Map<String, Boolean>> checkWarningName(@RequestParam String name) {
        boolean exists = adminConsumerWarningService.warningNameExists(name);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }
}
