package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.example.dto.Token1;

@Mapper
public interface TokenMapper {

    @Insert({ " INSERT INTO token1(id, token, expiretime)",
            " VALUES(#{id}, #{token}, #{expiretime})"})
    public int insertToken(Token1 obj);
}
