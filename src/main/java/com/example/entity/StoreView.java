package com.example.entity;

import java.time.LocalTime;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Immutable
@Table(name = "storedetailview")
@Data
public class StoreView {

    @Id
    String storeid;

    String storename;

    String address;

    String phone;

    String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime startpickup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    LocalTime endpickup;

    Double avgrating; // 평균 평점

    int bookmarkcount; // 북마크 수

    String filename; // 가게 이미지 파일명
    
}
