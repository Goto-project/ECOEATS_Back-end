package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "order")
@Data
public class Order {
    
    @Id
    int orderno;

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    Store storeId;

    @ManyToOne
    @JoinColumn(name = "customer_email" , referencedColumnName = "customer_email")
    CustomerMember customerEmail;

    @ManyToOne
    @JoinColumn(name = "dailymenu_no" , referencedColumnName = "dailymenu_no")
    DailyMenu dailymenuNo;

    int qty;
    

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @CreationTimestamp
    Date regdate;
}
