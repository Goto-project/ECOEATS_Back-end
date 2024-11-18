package com.example.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;

@Data
public class Menu {

    private int menuNo;

    @JsonProperty(access = Access.WRITE_ONLY) // Excluding storeId from being serialized (write-only)
    private String storeId;

    private String name;

    private Integer price;

    private List<MenuImage> menuImage; // 여러 이미지 처리


    @JsonIgnore // getImage 메서드를 직렬화에서 제외합니다.
    public String getImage() {
        throw new UnsupportedOperationException("Unimplemented method 'getImage'");
    }

    public void setMenuNo(int menuNo) {
        this.menuNo = menuNo; // 메뉴 번호를 설정
    }
}
