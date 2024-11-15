package com.example.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;

@Data
public class Menu {

    private int menuNo;

    @JsonProperty(access = Access.WRITE_ONLY) // Excluding storeId from being serialized (write-only)
    private String storeId;

    private String name;

    // price를 Integer로 변경하여 null 값을 처리할 수 있게 함
    private Integer price;

    // 수정된 setMenuNo 메서드
    public void setMenuNo(int menuNo) {
        this.menuNo = menuNo; // 메뉴 번호를 설정
    }
}
