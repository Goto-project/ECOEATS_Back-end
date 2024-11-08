package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "storeimage")
public class StoreImage {
    
    @Id
    @Column(name = "storeimage_no")
    int storeimageNo;

    String filename;
    String filetype;
    long filesize;
    byte[] filedata;

    @Column(name = "store_id")
    String storeId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:ss.SSS")
    @CreationTimestamp
    Date regdate;
}
