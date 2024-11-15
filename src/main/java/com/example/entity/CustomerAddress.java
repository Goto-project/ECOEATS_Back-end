package com.example.entity;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "customeraddress")
public class CustomerAddress {

    @Id
    int no;

    String postcode;

    String address;

    String addressdetail;

    BigDecimal latitude;  // 위도

    BigDecimal longitude; // 경도
    

    @ManyToOne
    @JoinColumn(name = "customeremail", referencedColumnName = "customer_email")
    @JsonProperty(access = Access.WRITE_ONLY)
    CustomerMember customeremail;
    
}
