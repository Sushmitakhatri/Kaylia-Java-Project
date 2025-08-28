package com.app.Kaylia.controller;

import com.app.Kaylia.model.Cart;
import com.app.Kaylia.model.Products;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.CartRepo;
import com.app.Kaylia.repository.ProductsRepo;
import com.app.Kaylia.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
public class AddToCartController {

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ProductsRepo productsRepo;

    @PostMapping("/add-to-cart")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> addToCart(@RequestBody Cart cartItems, HttpSession session) {
        System.out.println("product to add: " + cartItems.getProductId());

        User user = userRepo.findByEmail((String) session.getAttribute("email"));

        Products products = productsRepo.findProductsById((long) cartItems.getProductId());

        // Check if this product already exists in the user's cart
        Optional<Cart> existingCart = cartRepo.findByUserIdAndProductId(user.getUserId(), Math.toIntExact(cartItems.getProductId()));

        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + cartItems.getQuantity()); // increment quantity
            cartRepo.save(cart);
        } else {
            Cart saveCart = new Cart();
            saveCart.setProductId(cartItems.getProductId());
            saveCart.setUserId(user.getUserId());
            saveCart.setQuantity(cartItems.getQuantity());
            saveCart.setProductName(products.getName());
            saveCart.setProductPrice(products.getPrice());
            cartRepo.save(saveCart);
        }

        return ResponseEntity.ok(Map.of("message", "Added to Cart Successfully"));
    }

    @GetMapping("/cart-get")
    @ResponseBody
    public ResponseEntity<?> getCart(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        }

        User user = userRepo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }

        List<Cart> cartItems = cartRepo.findByUserId(user.getUserId());

        return ResponseEntity.ok(cartItems);
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCart(@RequestBody Map<String, Object> request, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        }

        User user = userRepo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }

        int productId = Integer.parseInt(request.get("productId").toString());
        int change = Integer.parseInt(request.get("change").toString());

        Cart cartItem = cartRepo.findProductByUserIdAndProductId(user.getUserId(), productId);

        if (cartItem == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Item not found in cart"));
        }

        int newQty = cartItem.getQuantity() + change;
        if (newQty <= 0) {
            cartRepo.delete(cartItem);
            return ResponseEntity.ok(Map.of("message", "Item removed from cart"));
        } else {
            cartItem.setQuantity(newQty);
            cartRepo.save(cartItem);
            return ResponseEntity.ok(Map.of("message", "Quantity updated", "quantity", newQty));
        }
    }

    @DeleteMapping("/cart/remove/{productId}")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(@PathVariable int productId, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Not logged in"));
        }

        User user = userRepo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "User not found"));
        }

        Cart cartItem = cartRepo.findProductByUserIdAndProductId(user.getUserId(), productId);

        if (cartItem != null) {
            cartRepo.delete(cartItem);
            return ResponseEntity.ok(Map.of("message", "Item removed successfully"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "Item not found in cart"));
        }
    }


}
