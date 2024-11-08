package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "freeboardreply1")
public class FreeBoardReply1 {

    @Id
    @Column(name = "no") // 설정하지 않으면 변수명이 컬럼명이 됨. 변수명을 다르게 쓸 거면 반드시 써야 함. 보통 헷갈려서 하나로 통일하기 때문에 생략하는 것.
    int no;

    String content;
    String writer;

    // 게시판답글(N) ---- 게시글(1)
    @ManyToOne // 외래키 n(freeboardreply1):1(freeboard1)
    @JoinColumn(name = "bno", referencedColumnName = "no")
    @JsonProperty(access = Access.WRITE_ONLY) // 추가할 때는 사용 가능, 조회할 때는 표시
    // @ToString.Exclude
    FreeBoard1 bno;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:ss.SSS")
    @CreationTimestamp
    Date regdate;

}
