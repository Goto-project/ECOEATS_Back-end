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

@Entity
@Table(name = "order1")
@Data
public class Order {
    
    @Id
    int orderno;

    @ManyToOne
    @JoinColumn(name = "cartno", referencedColumnName = "no")
    Cart cartno;

    int pay;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @CreationTimestamp
    Date regdate;
}
