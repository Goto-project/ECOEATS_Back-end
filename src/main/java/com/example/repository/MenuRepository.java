package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Menu;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer>{

    List<Menu> findByStoreId_StoreId(String storeId);

    List<Menu> findByStoreId_StoreIdAndIsdeletedFalse(String storeId);
}
