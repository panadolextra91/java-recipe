package com.javarecipe.backend.recipe.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "consumer_warnings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = "recipes")
@ToString(exclude = "recipes")
public class ConsumerWarning {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Column(length = 255)
    private String description;

    @ManyToMany(mappedBy = "consumerWarnings")
    @JsonIgnoreProperties("consumerWarnings")
    private Set<Recipe> recipes = new HashSet<>();

    public ConsumerWarning(String name) {
        this.name = name;
    }
    
    public ConsumerWarning(String name, String description) {
        this.name = name;
        this.description = description;
    }
} 