package com.example.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cart")
public class Cart {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // id 값 자동 생성
    int no;

    @ManyToOne
    @JoinColumn(name = "dailymenuno", referencedColumnName = "dailymenu_no")
    DailyMenu dailymenuNo;

    int qty;

    int price;

    @ManyToOne
    @JoinColumn(name = "orderno", referencedColumnName = "orderno")
    Order orderno;
}

