package com.example.dto;

import jakarta.persistence.Transient;
import lombok.Data;

@Data
public class CustomerMember {
    
    String customerEmail;

    String password;

    String nickname;

    String phone;

    @Transient
    String role = "CUSTOMER";

}
