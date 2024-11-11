package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.BookMark;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Integer>{
    
}
