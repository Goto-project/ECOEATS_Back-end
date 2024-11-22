package com.example.entity;

import java.time.LocalDate;
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
@Data
@Table(name = "status")
public class Status {
    
    @Id
    int no;

    @ManyToOne
    @JoinColumn(name = "orderno", referencedColumnName = "orderno")
    Order orderno;

    String status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:ss.SSS")
    @CreationTimestamp
    Date regdate;


}
