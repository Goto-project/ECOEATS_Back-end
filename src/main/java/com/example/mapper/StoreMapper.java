package com.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.dto.Store;

@Mapper
public interface StoreMapper {

        // 회원가입 INSERT, UPDATE, DELETE는 반환값이 int로 고정됨
        @Insert({ "INSERT INTO store(store_id, store_email, password, store_name, address, phone, category, start_pickup, end_pickup, latitude, longitude)",
                        " VALUES(#{storeId}, #{storeEmail}, #{password}, #{storeName}, #{address}, #{phone}, #{category}, #{startPickup}, #{endPickup}, #{latitude}, #{longitude})" })
        public int insertStoreOne(Store store);

        // 아이디 중복 체크 메서드
        @Select("SELECT COUNT(*) FROM store WHERE store_id = #{storeId}")
        int checkStoreIdExists(String storeId);

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

        @Select({ "SELECT * FROM store WHERE store_id = #{storeId} AND store_email = #{storeEmail}" })
        @Results({
                        @Result(property = "storeId", column = "store_id"),
                        @Result(property = "storeEmail", column = "store_email"),
                        @Result(property = "storeName", column = "store_name"),
                        @Result(property = "startPickup", column = "start_pickup"),
                        @Result(property = "endPickup", column = "end_pickup")
        })
        Store findStoreByIdAndEmail(String storeId, String storeEmail);

        @Update("UPDATE store SET store_email = #{storeEmail}, store_name = #{storeName}, address = #{address}, phone = #{phone}, category = #{category}, start_pickup = #{startPickup}, end_pickup = #{endPickup} , latitude = #{latitude}, longitude = #{longitude} WHERE store_id = #{storeId}")
        @Results({
                        @Result(property = "storeId", column = "store_id"),
                        @Result(property = "storeEmail", column = "store_email"),
                        @Result(property = "storeName", column = "store_name"),
                        @Result(property = "startPickup", column = "start_pickup"),
                        @Result(property = "endPickup", column = "end_pickup"),
                        @Result(property = "latitude", column = "latitude"),
                        @Result(property = "longitude", column = "longitude")

        })
        int updateStore(Store store);

        @Update("UPDATE store SET password = #{password} WHERE store_id = #{storeId}")
        int updatePassword(Store store);

        @Delete({ "DELETE FROM store WHERE store_id = #{storeId}" })
        int deleteStore(String storeId);
}