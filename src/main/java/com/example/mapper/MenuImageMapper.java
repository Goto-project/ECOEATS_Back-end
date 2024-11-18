package com.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.dto.MenuImage;

@Mapper
public interface MenuImageMapper {

    // MenuImage 등록
    @Insert("INSERT INTO menuimage (menu_no, filename, filetype, filesize, filedata) " +
            "VALUES (#{menuNo}, #{filename}, #{filetype}, #{filesize}, #{filedata})")
    int insertMenuImage(MenuImage menuImage);

    // menuimage_no로 MenuImage 조회
    @Select("SELECT * FROM menuimage WHERE menuimage_no = #{menuimageNo}")
    MenuImage selectMenuImageById(int menuimageNo);

    // menuNo로 MenuImage 조회
    @Select("SELECT * FROM menuimage WHERE menu_no = #{menuNo}")
    MenuImage selectMenuImageByMenuId(int menuNo);

    // menuNo로 MenuImage 삭제
    @Delete("DELETE FROM menuimage WHERE menu_no = #{menuNo}")
    int deleteMenuImageByMenuNo(int menuNo);  // 삭제된 행 수를 반환

    // 메뉴 이미지 수정
    @Update("UPDATE menuimage SET filename = #{filename}, filetype = #{filetype}, filesize = #{filesize}, filedata = #{filedata}, regdate = #{regdate} WHERE menuimage_no = #{menuimageNo}")
    void updateMenuImage(MenuImage menuImage);
}

