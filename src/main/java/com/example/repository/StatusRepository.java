package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Order;
import com.example.entity.Status;

@Repository
public interface StatusRepository extends JpaRepository<Status, Integer>{
    
    // 특정 주문의 최신 상태를 가져옴
    Optional<Status> findTopByOrdernoOrderByRegdateDesc(Order orderno);
}
