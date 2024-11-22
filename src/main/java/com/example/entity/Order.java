package com.example.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

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
    Long orderno;

    @Column(columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime regdate;
    int pay;

    String status;

    int totalprice;

    @ManyToOne
    @JoinColumn(name = "customeremail", referencedColumnName = "customer_email")
    CustomerMember customeremail;

    @ManyToOne
    @JoinColumn(name = "storeid", referencedColumnName = "store_id")
    Store storeid;
    
}

