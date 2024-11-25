package com.example.entity;

import java.time.LocalDate;

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
import lombok.Data;

@Entity
@Table(name = "dailymenu")
@Data
public class DailyMenu {

    @Id
    @Column(name = "dailymenu_no")
    int dailymenuNo;

    @ManyToOne
    @JoinColumn(name = "menu_no", referencedColumnName = "menu_no")
    @JsonProperty(access = Access.WRITE_ONLY)
    Menu menuNo;

    int price;

    int qty;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @CreationTimestamp
    LocalDate regdate;

    // 기본 생성자 (JPA 필수)
    public DailyMenu() {
    }

    // dailymenuNo를 매개변수로 받는 생성자 추가
    public DailyMenu(int dailymenuNo) {
        this.dailymenuNo = dailymenuNo;
    }
}
