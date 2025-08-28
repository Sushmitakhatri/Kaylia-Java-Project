package com.app.Kaylia.dto;

import com.app.Kaylia.model.Products;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDisplayDto {

    private int orderId;
    private LocalDateTime timeOrdered;
    private double totalAmount;
    private List<Products> products;

}
