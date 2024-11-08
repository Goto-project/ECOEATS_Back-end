package com.example.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.FreeBoard1;
import com.example.entity.FreeBoard1Projection;

@Repository
public interface FreeBoard1Repository extends CrudRepository<FreeBoard1, Integer> {


    // SELECT * FROM freeboard1 WHERE no=#{no}
    FreeBoard1 findByNO(int no);

    // SELECT no,title FROM freeboard1 WHERE no=#{no}
    FreeBoard1Projection findAllByNo(int no);


    // findBy ... => SQL문으로 처리
    
}
