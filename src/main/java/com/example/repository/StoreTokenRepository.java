package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.StoreToken;

@Repository
public interface StoreTokenRepository extends JpaRepository<StoreToken, Integer>{
    
    @Transactional
    @Modifying
    int deleteById_StoreId(String storeId);
}
