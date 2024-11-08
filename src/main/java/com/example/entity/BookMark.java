package com.example.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bookmark")
public class BookMark {

    @Id
    @Column(name = "bookmark_no")
    int bookmarkNo;
    

    @ManyToOne
    @JoinColumn(name = "customer_email", referencedColumnName = "customer_email")
    @JsonProperty(access = Access.WRITE_ONLY) //추가할때는 사용가능, 조회할때는 표시
    CustomerMember customerEmail;


    @ManyToOne
    @JoinColumn(name = "store_id", referencedColumnName = "store_id")
    @JsonProperty(access = Access.WRITE_ONLY)
    Store storeId;
}

