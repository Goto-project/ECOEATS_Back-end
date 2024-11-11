package com.example.entity;

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
    @Column(name = "default_pickup")
    LocalTime defaultPickup;

    @Transient
    String role = "SELLER";
}
