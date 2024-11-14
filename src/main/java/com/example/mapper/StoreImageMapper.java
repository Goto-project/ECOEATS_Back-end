package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;

import com.example.dto.StoreImage;

public interface StoreImageMapper {
    
    // StoreImage 등록
    @Insert("INSERT INTO storeimage (filename, filetype, filesize, filedata, store_id) " +
            "VALUES (#{filename}, #{filetype}, #{filesize}, #{filedata}, #{storeId})")
    void insertStoreImage(StoreImage storeImage);

    // storeimage_no로 StoreImage 조회
    @Select("SELECT * FROM storeimage WHERE storeimage_no = #{storeimageNo}")
    StoreImage selectStoreImageById(int storeimageNo);

    // storeId로 StoreImage 조회
    @Select("SELECT * FROM storeimage WHERE store_id = #{storeId}")
    StoreImage selectStoreImageByStoreId(String storeId);
}
