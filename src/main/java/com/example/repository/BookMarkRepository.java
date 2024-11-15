package com.example.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.BookMark;
import com.example.entity.CustomerMember;


@Repository
public interface BookMarkRepository extends JpaRepository<BookMark, Integer>{
    List<BookMark> findByBookmarkNo(int bookmarkNo);
}
