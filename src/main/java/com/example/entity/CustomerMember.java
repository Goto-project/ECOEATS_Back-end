package com.example.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = " customer_member")
public class CustomerMember {

    @Id
    @Column(name = " customer_email")
    String customerEmail ;

    String password;

    String nickname;

    String phone;

    @Transient
    String role = "CUSTOMER";


}
