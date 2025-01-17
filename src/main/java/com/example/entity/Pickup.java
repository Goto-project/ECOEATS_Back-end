package com.example.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "pickup")
@Data
public class Pickup {
    
    @Id
    @Column(name = "pickup_no")
    int pickupNo;

    @ManyToOne
    @JoinColumn(name = "orderno" , referencedColumnName = "orderno")
    Order orderno;

    int pickup;

    @CreationTimestamp
    private LocalDateTime regdate;
}
