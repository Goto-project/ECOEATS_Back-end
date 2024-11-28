package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.entity.CustomerMember;
import com.example.entity.CustomerToken;

@Repository
public interface CustomerTokenRepository extends JpaRepository<CustomerToken,Integer>{
    
    @Transactional
    @Modifying
    void deleteByToken(String token);

    Optional<CustomerToken> findById(CustomerMember customerMember);
}
