package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.BookMark;
import com.example.entity.CustomerMember;
import com.example.entity.Store;

@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Integer>{
    BookMark findByCustomerAndStore(CustomerMember customer, Store store);  // 고객과 상점으로 즐겨찾기 찾기
    
}
