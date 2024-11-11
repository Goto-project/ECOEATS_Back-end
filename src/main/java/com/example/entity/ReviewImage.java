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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "reviewimage")
public class ReviewImage {
    
    @Id
    @Column(name = "reviewimage_no")
    int reviewimageNo;

    String filename;
    String filetype;
    long filesize;
    byte[] filedata;

    @OneToOne
    @JoinColumn(name = "reviewno", referencedColumnName = "review_no")
    @JsonProperty(access = Access.WRITE_ONLY)
    Review reviewno;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:ss.SSS")
    @CreationTimestamp
    Date regdate;
}
