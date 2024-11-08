package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Menu;

public interface MenuRepository extends JpaRepository<Menu, Integer>{
    
}
