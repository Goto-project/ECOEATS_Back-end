package com.example.dto;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.Data;

@Data
@Entity
public class Store {
    
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
    private LocalTime endPickup;

    @Transient  // 이 필드는 DB에 저장되지 않도록
    private String role = "SELLER";  // 기본값 설정
}
