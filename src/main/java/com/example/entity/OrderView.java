package com.example.entity;

import java.time.LocalDateTime;
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
@Table(name = "orderview")
public class OrderView {
    
    @Id
    int no;
    
    String ordernumber;

    int paymentstatus;

    int totalprice;

    String customeremail;

    String storeid; 

    String orderstatus;

    String storename;

    int dailymenuno;

    LocalDateTime ordertime;

    String menuname;

    int dailymenuprice;

    int quantity;

    int unitprice;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startpickup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime endpickup;
    
    int pickupstatus;

    LocalDateTime  pickuptime;

}

