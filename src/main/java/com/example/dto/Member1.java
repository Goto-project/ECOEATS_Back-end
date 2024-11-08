package com.example.dto;

import java.util.Date;

import lombok.Data;

//mybatis에서 테이블과 매칭되는 object는 DTO라고 하고 역할이 없음
//mybatis는 SQL문 기반으로 DB와 연동하기 떄문
@Data
public class Member1 {
    String id;
    String pw;
    String name;
    String phone;
    int age;
    Date regdate;
    String role;
}
