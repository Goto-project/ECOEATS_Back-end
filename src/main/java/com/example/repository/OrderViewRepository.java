package com.example.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.OrderView;

@Repository
public interface OrderViewRepository extends JpaRepository<OrderView, Integer> {

     // 고객 이메일을 기준으로 주문 내역 조회
     List<OrderView> findByCustomeremail(String customerEmail);

     // 가게 ID를 기준으로 주문 내역 조회
     List<OrderView> findByStoreid(String storeId);

     List<OrderView> findByCustomeremailAndOrdernumber(String customeremail, String ordernumber);

     List<OrderView> findByCustomeremailAndOrderstatus(String customeremail, String ordernumber);

     public List<OrderView> findByCustomeremailAndOrdertimeBetween(String customerEmail, LocalDateTime start,
               LocalDateTime end);
}
