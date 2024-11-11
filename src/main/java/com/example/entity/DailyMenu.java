package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "dailymenu")
@Data
public class DailyMenu {


    
    @Id
    @Column(name = "dailymenu_no")
    int dailymenuNo;


    @ManyToOne
    @JoinColumn(name = "menu_no" , referencedColumnName = "menu_no")
    @JsonProperty(access = Access.WRITE_ONLY)
    Menu menuNol;

    int price;


    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @CreationTimestamp
    @Column(name = "pickup_time")
    private Date pickupTime;

    
    int qty;


    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @CreationTimestamp
    private Date regdate;
}
