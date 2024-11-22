package com.example.dto;

import lombok.Data;

@Data
public class CartRequestDTO {
    int dailymenuNo;
    int qty;
    int price; // 메뉴 단가
}
