package com.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
@Table(name = "admin")
public class Admin {


    @Id
    @Column(name = "admin_id")
    String adminId;
    
    String password;

    @Transient
    String role = "ADMIN";
}
