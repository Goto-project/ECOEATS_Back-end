package com.example.dto;

import jakarta.persistence.Transient;
import lombok.Data;

@Data
public class AdminDTO {
    String adminId;
    String password;
    @Transient
    String role = "ADMIN";
}
