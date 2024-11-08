package com.example.repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.FreeBoardReply1;

@Repository
public interface FreeBoardReply1Repository extends CrudRepository<FreeBoardReply1, Integer>{
    //insert, update, delete, selectall은 제공


    // 외래키 정보를 조회할 때는 findBy객체_하위변수
    // 해당하는 게시글의 모든 답글 목록
    //SELECT * FROM FreeBoardReply1 WHERE bno=#{bno}

    //엔티티는 변수에서 _ 사용불가
    // int bno_no => bnoNO
    List<FreeBoardReply1> findByBno_no(int no);



}
