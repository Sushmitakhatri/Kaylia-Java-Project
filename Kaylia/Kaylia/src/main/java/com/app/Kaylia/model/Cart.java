package com.app.Kaylia.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int cartId;

    private Long productId;


    @Column(nullable = true)
    private String productName;

    @Column(nullable = true)
    private double productPrice;

    private int userId;

    @Column(nullable = true)
    private int quantity;
}
