package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.FreeBoard1;

@Repository
public interface FreeBoard1Repository extends JpaRepository<FreeBoard1, Integer> {
    
}
