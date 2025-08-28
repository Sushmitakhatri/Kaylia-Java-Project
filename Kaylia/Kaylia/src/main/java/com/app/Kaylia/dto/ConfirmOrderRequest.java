package com.app.Kaylia.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmOrderRequest {

    private String email;
    private String address;
    private String city;
    private String state;
    private String contactNo;
    private String payment;  // "card", "esewa", "khalti"
    private double totalAmount;

}
