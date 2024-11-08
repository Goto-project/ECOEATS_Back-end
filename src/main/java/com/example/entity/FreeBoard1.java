package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "freeboard1")
// @Immutable => view일 경우에
public class FreeBoard1 {
    
    @Id
    @Column(name = "no", length = 11)
    int no;

    
    String title;
    String content;
    String writer;
    int hit;

    String filename; //파일명
    String filetype; //파일타입
    long filesize; //파일사이즈
    byte[] filedata; //파일데이터
    
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:ss.SSS")
    @CreationTimestamp
    Date regdate;

    //테이블에 컬럼이 없어도 됨. 오류가 아님.
    @Transient
    String temp1;
}
