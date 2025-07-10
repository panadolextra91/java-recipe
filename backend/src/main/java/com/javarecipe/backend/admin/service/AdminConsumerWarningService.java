package com.javarecipe.backend.admin.service;

import com.javarecipe.backend.admin.dto.ConsumerWarningDTO;
import com.javarecipe.backend.admin.dto.ConsumerWarningRequest;
import com.javarecipe.backend.recipe.entity.ConsumerWarning;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminConsumerWarningService {
    
    /**
     * Get all consumer warnings with pagination
     */
    Page<ConsumerWarningDTO> getAllConsumerWarnings(Pageable pageable);
    
    /**
     * Get all consumer warnings without pagination
     */
    List<ConsumerWarningDTO> getAllConsumerWarnings();
    
    /**
     * Get consumer warning by ID
     */
    ConsumerWarningDTO getConsumerWarningById(Long id);
    
    /**
     * Create new consumer warning
     */
    ConsumerWarningDTO createConsumerWarning(ConsumerWarningRequest warningRequest);
    
    /**
     * Update existing consumer warning
     */
    ConsumerWarningDTO updateConsumerWarning(Long id, ConsumerWarningRequest warningRequest);
    
    /**
     * Delete consumer warning
     */
    void deleteConsumerWarning(Long id);
    
    /**
     * Check if consumer warning name exists
     */
    boolean warningNameExists(String name);
    
    /**
     * Convert ConsumerWarning entity to ConsumerWarningDTO
     */
    ConsumerWarningDTO convertToDTO(ConsumerWarning warning);
}
