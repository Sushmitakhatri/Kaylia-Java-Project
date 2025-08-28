package com.app.Kaylia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoutineRecommendationDTO {
    private String routineType; // "morning" or "evening"
    private List<ProductRecommendationDTO> products;
}
