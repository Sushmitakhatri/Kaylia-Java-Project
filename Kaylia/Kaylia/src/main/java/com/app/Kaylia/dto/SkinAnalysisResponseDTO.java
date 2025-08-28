package com.app.Kaylia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Response DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinAnalysisResponseDTO {
    private Long analysisId;
    private SkinProfileDTO skinProfile;
    private RoutineRecommendationDTO morningRoutine;
    private RoutineRecommendationDTO eveningRoutine;
    private double totalPrice;
    private String message;
}
