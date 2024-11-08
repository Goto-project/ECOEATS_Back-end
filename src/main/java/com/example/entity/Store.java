package com.example.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
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

    @Column(name = "default_pickup")
    Date defaultPickup;
}
