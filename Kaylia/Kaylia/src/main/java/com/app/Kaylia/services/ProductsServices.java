package com.app.Kaylia.services;

import com.app.Kaylia.model.Products;
import com.app.Kaylia.repository.ProductsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductsServices {

    @Autowired
    private ProductsRepo productsRepo;

    public List<Products> getAllProducts() {
        return productsRepo.findAll();
    }

    @Transactional
    public List<Products> getNewProducts() {
        LocalDateTime threeDaysAgo = LocalDateTime.now().minusDays(3);
        return productsRepo.findByTimeCreatedAfter(threeDaysAgo);
    }


    public Products getProduct(int id) {
        Products products = productsRepo.findById((long) id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return products;
    }
}
