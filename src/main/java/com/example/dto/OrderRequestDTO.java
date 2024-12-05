package com.example.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderRequestDTO {
    int pay;
    List<CartRequestDTO> cartRequests;
    String storeid;
}
