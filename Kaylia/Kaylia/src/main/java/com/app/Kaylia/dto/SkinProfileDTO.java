package com.app.Kaylia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkinProfileDTO {
    private String skinType;
    private List<String> primaryConcerns;
    private String experienceLevel;
}
