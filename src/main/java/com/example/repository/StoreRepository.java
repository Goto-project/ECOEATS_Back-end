package com.example.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Store;

@Repository
public interface StoreRepository extends JpaRepository<Store, String>{
    Store findByStoreId(String storeId);

    List<Store> findByStoreNameContainingOrderByStoreNameAsc(String title, Pageable pageable);

    //페이지네이션에서 사용할 전체 개수
    long countByStoreNameContaining(String storeName);
}
