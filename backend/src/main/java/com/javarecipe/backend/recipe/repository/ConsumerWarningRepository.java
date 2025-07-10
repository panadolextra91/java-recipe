package com.javarecipe.backend.recipe.repository;

import com.javarecipe.backend.recipe.entity.ConsumerWarning;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsumerWarningRepository extends JpaRepository<ConsumerWarning, Long> {
    
    Optional<ConsumerWarning> findByName(String name);
    
    boolean existsByName(String name);
} 