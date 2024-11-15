package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    // menuId로 MenuImage 조회
    @Select("SELECT * FROM menuimage WHERE menu_no = #{menuNo}")
    MenuImage selectMenuImageByMenuId(String menuId);

    void deleteMenuImageByMenuNo(int menuNo);

    void updateMenuImage(MenuImage menuImageObj);
}
