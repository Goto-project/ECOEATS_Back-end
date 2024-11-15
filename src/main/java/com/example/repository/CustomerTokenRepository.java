package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.CustomerToken;

public interface CustomerTokenRepository extends JpaRepository<CustomerToken,Integer>{
    
    @Transactional
    @Modifying
    void deleteByToken(String token);
}
