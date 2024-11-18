package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.StoreImage;

@Repository
public interface StoreImageRepository extends JpaRepository<StoreImage, Integer>{
    
    StoreImage findByStoreId_StoreId(String storeId);
}
