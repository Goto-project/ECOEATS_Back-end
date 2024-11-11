package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

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

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @CreationTimestamp
    @Column(name = "default_pickup")
    private Date defaultPickup;

    
    @Transient
    String role = "SELLER";
}
