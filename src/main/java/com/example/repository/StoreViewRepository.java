package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.StoreView;

@Repository
public interface StoreViewRepository extends JpaRepository<StoreView, String>{
    
    //별점순 조회
}
