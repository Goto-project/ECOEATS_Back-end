package com.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Update;

import com.example.dto.Menu;
import com.example.dto.MenuImage;

import java.util.List;

@Mapper
public interface MenuMapper {

    // 메뉴 추가
    @Insert("INSERT INTO menu(store_id, name, price) VALUES(#{storeId}, #{name}, #{price})")
    @Options(useGeneratedKeys = true, keyProperty = "menuNo") // 자동 증가된 키를 menuNo에 설정
    int insertMenu(Menu menu);

    // 메뉴 전체 조회
    @Select("SELECT * FROM menu")
    @Results({
            @Result(property = "menuNo", column = "menu_no"),
            @Result(property = "storeId", column = "store_id"),
            @Result(property = "name", column = "name"),
            @Result(property = "price", column = "price")
    })
    List<Menu> selectAllMenus(); // 전체 메뉴 리스트 조회

    // 메뉴 수정
    @Update("UPDATE menu SET store_id = #{storeId}, name = #{name}, price = #{price} WHERE menu_no = #{menuNo}")
    int updateMenu(Menu menu);

    // 메뉴 이미지 수정
    @Update("UPDATE menu_images SET filename = #{filename}, filetype = #{filetype}, filesize = #{filesize}, filedata = #{filedata}, regdate = #{regdate} WHERE menu_no = #{menuNo}")
    int updateMenuImage(MenuImage menuImage);

    // 메뉴 삭제
    @Delete("DELETE FROM menu WHERE menu_no = #{menuNo}")
    int deleteMenu(int menuNo);

    // 메뉴 이미지 삭제 (선택 사항)
    @Delete("DELETE FROM menu_images WHERE menu_no = #{menuNo}")
    int deleteMenuImage(int menuNo);

    void updateMenuName(int menuNo, String name);

    void updateMenuPrice(int menuNo, Integer price);
}

