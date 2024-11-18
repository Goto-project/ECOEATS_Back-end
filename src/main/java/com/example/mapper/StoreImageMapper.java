package com.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

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
        @Results({
                        @Result(property = "storeId", column = "store_id"),
                        @Result(property = "storeimageNo", column = "storeimage_no")
        })
        StoreImage selectStoreImageByStoreId(String storeId);

        // 이미지 삭제
        @Delete("DELETE FROM storeimage WHERE store_id = #{storeId}")
        void deleteStoreImage(String storeId);

        @Update("UPDATE storeimage SET filename = #{filename}, filetype = #{filetype}, filesize = #{filesize}, filedata = #{filedata} WHERE store_id = #{storeId}")
        int updateStoreImage(StoreImage storeImage);
}
