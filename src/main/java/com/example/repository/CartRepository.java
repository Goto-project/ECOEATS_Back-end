package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Cart;

@Repository
public interface CartRepository  extends JpaRepository<Cart,Integer>{
    
    // 주문번호와 메뉴번호가 일치하는 카트 항목을 조회하는 메서드
    // Optional<Cart> findByDailymenuNoAndOrderno(DailyMenu dailymenuNo, Order order);
}

