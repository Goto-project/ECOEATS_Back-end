package com.example.mapper;

import org.apache.ibatis.annotations.*;
import com.example.dto.MenuImageDTO;

@Mapper
public interface MenuImageMapper {

    // 메뉴 이미지 등록
    @Insert("INSERT INTO menuimage (menu_no, filename, filetype, filesize, filedata) " +
            "VALUES (#{menuNo}, #{filename}, #{filetype}, #{filesize}, #{filedata})")
    int insertMenuImage(MenuImageDTO menuImage);

    // menuimage_no로 메뉴 이미지 조회
    @Select("SELECT * FROM menuimage WHERE menuimage_no = #{menuimageNo}")
    MenuImageDTO selectMenuImageById(int menuimageNo);

    // menuNo로 메뉴 이미지 조회
    @Select("SELECT * FROM menuimage WHERE menu_no = #{menuNo}")
    MenuImageDTO selectMenuImageByMenuNo(int menuNo);  // menuNo로 메뉴 이미지 조회

    // 메뉴 이미지 삭제
    @Delete("DELETE FROM menuimage WHERE menu_no = #{menuNo}")
    int deleteMenuImageByMenuNo(int menuNo);  // 메뉴 번호로 이미지 삭제

    // 메뉴 이미지 수정
    @Update("UPDATE menuimage SET filename = #{filename}, filetype = #{filetype}, filesize = #{filesize}, filedata = #{filedata}, regdate = #{regdate} WHERE menuimage_no = #{menuimageNo}")
    void updateMenuImage(MenuImageDTO menuImage);
}
