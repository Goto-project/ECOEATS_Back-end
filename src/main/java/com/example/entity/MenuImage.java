package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "menuimage")
@Data
public class MenuImage {
    
    @Id
    @Column(name = "menuimage_no")
    int menuimageNo;

    String filename;

    String filetype;

    long filesize;

    byte[] filedata;

    @ManyToOne
    @JoinColumn(name = "menu_no" ,referencedColumnName="menu_no")
    Menu menuNo;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @CreationTimestamp
    Date regdate;
}
