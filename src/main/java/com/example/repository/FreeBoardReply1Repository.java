package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.FreeBoardReply1;

@Repository
public interface FreeBoardReply1Repository extends JpaRepository<FreeBoardReply1, Integer>{
    //insert, update, delete, selectall은 제공
}
