package com.ilich.sb.e_commerce.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.ilich.sb.e_commerce.payload.CategoryDTO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description") // Usa TEXT para descripciones largas
    private String description;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false, fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Product> products = new ArrayList<>();

    public Category(CategoryDTO categoryDTO) {
        this.id = categoryDTO.getId();
        this.name = categoryDTO.getName();
        this.description = categoryDTO.getDescription();
    }


    public Category(String name) {
        this.name = name;
    }
}