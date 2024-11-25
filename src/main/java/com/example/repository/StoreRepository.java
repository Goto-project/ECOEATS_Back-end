package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, String>{
    Store findByStoreId(String storeId);
}
