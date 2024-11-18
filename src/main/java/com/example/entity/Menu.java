package com.example.entity;

import org.hibernate.annotations.ManyToAny;

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
@Table(name = "menu")
@Data
public class Menu {
    
    @Id
    @Column(name = "menu_no")
    int menuNo;

    @ManyToOne
    @JoinColumn(name = "store_id" , referencedColumnName ="store_id")
    @JsonProperty(access = Access.WRITE_ONLY) //추가할때는 사용 가능 , 조회할때는 표시
    Store storeId;

    String name;

    int price;
}
