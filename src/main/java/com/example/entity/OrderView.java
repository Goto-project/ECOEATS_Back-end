package com.example.entity;

import java.time.LocalTime;
import java.util.Date;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Immutable
@Table(name = "orderdetailview")
public class OrderView {
    
    @Id
    String orderno;

    Date orderdate;

    int pay;

    String status;

    int totalprice; // 총합

    String customeremail;

    String storeid;

    String storename;

    String menuname;

    int dailymenuprice; // 메뉴 단가

    int quantity;

    int unitprice; // 각 메뉴 단가 * 수량

    int pickup;

    Date pickupdate;


    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startpickup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime endpickup;
}

