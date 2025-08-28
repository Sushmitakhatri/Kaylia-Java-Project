package com.app.Kaylia.repository;

import com.app.Kaylia.model.Products;
import com.app.Kaylia.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProductsRepo extends JpaRepository<Products, Long> {
    List<Products> findByTimeCreatedAfter(LocalDateTime dateTime);

    Products findProductsById(Long id);

    // Find all products created by a specific user
    List<Products> findByCreatedBy(User user);

    void removeProductsById(Long id);

    // Find products by category (case insensitive)
    @Query("SELECT p FROM Products p WHERE LOWER(p.categories) = LOWER(:category)")
    List<Products> findByCategoryIgnoreCase(@Param("category") String category);

    // Find products by multiple categories
    @Query("SELECT p FROM Products p WHERE LOWER(p.categories) IN :categories")
    List<Products> findByCategoriesInIgnoreCase(@Param("categories") List<String> categories);

    // Search products by name or ingredients
    @Query("SELECT DISTINCT p FROM Products p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "EXISTS (SELECT i FROM p.ingredients i WHERE LOWER(i) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Products> searchByNameOrIngredients(@Param("keyword") String keyword);

    // Find products suitable for specific skin concerns
    @Query("SELECT DISTINCT p FROM Products p WHERE " +
            "EXISTS (SELECT b FROM p.benefits b WHERE LOWER(b) LIKE LOWER(CONCAT('%', :benefit, '%')))")
    List<Products> findByBenefit(@Param("benefit") String benefit);
}
