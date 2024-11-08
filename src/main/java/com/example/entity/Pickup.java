package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "pickup_pay")
public class Pickup {
    
    @Id
    @Column(name = "pickup_no")
    int pickupNo;

    @ManyToOne
    @JoinColumn(name = "orderno" , referencedColumnName = "orderno")
    Order orderno;

    int pickup;

    Date regdate;
}
