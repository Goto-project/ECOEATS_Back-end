package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
@Table(name = "store")
public class Store {
    
    @Id
    @Column(name = "store_id")
    String storeId;

    @Column(name = "store_email")
    String storeEmail;

    String password;

    @Column(name = "store_name")
    String storeName;

    String address;

    String phone;

    String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Column(name = "start_pickup")
    LocalTime startPickup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Column(name = "end_pickup")
    LocalTime endPickup;

    BigDecimal latitude;  // 위도
    BigDecimal longitude; // 경도

    @Transient
    String role = "SELLER";
}
