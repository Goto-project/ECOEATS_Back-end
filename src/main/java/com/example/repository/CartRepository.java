package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Cart;


public interface CartRepository  extends JpaRepository<Cart,Integer>{
    
    //특정 이메일의 장바구니 리스트 조회
    // List<Cart> findByCustomerEmail_CustomerEmail(String customerEmail);
}

