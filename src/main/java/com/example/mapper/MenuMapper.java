package com.example.mapper;

import org.apache.ibatis.annotations.*;
import com.example.dto.MenuDTO;
import com.example.dto.MenuImageDTO;
import java.util.List;

@Mapper
public interface MenuMapper {

        // 메뉴 추가
        @Insert("INSERT INTO menu(store_id, name, price) VALUES(#{storeId}, #{name}, #{price})")
        @Options(useGeneratedKeys = true, keyProperty = "menuNo")
        int insertMenu(MenuDTO menu);

        // 메뉴와 이미지 정보 함께 조회
        @Select("SELECT m.menu_no, m.store_id, m.name, m.price, " +
                        "mi.menuimage_no, mi.filename, mi.filetype, mi.filesize, mi.regdate " +
                        "FROM menu m LEFT JOIN menuimage mi ON m.menu_no = mi.menu_no WHERE m.store_id = #{storeId}")
        @Results({
                        @Result(property = "menuNo", column = "menu_no"),
                        @Result(property = "storeId", column = "store_id"),
                        @Result(property = "name", column = "name"),
                        @Result(property = "price", column = "price"),
                        @Result(property = "menuImage", column = "menu_no", many = @Many(select = "selectMenuImageByMenuNo")) // 메뉴
                                                                                                                              // 이미지
                                                                                                                              // 조회
        })
        List<MenuDTO> selectMenuListWithImages(String storeId);

        // 메뉴 번호로 메뉴 정보 조회
        @Select("SELECT * FROM menu WHERE menu_no = #{menuNO}")
        MenuDTO selectMenuByNo(int menuNo);

        // 메뉴 이미지 조회
        @Select("SELECT menu_no, filename, filetype, filesize, regdate FROM menuimage WHERE menu_no = #{menuNo}")
        List<MenuImageDTO> selectMenuImageByMenuNo(int menuNo); // menuNo로 메뉴 이미지 조회

        // 메뉴 수정
        @Update("UPDATE menu SET store_id = #{storeId}, name = #{name}, price = #{price} WHERE menu_no = #{menuNo}")
        int updateMenu(MenuDTO menu);

        // 메뉴 이미지 수정
        @Update("UPDATE menuimage SET filename = #{filename}, filetype = #{filetype}, filesize = #{filesize}, filedata = #{filedata}, regdate = #{regdate} WHERE menu_no = #{menuNo}")
        int updateMenuImage(MenuImageDTO menuImage);

        // 메뉴 삭제
        // @Delete("DELETE FROM menu WHERE menu_no = #{menuNo}")
        // int deleteMenu(int menuNo);

        @Update("UPDATE menu SET isdeleted = TRUE WHERE menu_no = #{menuNo}")
        int deleteMenu(int menuNo);

        // 메뉴 이미지 삭제
        @Delete("DELETE FROM menuimage WHERE menu_no = #{menuNo}")
        int deleteMenuImage(int menuNo);

        // 특정 가게의 메뉴 리스트 조회
        @Select("SELECT * FROM menu WHERE store_id = #{storeId}")
        List<MenuDTO> selectMenuList(String storeId);
}
