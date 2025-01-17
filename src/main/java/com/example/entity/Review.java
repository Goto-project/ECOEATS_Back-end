package com.example.entity;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Data
@Table(name = "review")
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_no")
    int reviewNo;

    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    @JsonProperty(access = Access.WRITE_ONLY)
    Store storeId;

    @ManyToOne
    @JoinColumn(name = "customer_email", referencedColumnName = "customer_email")
    @JsonProperty(access = Access.WRITE_ONLY)
    CustomerMember customerEmail;

    @ManyToOne
    @JoinColumn(name = "orderno", referencedColumnName = "orderno")
    @JsonProperty(access = Access.WRITE_ONLY)
    Order orderno;

    double rating;
    String content;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:MM:ss.SSS")
    @CreationTimestamp
    Date regdate;

    @Transient
    String imageurl="/ROOT/store/imagereview?no=";

    // storeName을 임시로 저장할 필드
    @Transient
    private String storeName;

    @Transient
    private String nickname;

    @Transient
    private String menu;
}
