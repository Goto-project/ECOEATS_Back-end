package com.example.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    int countByOrdernoStartingWith(String date);

    Order findByOrderno(String orderno);

     // 오늘 주문만 가져오는 메서드
    @Query("SELECT o FROM Order o WHERE o.regdate >= :todayStart AND o.regdate <= :todayEnd")
        List<Order> findOrdersForToday(LocalDateTime todayStart, LocalDateTime todayEnd);

        List<Order> findByStoreid_StoreIdAndRegdateBetween(String storeId, LocalDateTime startOfDay, LocalDateTime endOfDay);
        
        List<Order> findByStoreid_StoreId(String storeId);


    
}
