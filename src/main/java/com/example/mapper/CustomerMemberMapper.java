package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.dto.CustomerMember;

@Mapper
public interface CustomerMemberMapper {

    // 회원가입
    @Insert({ "INSERT INTO customer_member(customer_email, password, nickname, phone)",
            " VALUES(#{customerEmail} ,#{password} , #{nickname} , #{phone})" })
    public int insertCustomerMemberOne(CustomerMember obj);

    // 로그인
    @Select({ "SELECT * FROM customer_member WHERE customer_email=#{customerEmail}" })
    @Results({
                        @Result(property = "customerEmail", column = "customer_email"),
        })

    public CustomerMember selectCustomerMemberOne(String customerEmail);

    // 닉네임 ,핸드폰 변경
    @Update({ "UPDATE customer_member SET password=#{password}, nickname=#{nickname}, phone=#{phone} WHERE customer_email=#{customerEmail}" })
    @Results({
        @Result(property = "customerEmail", column = "customer_email"),
})
    public int updateCustomer(CustomerMember customerMember);
}
