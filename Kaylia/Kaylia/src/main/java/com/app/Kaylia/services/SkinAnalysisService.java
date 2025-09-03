package com.app.Kaylia.services;

import com.app.Kaylia.dto.*;
import com.app.Kaylia.model.Products;
import com.app.Kaylia.model.SkinAnalysis;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.ProductsRepo;
import com.app.Kaylia.repository.SkinAnalysisRepository;
import com.app.Kaylia.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SkinAnalysisService {

    @Autowired
    private SkinAnalysisRepository skinAnalysisRepository;

    @Autowired
    private ProductsRepo productsRepository;

    @Autowired
    private UserRepo userRepository;

    public SkinAnalysisResponseDTO analyzeSkin(SkinAnalysisRequestDTO request, Long userId) {
        // Save analysis to database
        SkinAnalysis analysis = saveSkinAnalysis(request, userId);

        // Generate recommendations
        SkinAnalysisResponseDTO response = generateRecommendations(analysis);
        response.setAnalysisId(analysis.getId());

        return response;
    }

    private SkinAnalysis saveSkinAnalysis(SkinAnalysisRequestDTO request, Long userId) {
        // Convert Long to Integer for the findById method
        User user = userRepository.findById(Math.toIntExact(userId))
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Delete existing skin analysis for this user if it exists
        skinAnalysisRepository.deleteByUser(user);

        SkinAnalysis analysis = new SkinAnalysis();
        analysis.setUser(user);
        analysis.setSkinType(request.getSkinType());
        analysis.setConcerns(request.getConcerns() != null ? request.getConcerns() : new ArrayList<>());
        analysis.setTimeAvailable(request.getTimeAvailable());
        analysis.setExperience(request.getExperience());
        analysis.setSensitivities(request.getSensitivities() != null ? request.getSensitivities() : new ArrayList<>());
        analysis.setCurrentRoutine(request.getCurrentRoutine() != null ? request.getCurrentRoutine() : new ArrayList<>());
        analysis.setGoals(request.getGoals() != null ? request.getGoals() : new ArrayList<>());

        System.out.println("Saving analysis for user: " + user.getEmail());
        return skinAnalysisRepository.save(analysis);
    }

    private SkinAnalysisResponseDTO generateRecommendations(SkinAnalysis analysis) {
        SkinAnalysisResponseDTO response = new SkinAnalysisResponseDTO();

        // Create skin profile
        SkinProfileDTO skinProfile = new SkinProfileDTO();
        skinProfile.setSkinType(analysis.getSkinType().toString().toLowerCase());
        skinProfile.setPrimaryConcerns(analysis.getConcerns().stream()
                .map(concern -> concern.toString().toLowerCase().replace("_", " "))
                .collect(Collectors.toList()));
        skinProfile.setExperienceLevel(analysis.getExperience().toString().toLowerCase());
        response.setSkinProfile(skinProfile);

        // Generate morning and evening routines
        response.setMorningRoutine(generateMorningRoutine(analysis));
        response.setEveningRoutine(generateEveningRoutine(analysis));

        // Calculate total price
        double totalPrice = calculateTotalPrice(response.getMorningRoutine(), response.getEveningRoutine());
        response.setTotalPrice(totalPrice);

        response.setMessage("Your personalized skincare routine has been created!");

        return response;
    }

    private RoutineRecommendationDTO generateMorningRoutine(SkinAnalysis analysis) {
        List<ProductRecommendationDTO> morningProducts = new ArrayList<>();

        // 1. Morning Cleanser
        ProductRecommendationDTO cleanser = recommendCleanser(analysis, "morning");
        if (cleanser != null) morningProducts.add(cleanser);

        // 2. Morning Serum/Treatment
        ProductRecommendationDTO serum = recommendSerum(analysis, "morning");
        if (serum != null) morningProducts.add(serum);

        // 3. Morning Moisturizer
        ProductRecommendationDTO moisturizer = recommendMoisturizer(analysis, "morning");
        if (moisturizer != null) morningProducts.add(moisturizer);

        // 4. Sunscreen (always for morning)
        ProductRecommendationDTO sunscreen = recommendSunscreen(analysis);
        if (sunscreen != null) morningProducts.add(sunscreen);

        RoutineRecommendationDTO routine = new RoutineRecommendationDTO();
        routine.setRoutineType("morning");
        routine.setProducts(morningProducts);

        return routine;
    }

    private RoutineRecommendationDTO generateEveningRoutine(SkinAnalysis analysis) {
        List<ProductRecommendationDTO> eveningProducts = new ArrayList<>();

        // 1. Evening Cleanser (double cleansing for intermediate/advanced)
        if (analysis.getExperience() != SkinAnalysis.ExperienceLevel.BEGINNER) {
            ProductRecommendationDTO oilCleanser = recommendOilCleanser(analysis);
            if (oilCleanser != null) eveningProducts.add(oilCleanser);
        }

        ProductRecommendationDTO cleanser = recommendCleanser(analysis, "evening");
        if (cleanser != null) eveningProducts.add(cleanser);

        // 2. Evening Treatment (based on experience and concerns)
        if (analysis.getExperience() != SkinAnalysis.ExperienceLevel.BEGINNER) {
            ProductRecommendationDTO treatment = recommendTreatment(analysis);
            if (treatment != null) eveningProducts.add(treatment);
        }

        // 3. Evening Moisturizer
        ProductRecommendationDTO moisturizer = recommendMoisturizer(analysis, "evening");
        if (moisturizer != null) eveningProducts.add(moisturizer);

        RoutineRecommendationDTO routine = new RoutineRecommendationDTO();
        routine.setRoutineType("evening");
        routine.setProducts(eveningProducts);

        return routine;
    }

    private ProductRecommendationDTO recommendCleanser(SkinAnalysis analysis, String timeOfDay) {
        List<Products> cleansers = productsRepository.findByCategoryIgnoreCase("cleansers");

        if (cleansers.isEmpty()) return null;

        // Logic for cleanser selection based on skin type
        Products selectedCleanser = cleansers.stream()
                .filter(p -> isCleanserSuitableForSkinType(p, analysis.getSkinType()))
                .findFirst()
                .orElse(cleansers.get(0));

        String reason = generateCleanserReason(analysis.getSkinType(), timeOfDay);
        return createProductRecommendation(selectedCleanser, reason);
    }

    private ProductRecommendationDTO recommendSerum(SkinAnalysis analysis, String timeOfDay) {
        List<Products> serums = productsRepository.findByCategoryIgnoreCase("serum");

        if (serums.isEmpty()) return null;

        // Select serum based on primary concern
        Products selectedSerum = serums.stream()
                .filter(p -> isSerumSuitableForConcerns(p, analysis.getConcerns()))
                .findFirst()
                .orElse(serums.get(0));

        String reason = generateSerumReason(analysis.getConcerns());
        return createProductRecommendation(selectedSerum, reason);
    }

    private ProductRecommendationDTO recommendMoisturizer(SkinAnalysis analysis, String timeOfDay) {
        List<Products> moisturizers = productsRepository.findByCategoryIgnoreCase("moisturizer");

        if (moisturizers.isEmpty()) return null;

        Products selectedMoisturizer = moisturizers.stream()
                .filter(p -> isMoisturizerSuitableForSkinType(p, analysis.getSkinType(), timeOfDay))
                .findFirst()
                .orElse(moisturizers.get(0));

        String reason = generateMoisturizerReason(analysis.getSkinType(), timeOfDay);
        return createProductRecommendation(selectedMoisturizer, reason);
    }

    private ProductRecommendationDTO recommendSunscreen(SkinAnalysis analysis) {
        List<Products> sunscreens = productsRepository.findByCategoryIgnoreCase("sunscreen");

        if (sunscreens.isEmpty()) return null;

        Products selectedSunscreen = sunscreens.get(0); // For now, select the first available
        String reason = "Essential daily protection against UV damage and premature aging";
        return createProductRecommendation(selectedSunscreen, reason);
    }

    private ProductRecommendationDTO recommendOilCleanser(SkinAnalysis analysis) {
        List<Products> cleansers = productsRepository.findByCategoryIgnoreCase("cleansers");

        // Look for oil cleansers or balms
        Products oilCleanser = cleansers.stream()
                .filter(p -> p.getName().toLowerCase().contains("oil") ||
                        p.getName().toLowerCase().contains("balm") ||
                        p.getDescription().toLowerCase().contains("double cleans"))
                .findFirst()
                .orElse(null);

        if (oilCleanser == null) return null;

        String reason = "First step in double cleansing to remove makeup and sunscreen";
        return createProductRecommendation(oilCleanser, reason);
    }

    private ProductRecommendationDTO recommendTreatment(SkinAnalysis analysis) {
        List<Products> treatments = productsRepository.findByCategoryIgnoreCase("serum");

        // Select treatment based on primary concern and experience
        Products selectedTreatment = treatments.stream()
                .filter(p -> isTreatmentSuitableForConcernsAndExperience(p, analysis.getConcerns(), analysis.getExperience()))
                .findFirst()
                .orElse(null);

        if (selectedTreatment == null) return null;

        String reason = generateTreatmentReason(analysis.getConcerns(), analysis.getExperience());
        return createProductRecommendation(selectedTreatment, reason);
    }

    // Helper methods for product selection logic
    private boolean isCleanserSuitableForSkinType(Products product, SkinAnalysis.SkinType skinType) {
        String description = product.getDescription().toLowerCase();
        String name = product.getName().toLowerCase();

        switch (skinType) {
            case OILY:
                return description.contains("oil control") || description.contains("foaming") ||
                        name.contains("foam") || description.contains("deep clean");
            case DRY:
                return description.contains("gentle") || description.contains("hydrating") ||
                        description.contains("cream") || name.contains("cream");
            case SENSITIVE:
                return description.contains("gentle") || description.contains("sensitive") ||
                        description.contains("mild");
            default:
                return true; // Normal/combination can use most cleansers
        }
    }

    private boolean isSerumSuitableForConcerns(Products product, List<SkinAnalysis.SkinConcern> concerns) {
        String description = product.getDescription().toLowerCase();
        String name = product.getName().toLowerCase();
        List<String> ingredients = product.getIngredients().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        for (SkinAnalysis.SkinConcern concern : concerns) {
            switch (concern) {
                case ACNE:
                    if (description.contains("acne") || description.contains("blemish") ||
                            ingredients.contains("salicylic acid") || ingredients.contains("niacinamide")) {
                        return true;
                    }
                    break;
                case AGING:
                    if (description.contains("anti-aging") || description.contains("wrinkle") ||
                            ingredients.contains("retinol") || ingredients.contains("peptide") ||
                            ingredients.contains("vitamin c")) {
                        return true;
                    }
                    break;
                case HYPERPIGMENTATION:
                    if (description.contains("brightening") || description.contains("dark spot") ||
                            ingredients.contains("vitamin c") || ingredients.contains("kojic acid") ||
                            ingredients.contains("arbutin")) {
                        return true;
                    }
                    break;
                case DRYNESS:
                    if (description.contains("hydrating") || description.contains("moisture") ||
                            ingredients.contains("hyaluronic acid") || ingredients.contains("glycerin")) {
                        return true;
                    }
                    break;
                case PORES:
                    if (description.contains("pore") || ingredients.contains("niacinamide") ||
                            ingredients.contains("salicylic acid")) {
                        return true;
                    }
                    break;
            }
        }
        return false;
    }

    private boolean isMoisturizerSuitableForSkinType(Products product, SkinAnalysis.SkinType skinType, String timeOfDay) {
        String description = product.getDescription().toLowerCase();
        String name = product.getName().toLowerCase();

        if ("morning".equals(timeOfDay) && (name.contains("spf") || description.contains("sunscreen"))) {
            return true; // Morning moisturizers with SPF are ideal
        }

        switch (skinType) {
            case OILY:
                return description.contains("lightweight") || description.contains("gel") ||
                        name.contains("gel") || description.contains("oil-free");
            case DRY:
                return description.contains("rich") || description.contains("cream") ||
                        description.contains("intensive") || name.contains("cream");
            case SENSITIVE:
                return description.contains("gentle") || description.contains("sensitive") ||
                        description.contains("fragrance-free");
            default:
                return true;
        }
    }

    private boolean isTreatmentSuitableForConcernsAndExperience(Products product,
                                                                List<SkinAnalysis.SkinConcern> concerns, SkinAnalysis.ExperienceLevel experience) {

        // More targeted treatments for intermediate/advanced users
        if (experience == SkinAnalysis.ExperienceLevel.BEGINNER) {
            return false; // No treatments for beginners in evening routine
        }

        return isSerumSuitableForConcerns(product, concerns);
    }

    // Reason generation methods
    private String generateCleanserReason(SkinAnalysis.SkinType skinType, String timeOfDay) {
        switch (skinType) {
            case OILY:
                return "Controls oil and removes excess sebum without over-drying";
            case DRY:
                return "Gently cleanses while maintaining skin's natural moisture barrier";
            case COMBINATION:
                return "Balances different areas of your face without over-cleansing";
            default:
                return "Maintains healthy skin balance with gentle cleansing";
        }
    }

    private String generateSerumReason(List<SkinAnalysis.SkinConcern> concerns) {
        if (concerns.contains(SkinAnalysis.SkinConcern.ACNE)) {
            return "Targets acne and blemishes while minimizing pores";
        } else if (concerns.contains(SkinAnalysis.SkinConcern.AGING)) {
            return "Reduces fine lines and improves skin firmness";
        } else if (concerns.contains(SkinAnalysis.SkinConcern.HYPERPIGMENTATION)) {
            return "Brightens skin tone and fades dark spots";
        } else if (concerns.contains(SkinAnalysis.SkinConcern.DRYNESS)) {
            return "Provides deep hydration and plumps the skin";
        }
        return "Addresses your primary skin concerns with targeted ingredients";
    }

    private String generateMoisturizerReason(SkinAnalysis.SkinType skinType, String timeOfDay) {
        if ("morning".equals(timeOfDay)) {
            return "Lightweight hydration with built-in sun protection";
        } else {
            switch (skinType) {
                case OILY:
                    return "Hydrates without clogging pores or adding excess oil";
                case DRY:
                    return "Rich overnight hydration to repair and restore skin";
                default:
                    return "Maintains optimal hydration levels throughout the night";
            }
        }
    }

    private String generateTreatmentReason(List<SkinAnalysis.SkinConcern> concerns, SkinAnalysis.ExperienceLevel experience) {
        return "Advanced treatment for your experience level to target specific concerns";
    }

    private ProductRecommendationDTO createProductRecommendation(Products product, String reason) {
        ProductRecommendationDTO dto = new ProductRecommendationDTO();
        dto.setProductId(product.getId());
        dto.setName(product.getName());
        dto.setCategory(product.getCategories());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        dto.setKeyIngredients(product.getIngredients());
        dto.setUsage(product.getHowToUse());
        dto.setImage(product.getImage());
        dto.setReason(reason);
        return dto;
    }

    private double calculateTotalPrice(RoutineRecommendationDTO morning, RoutineRecommendationDTO evening) {
        double total = 0.0;

        if (morning != null && morning.getProducts() != null) {
            total += morning.getProducts().stream()
                    .mapToDouble(ProductRecommendationDTO::getPrice)
                    .sum();
        }

        if (evening != null && evening.getProducts() != null) {
            total += evening.getProducts().stream()
                    .mapToDouble(ProductRecommendationDTO::getPrice)
                    .sum();
        }

        return Math.round(total * 100.0) / 100.0; // Round to 2 decimal places
    }

    public Optional<SkinAnalysis> getLatestAnalysis(Long userId) {
        return skinAnalysisRepository.findLatestByUserId(userId);
    }

    public List<SkinAnalysis> getUserAnalysisHistory(Long userId) {
        return skinAnalysisRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}