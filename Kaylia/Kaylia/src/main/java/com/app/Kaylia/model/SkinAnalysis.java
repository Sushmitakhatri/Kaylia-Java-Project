package com.app.Kaylia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "skin_analysis")
public class SkinAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SkinType skinType;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "skin_concerns", joinColumns = @JoinColumn(name = "analysis_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "concern")
    private List<SkinConcern> concerns;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TimeAvailable timeAvailable;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ExperienceLevel experience;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "skin_sensitivities", joinColumns = @JoinColumn(name = "analysis_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "sensitivity")
    private List<Sensitivity> sensitivities;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "current_routine", joinColumns = @JoinColumn(name = "analysis_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "routine_item")
    private List<RoutineItem> currentRoutine;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "skincare_goals", joinColumns = @JoinColumn(name = "analysis_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "goal")
    private List<SkincareGoal> goals;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum SkinType {
        NORMAL, DRY, OILY, COMBINATION, SENSITIVE
    }

    public enum SkinConcern {
        ACNE, AGING, HYPERPIGMENTATION, SENSITIVITY, DRYNESS, PORES
    }

    public enum TimeAvailable {
        MINIMAL, MODERATE, EXTENSIVE
    }

    public enum ExperienceLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    public enum Sensitivity {
        FRAGRANCE, RETINOL, ACIDS, NONE
    }

    public enum RoutineItem {
        CLEANSER_AM, MOISTURIZER_AM, SUNSCREEN, SERUM_AM,
        CLEANSER_PM, MOISTURIZER_PM, TREATMENT_PM, NONE_PM
    }

    public enum SkincareGoal {
        PREVENTION, TREATMENT, GLOW, SIMPLICITY
    }
}