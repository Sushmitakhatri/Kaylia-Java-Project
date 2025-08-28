package com.app.Kaylia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;

    private String categories;

    @Lob
    private byte[] image; // Image stored in DB

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @ElementCollection
    @CollectionTable(name = "product_ingredients", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "ingredient", nullable = false)
    private List<String> ingredients;

    @ElementCollection
    @CollectionTable(name = "product_benefits", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "benefit", nullable = false)
    private List<String> benefits;

    @Column(nullable = false, columnDefinition = "TEXT", name = "how_to_use")
    private String howToUse;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timeCreated;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", unique = false)
    private User createdBy; // FK to User entity

}
