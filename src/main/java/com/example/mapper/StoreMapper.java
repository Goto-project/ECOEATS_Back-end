package com.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.example.dto.Store;

@Mapper
public interface StoreMapper {

        // 회원가입 INSERT, UPDATE, DELETE는 반환값이 int로 고정됨
        @Insert({ "INSERT INTO store(store_id, store_email, password, store_name, address, phone, category, start_pickup, end_pickup)",
                        " VALUES(#{storeId}, #{storeEmail}, #{password}, #{storeName}, #{address}, #{phone}, #{category}, #{startPickup}, #{endPickup})" })
        public int insertStoreOne(Store store);

        // 로그인 처리 -> 아이디를 전달받으면 해당하는 정보를 반환
        // 아이디, 암호, 권한
        @Select({ "SELECT * FROM store WHERE store_id=#{storeId}" })
        @Results({
                        @Result(property = "storeId", column = "store_id"),
                        @Result(property = "storeEmail", column = "store_email"),
                        @Result(property = "storeName", column = "store_name"),
                        @Result(property = "startPickup", column = "start_pickup"),
                        @Result(property = "endPickup", column = "end_pickup")
        })
        public Store selectStoreOne(String storeId);

        @Insert({
                        "<script>",
                        "INSERT INTO store(store_id, store_email, password, store_name, address, phone, category, start_pickup, end_pickup) ",
                        "VALUES(#{storeId}, #{storeEmail}, #{password}, #{storeName}, #{address}, #{phone}, #{category}, #{startPickup}, #{endPickup}) ",
                        "ON DUPLICATE KEY UPDATE ",
                        "<if test='storeEmail != null'>store_email = #{storeEmail},</if>",
                        "<if test='password != null'>password = #{password},</if>",
                        "<if test='storeName != null'>store_name = #{storeName},</if>",
                        "<if test='address != null'>address = #{address},</if>",
                        "<if test='phone != null'>phone = #{phone},</if>",
                        "<if test='category != null'>category = #{category},</if>",
                        "<if test='startPickup != null'>start_pickup = #{startPickup},</if>",
                        "<if test='endPickup != null'>end_pickup = #{endPickup}</if>",
                        "</script>"
        })
        int updateStore(Store store);

        @Delete({"DELETE FROM store WHERE store_id = #{storeId}"})
        int deleteStore(String storeId);
}