package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.BookMark;


@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Integer>{
    List<BookMark> findByBookmarkNo(int bookmarkNo);
    List<BookMark> findByCustomerEmail_CustomerEmailAndStoreId_StoreId(String customerEmail, String storeId);
}
