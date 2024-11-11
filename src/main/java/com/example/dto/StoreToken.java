package com.example.dto;

import java.util.Date;

import lombok.Data;

@Data
public class StoreToken {
    int no;
    String id;
    String token;
    Date expiretime;
    Date regdate;
}
