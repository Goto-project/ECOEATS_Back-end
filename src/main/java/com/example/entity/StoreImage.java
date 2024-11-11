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
@Table(name = "storeimage")
public class StoreImage {
    
    @Id
    @Column(name = "storeimage_no")
    int storeimageNo;

    String filename;
    String filetype;
    long filesize;
    byte[] filedata;

    @OneToOne
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    @JsonProperty(access = Access.WRITE_ONLY)
    Store storeId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:ss.SSS")
    @CreationTimestamp
    Date regdate;
}
