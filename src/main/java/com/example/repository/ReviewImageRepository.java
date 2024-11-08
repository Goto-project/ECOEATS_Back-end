package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.ReviewImage;

@Repository
public interface ReviewImageRepository extends JpaRepository<ReviewImage, Integer>{
    
}
