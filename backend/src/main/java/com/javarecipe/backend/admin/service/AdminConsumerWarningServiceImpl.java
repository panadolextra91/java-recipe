package com.javarecipe.backend.admin.service;

import com.javarecipe.backend.admin.dto.ConsumerWarningDTO;
import com.javarecipe.backend.admin.dto.ConsumerWarningRequest;
import com.javarecipe.backend.recipe.entity.ConsumerWarning;
import com.javarecipe.backend.recipe.repository.ConsumerWarningRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminConsumerWarningServiceImpl implements AdminConsumerWarningService {

    private final ConsumerWarningRepository consumerWarningRepository;

    @Autowired
    public AdminConsumerWarningServiceImpl(ConsumerWarningRepository consumerWarningRepository) {
        this.consumerWarningRepository = consumerWarningRepository;
    }

    @Override
    public Page<ConsumerWarningDTO> getAllConsumerWarnings(Pageable pageable) {
        return consumerWarningRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public List<ConsumerWarningDTO> getAllConsumerWarnings() {
        return consumerWarningRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ConsumerWarningDTO getConsumerWarningById(Long id) {
        ConsumerWarning warning = consumerWarningRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consumer warning not found with id: " + id));
        return convertToDTO(warning);
    }

    @Override
    @Transactional
    public ConsumerWarningDTO createConsumerWarning(ConsumerWarningRequest warningRequest) {
        // Check if warning name already exists
        if (consumerWarningRepository.existsByName(warningRequest.getName())) {
            throw new IllegalArgumentException("Consumer warning with name '" + warningRequest.getName() + "' already exists");
        }

        ConsumerWarning warning = ConsumerWarning.builder()
                .name(warningRequest.getName())
                .description(warningRequest.getDescription())
                .build();

        ConsumerWarning savedWarning = consumerWarningRepository.save(warning);
        return convertToDTO(savedWarning);
    }

    @Override
    @Transactional
    public ConsumerWarningDTO updateConsumerWarning(Long id, ConsumerWarningRequest warningRequest) {
        ConsumerWarning existingWarning = consumerWarningRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consumer warning not found with id: " + id));

        // Check if the new name conflicts with another warning (excluding current one)
        if (!existingWarning.getName().equals(warningRequest.getName()) && 
            consumerWarningRepository.existsByName(warningRequest.getName())) {
            throw new IllegalArgumentException("Consumer warning with name '" + warningRequest.getName() + "' already exists");
        }

        existingWarning.setName(warningRequest.getName());
        existingWarning.setDescription(warningRequest.getDescription());

        ConsumerWarning updatedWarning = consumerWarningRepository.save(existingWarning);
        return convertToDTO(updatedWarning);
    }

    @Override
    @Transactional
    public void deleteConsumerWarning(Long id) {
        ConsumerWarning warning = consumerWarningRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Consumer warning not found with id: " + id));

        // Check if warning is being used by any recipes
        if (!warning.getRecipes().isEmpty()) {
            throw new IllegalStateException("Cannot delete consumer warning '" + warning.getName() + 
                    "' because it is being used by " + warning.getRecipes().size() + " recipe(s)");
        }

        consumerWarningRepository.delete(warning);
    }

    @Override
    public boolean warningNameExists(String name) {
        return consumerWarningRepository.existsByName(name);
    }

    @Override
    public ConsumerWarningDTO convertToDTO(ConsumerWarning warning) {
        return ConsumerWarningDTO.builder()
                .id(warning.getId())
                .name(warning.getName())
                .description(warning.getDescription())
                .recipeCount(warning.getRecipes() != null ? warning.getRecipes().size() : 0)
                .build();
    }
}
