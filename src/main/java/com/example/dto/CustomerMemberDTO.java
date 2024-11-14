package com.example.dto;

import lombok.Data;

@Data
public class CustomerMemberDTO {
    
    String customerEmail;

    String password;

    String nickname;

    String phone;

    String role = "CUSTOMER";

}
