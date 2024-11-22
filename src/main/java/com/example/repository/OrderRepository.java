package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.entity.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    // String 으로 변환하여 쿼리 처리
    @Query(value = "SELECT COUNT(*) FROM order1 o WHERE CAST(o.orderno AS CHAR) LIKE CONCAT(:date, '%')", nativeQuery = true)
    int countByOrdernoStartingWith(@Param("date") String date);
}
