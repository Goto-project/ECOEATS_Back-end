package com.example.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Transient;
import lombok.Data;

@Data
public class Store {
    String storeId;
    String storeEmail;
    String password;
    String storeName;
    String address;
    String phone;
    String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startPickup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime endPickup;
    
    BigDecimal latitude;  // 위도
    BigDecimal longitude; // 경도

    @Transient
    String role = "SELLER";
}
