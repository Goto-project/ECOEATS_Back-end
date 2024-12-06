package com.example.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.DailyMenu;
import com.example.entity.Store;

@Repository
public interface DailyMenuRepository extends JpaRepository<DailyMenu, Integer>{
    // Menu 엔티티를 통해 Store를 찾도록 수정
    List<DailyMenu> findByMenuNoStoreIdAndRegdate(Store store, LocalDate regdate);

    List<DailyMenu> findByMenuNoStoreIdAndRegdateAndMenuNoIsdeletedFalse(Store store, LocalDate date);
}
