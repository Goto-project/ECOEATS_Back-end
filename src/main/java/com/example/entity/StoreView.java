package com.example.entity;

import java.time.LocalTime;
import java.util.Date;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Immutable
@Table(name = "store_detail_view")
@Data
public class StoreView {

    @Id
    @Column(name = "store_id")
    String storeId;


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

    @Transient
    String role = "SELLER";
    
}
