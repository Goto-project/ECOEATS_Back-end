package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.OrderView;

@Repository
public interface OrderViewRepository extends JpaRepository<OrderView, Integer> {
    
    //customerEmail을 기준으로 데이터 필터링
    List<OrderView> findByCustomeremail(String customerEmail);

    List<OrderView> findByStoreid(String storeId);
}
