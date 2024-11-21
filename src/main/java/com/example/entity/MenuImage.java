package com.example.entity;

import java.util.Date;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "menuimage")
@Data
public class MenuImage {

    @Id
    @Column(name = "menuimage_no")
    private int menuimageNo;  // 이미지 번호

    private String filename;  // 파일명

    private String filetype;  // 파일 형식

    private long filesize;  // 파일 크기

    private byte[] filedata;  // 파일 데이터 (이미지)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "menu_no", referencedColumnName = "menu_no") // menu_no와 연결
    private Menu menu;  // Menu 객체 참조

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @CreationTimestamp
    private Date regdate;  // 등록일
}
