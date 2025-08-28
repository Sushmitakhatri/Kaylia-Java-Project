package com.app.Kaylia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRecommendationDTO {
    private Long productId;
    private String name;
    private String category;
    private double price;
    private String description;
    private List<String> keyIngredients;
    private String usage;
    private byte[] image;
    private String reason; // Why this product was recommended
}
