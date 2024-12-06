package com.example.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.dto.CustomerMemberDTO;

@Mapper
public interface CustomerMemberMapper {

    // 회원가입
    @Insert({ "INSERT INTO customer_member(customer_email, password, nickname, phone)",
            " VALUES(#{customerEmail} ,#{password} , #{nickname} , #{phone})" })
    public int insertCustomerMemberOne(CustomerMemberDTO obj);

    // 로그인
    @Select({ "SELECT * FROM customer_member WHERE customer_email=#{customerEmail}" })
    @Results({
                        @Result(property = "customerEmail", column = "customer_email")
        })

    public CustomerMemberDTO selectCustomerMemberOne(String customerEmail);

    @Select({ "SELECT * FROM customer_member WHERE customer_email=#{customerEmail}" })
    @Results({
        @Result(property = "customerEmail", column = "customer_email")
    })
    CustomerMemberDTO findCustomerMemberDTOByEmail(String customerEmail);
    
    // 닉네임 ,핸드폰 변경, 
    @Update({ "UPDATE customer_member SET nickname=#{nickname}, phone=#{phone} WHERE customer_email=#{customerEmail}" })
    public int updateCustomer(CustomerMemberDTO customerMember);

    @Update("UPDATE customer_member SET password = #{password} WHERE customer_email=#{customerEmail}")
        int updatePassword(CustomerMemberDTO customerMember);

    

    //삭제
    // @Delete({"DELETE FROM customer_member WHERE customer_email=#{customerEmail}"})
    //     int deleteCustomer(String customerEmail);

    // 회원 비활성화 (isdeleted를 true로 업데이트)
    @Update("UPDATE customer_member SET isdeleted = true WHERE customer_email = #{customerEmail}")
    int deleteCustomer(String customerEmail);

    
}
