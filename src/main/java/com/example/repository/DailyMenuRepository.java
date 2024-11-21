package com.example.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.DailyMenu;

@Repository
public interface DailyMenuRepository extends JpaRepository<DailyMenu, Integer>{
    
    // 특정 날짜에 해당하는 데일리 메뉴 리스트 가져오기
    List<DailyMenu> findByRegdate(LocalDate date);
}
