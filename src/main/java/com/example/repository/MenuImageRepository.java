package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.MenuImage;

public interface MenuImageRepository extends JpaRepository<MenuImage , Integer>{
    
    MenuImage findByMenu_menuNo(int menuNo);
}
