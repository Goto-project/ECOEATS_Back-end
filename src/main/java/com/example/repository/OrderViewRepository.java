package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.OrderView;

@Repository
public interface OrderViewRepository extends JpaRepository<OrderView, String> {
    
     // 고객 이메일을 기준으로 주문 내역 조회
     List<OrderView> findByCustomeremail(String customerEmail);

     // 가게 ID를 기준으로 주문 내역 조회
     List<OrderView> findByStoreid(String storeId);
}
