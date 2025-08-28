package com.app.Kaylia.repository;


import com.app.Kaylia.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepo extends JpaRepository<Cart, Integer> {
    Optional<Cart> findByUserIdAndProductId(int userId, int productId);

    List<Cart> findByUserId(int userId);

    Cart findProductByUserIdAndProductId(int userId, int productId);

    void deleteByUserId(int userId);

}
