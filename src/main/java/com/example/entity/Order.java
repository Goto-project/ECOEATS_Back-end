package com.example.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
    String orderno;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime regdate;
    int pay;

    String tid; // 카카오페이 TID 추가

    int totalprice;

    @ManyToOne
    @JoinColumn(name = "customeremail", referencedColumnName = "customer_email")
    CustomerMember customeremail;

    @ManyToOne
    @JoinColumn(name = "storeid", referencedColumnName = "store_id")
    Store storeid;
    
}

