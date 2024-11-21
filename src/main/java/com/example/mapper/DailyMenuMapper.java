package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.dto.DailyMenuDTO;
import com.example.entity.DailyMenu;

@Mapper
public interface DailyMenuMapper {
    // 당일 메뉴 추가
    @Insert("INSERT INTO dailymenu (menu_no) VALUES (#{menuNo})")
    int insertDailyMenu(DailyMenuDTO dailyMenuDTO);

    // 메뉴 번호로 당일 메뉴 조회
    @Select("SELECT * FROM dailymenu WHERE menu_no = #{menuNo}")
    DailyMenu selectByMenuNo(int MenuNo);
}
