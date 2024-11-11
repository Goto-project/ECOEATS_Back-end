package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.dto.CustomerMember;

@Mapper
public interface CustomerMemberMapper {
    
    //회원가입
    @Insert({"INSERT INTO customer_member(customer_email, password, nickname, phone)" , 
            " VALUES(#{customerEmail} ,#{password} , #{nickname} , #{phone})"})
    public int insertCustomerMemberOne(CustomerMember obj);

    //로그인
    @Select({"SELECT * FROM customer_member WHERE customer_email=#{customerEmail}"})
    public CustomerMember selectCustomerMemberOne(String customerEmail);

}
