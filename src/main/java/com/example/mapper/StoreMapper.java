package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.dto.Store;

@Mapper
public interface StoreMapper {

    //회원가입 INSERT, UPDATE, DELETE는 반환값이 int로 고정됨
    @Insert({"INSERT INTO store(store_id, store_email, password, store_name, address, phone, category, default_pickup)",
            " VALUES(#{storeId}, #{storeEmail}, #{password}, #{storeName}, #{address}, #{phone}, #{category}, #{defaultPickup})"})
    public int insertStoreOne(Store store);

    //로그인 처리 -> 아이디를 전달받으면 해당하는 정보를 반환
    //아이디, 암호, 권한
    @Select({"SELECT store_Id, password FROM store WHERE store_id=#{storeId}"})
    public Store selectStoreOne(String storeId);
}