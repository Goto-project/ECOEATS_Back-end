package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.example.dto.StoreToken;
import com.example.dto.CustomerToken;
import com.example.dto.Token1;

@Mapper
public interface TokenMapper {

    @Insert({" INSERT INTO storetoken(id, token, expiretime)",
            " VALUES(#{id}, #{token}, #{expiretime})"})
    public int insertStoreToken(StoreToken obj);
    
    @Insert({ " INSERT INTO token1(id, token, expiretime)",
            " VALUES(#{id}, #{token}, #{expiretime})"})
    public int insertToken(Token1 obj);

    @Insert({ " INSERT INTO customertoken(id, token, expiretime)",
    " VALUES(#{id}, #{token}, #{expiretime})"})
    public int insertCustomerToken(CustomerToken obj);


}
