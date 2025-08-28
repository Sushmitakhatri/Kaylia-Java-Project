package com.app.Kaylia.controller;

import com.app.Kaylia.services.AddProductServices;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product")
public class AddProductController {

    @Autowired
    private AddProductServices addProductServices;

    @PostMapping("/add")
    public ResponseEntity<?> addProduct(
            @RequestParam String name,
            @RequestParam double price,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String ingredients,
            @RequestParam String benefits,
            @RequestParam String howtouse,
            @RequestParam MultipartFile image,
            HttpSession session
    ) throws IOException {
        String email = (String) session.getAttribute("email");
        List<String> ingredientsList = Arrays.asList(ingredients.split("\\s*,\\s*"));
        List<String> benefitsList = Arrays.asList(benefits.split("\\s*,\\s*"));

        boolean addedProduct = addProductServices.addProduct(name, price, description,category, ingredientsList, benefitsList, howtouse, image, email);

        if (addedProduct){
            return ResponseEntity.ok(Map.of("message", "Product Added Successfully"));
        }else {
            return ResponseEntity.badRequest().body(Map.of("message", "Product Add Failed"));
        }
    }

}
