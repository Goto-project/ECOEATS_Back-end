package com.example.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.DailyMenu;
import com.example.entity.Store;

@Repository
public interface DailyMenuRepository extends JpaRepository<DailyMenu, Integer>{
    
    List<DailyMenu> findByStoreAndRegdate(Store store, LocalDate regdate);
}
