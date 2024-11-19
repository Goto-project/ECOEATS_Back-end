package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
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
    int no;

    @ManyToOne
    @JoinColumn(name = "dailymenuno", referencedColumnName = "dailymenu_no")
    DailyMenu dailymenuNo;

    int qty;

    @ManyToOne
    @JoinColumn(name = "customeremail" , referencedColumnName = "customer_email")
    CustomerMember customerEmail;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:ss.SSS")
    @CreationTimestamp
    Date regdate;
}
