package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.MenuImage;

@Repository
public interface MenuImageRepository extends JpaRepository<MenuImage , Integer>{
    
    MenuImage findByMenu_menuNo(int menuNo);
}
