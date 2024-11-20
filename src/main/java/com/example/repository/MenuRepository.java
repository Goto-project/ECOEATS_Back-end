package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Menu;


public interface MenuRepository extends JpaRepository<Menu, Integer>{

    List<Menu> findByStoreId_StoreId(String storeId);
}
