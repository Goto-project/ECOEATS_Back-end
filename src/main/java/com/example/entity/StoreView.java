package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalTime;

import org.hibernate.annotations.Immutable;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Immutable
@Table(name = "storedetailview")
@Data
public class StoreView {

    @Id
    String storeid;

    String storeemail;

    @Column(name = "storename")
    String storeName;

    String address;

    BigDecimal latitude;  // 위도
    
    BigDecimal longitude; // 경도

    String phone;

    String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Column(name = "startpickup")
    LocalTime startPickup;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    @Column(name = "endpickup")
    LocalTime endPickup;

    Double avgrating; // 평균 평점

    Integer bookmarkcount; // 북마크 수

    Integer reviewcount; // 리뷰 수

    Integer storeimageno;

    @Transient
    String imageurl="/ROOT/store/image?no=";

    boolean isdeleted;
}
