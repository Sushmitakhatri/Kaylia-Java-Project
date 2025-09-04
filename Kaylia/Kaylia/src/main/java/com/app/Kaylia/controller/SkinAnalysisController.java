package com.app.Kaylia.controller;

import com.app.Kaylia.dto.SkinAnalysisRequestDTO;
import com.app.Kaylia.dto.SkinAnalysisResponseDTO;
import com.app.Kaylia.model.SkinAnalysis;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.SkinAnalysisRepository;
import com.app.Kaylia.repository.UserRepo;
import com.app.Kaylia.services.SkinAnalysisService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/skin-analysis")
public class SkinAnalysisController {

    @Autowired
    private SkinAnalysisService skinAnalysisService;

    @Autowired
    private UserRepo userRepository;

    /**
     * Main endpoint to analyze skin and generate product recommendations
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeSkin(@RequestBody SkinAnalysisRequestDTO request,
                                         HttpSession session) {
        try {
            System.out.println("Received analysis request: " + request);

            // Get user from session
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                System.out.println("No user found in session");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not authenticated. Please log in."));
            }

            System.out.println("Processing analysis for user ID: " + userId);

            // Validate request
            if (!isValidRequest(request)) {
                System.out.println("Invalid request received");
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Invalid request. Please ensure all required fields are provided."));
            }

            // Process analysis
            SkinAnalysisResponseDTO response = skinAnalysisService.analyzeSkin(request, userId);
            System.out.println("Analysis completed successfully");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.err.println("Runtime exception during analysis: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("User not found: " + e.getMessage()));
        } catch (Exception e) {
            System.err.println("Exception during analysis: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An error occurred while analyzing your skin: " + e.getMessage()));
        }
    }

    /**
     * Get user's latest skin analysis
     */
    @GetMapping("/latest")
    public ResponseEntity<?> getLatestAnalysis(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not authenticated. Please log in."));
            }

            Optional<SkinAnalysis> latestAnalysis = skinAnalysisService.getLatestAnalysis(userId);

            if (latestAnalysis.isPresent()) {
                // Convert to response DTO if needed
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("analysis", latestAnalysis.get());
                response.put("message", "Latest analysis retrieved successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.ok(createErrorResponse("No previous analysis found. Take your first skin analysis!"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving analysis: " + e.getMessage()));
        }
    }

    /**
     * Get user's analysis history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getAnalysisHistory(HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not authenticated. Please log in."));
            }

            List<SkinAnalysis> history = skinAnalysisService.getUserAnalysisHistory(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("history", history);
            response.put("count", history.size());
            response.put("message", "Analysis history retrieved successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving analysis history: " + e.getMessage()));
        }
    }

    /**
     * Get specific analysis by ID (user must own the analysis)
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<?> getAnalysisById(@PathVariable Long analysisId,
                                             HttpSession session) {
        try {
            Long userId = getUserIdFromSession(session);
            if (userId == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(createErrorResponse("User not authenticated. Please log in."));
            }

            // Get user's analysis history and check if they own this analysis
            List<SkinAnalysis> userAnalyses = skinAnalysisService.getUserAnalysisHistory(userId);

            Optional<SkinAnalysis> requestedAnalysis = userAnalyses.stream()
                    .filter(analysis -> analysis.getId().equals(analysisId))
                    .findFirst();

            if (requestedAnalysis.isPresent()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("analysis", requestedAnalysis.get());
                response.put("message", "Analysis retrieved successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("Analysis not found or you don't have permission to access it."));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error retrieving analysis: " + e.getMessage()));
        }
    }

    /**
     * Check if user is authenticated and has valid session
     */
    @GetMapping("/session-status")
    public ResponseEntity<?> checkSessionStatus(HttpSession session) {
        Long userId = getUserIdFromSession(session);
        String userEmail = (String) session.getAttribute("email");

        Map<String, Object> response = new HashMap<>();

        if (userId != null && userEmail != null) {
            response.put("authenticated", true);
            response.put("userId", userId);
            response.put("email", userEmail);
            response.put("message", "User is authenticated");
        } else {
            response.put("authenticated", false);
            response.put("message", "User is not authenticated");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Helper method to extract user ID from session part
     */
    private Long getUserIdFromSession(HttpSession session) {
        try {
            // Try to get userId directly from session
            Object userIdObj = session.getAttribute("userId");
            if (userIdObj != null) {
                if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                } else if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj instanceof String) {
                    return Long.parseLong((String) userIdObj);
                }
            }

            // If userId not in session, try to get it from email
            String email = (String) session.getAttribute("email");
            if (email != null) {
                Optional<User> userOpt = userRepository.findUserByEmail(email);
                if (userOpt.isPresent()) {
                    Long userId = (long) userOpt.get().getUserId();
                    // Store userId in session for future requests
                    session.setAttribute("userId", userId);
                    return userId;
                }
            }

            return null;
        } catch (Exception e) {
            System.err.println("Error extracting user ID from session: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validate the skin analysis request
     */
    private boolean isValidRequest(SkinAnalysisRequestDTO request) {
        return request != null &&
                request.getSkinType() != null &&
                request.getConcerns() != null && !request.getConcerns().isEmpty() &&
                request.getTimeAvailable() != null &&
                request.getExperience() != null &&
                request.getSensitivities() != null &&
                request.getCurrentRoutine() != null &&
                request.getGoals() != null && !request.getGoals().isEmpty();
    }

    /**
     * Create standardized error response
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", true);
        errorResponse.put("message", message);
        return errorResponse;
    }

    /**
     * Create standardized success response
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("data", data);
        return response;
    }
}