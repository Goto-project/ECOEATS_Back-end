package com.example.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
public class StoreDTO {
    
    @Id  // storeId가 primary key로 사용된다면 이 어노테이션 추가
    private String storeId;

    private String storeEmail;
    private String password;
    private String storeName;
    private String address;
    private String phone;
    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startPickup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime endPickup;
    
    BigDecimal latitude;  // 위도
    BigDecimal longitude; // 경도

    @Transient
    String role = "SELLER";
}
