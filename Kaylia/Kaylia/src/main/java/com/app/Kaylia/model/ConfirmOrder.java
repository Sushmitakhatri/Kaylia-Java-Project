package com.app.Kaylia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int userId;
    private String email;
    private String address;
    private String city;
    private String state;
    private String contactNo;
    private String paymentMethod;
    private LocalDateTime timeOrdered;
    @Column(nullable = true)
    private double totalAmount;

    @ElementCollection
    @CollectionTable(name = "confirmed_order_products", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "product_id")
    private List<Long> productIds; // use Integer for productId
}
