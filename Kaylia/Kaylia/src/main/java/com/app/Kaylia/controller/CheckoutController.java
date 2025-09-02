package com.app.Kaylia.controller;

import com.app.Kaylia.dto.ConfirmOrderRequest;
import com.app.Kaylia.model.Cart;
import com.app.Kaylia.model.ConfirmOrder;
import com.app.Kaylia.model.User;
import com.app.Kaylia.repository.CartRepo;
import com.app.Kaylia.repository.ConfirmOrderRepo;
import com.app.Kaylia.repository.UserRepo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    @Autowired
    private ConfirmOrderRepo orderRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ConfirmOrderRepo confirmOrderRepo;

    @PostMapping("/confirm-order")
    @ResponseBody
    @Transactional
    public ResponseEntity<?> confirmOrder(@RequestBody ConfirmOrderRequest orderRequest,
                                          HttpSession session) {

        // get logged-in user
        String email = (String) session.getAttribute("email");
        User user = userRepo.findByEmail(email);
        if (user == null) {
            return ResponseEntity.status(401).body("User not logged in");
        }

        int userId = user.getUserId();

        // fetch products from cart
        List<Cart> cartItems = cartRepo.findByUserId(userId);
        if (cartItems.isEmpty()) {
            return ResponseEntity.badRequest().body("Cart is empty");
        }

        List<Long> productIds = cartItems.stream()
                .map(Cart::getProductId)
                .collect(Collectors.toList());

        // save confirmed order
        ConfirmOrder order = new ConfirmOrder();
        order.setUserId(userId);
        order.setEmail(orderRequest.getEmail());
        order.setAddress(orderRequest.getAddress());
        order.setCity(orderRequest.getCity());
        order.setState(orderRequest.getState());
        order.setContactNo(orderRequest.getContactNo());
        order.setPaymentMethod(orderRequest.getPayment());
        order.setProductIds(productIds);
        order.setTimeOrdered(LocalDateTime.now());
        order.setTotalAmount(orderRequest.getTotalAmount());

        ConfirmOrder savedOrder = orderRepo.save(order);

        // remove items from cart
        cartRepo.deleteByUserId(userId);

        return ResponseEntity.ok(savedOrder);
    }


}
