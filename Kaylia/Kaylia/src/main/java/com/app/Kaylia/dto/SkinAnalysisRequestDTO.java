package com.app.Kaylia.dto;

import com.app.Kaylia.model.SkinAnalysis;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Request DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinAnalysisRequestDTO {
    private SkinAnalysis.SkinType skinType;
    private List<SkinAnalysis.SkinConcern> concerns;
    private SkinAnalysis.TimeAvailable timeAvailable;
    private SkinAnalysis.ExperienceLevel experience;
    private List<SkinAnalysis.Sensitivity> sensitivities;
    private List<SkinAnalysis.RoutineItem> currentRoutine;
    private List<SkinAnalysis.SkincareGoal> goals;
}
