package com.example.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.dto.Member1;

//springboot, react, android, mybatis, jpa
//mysql, oracle
@Mapper
public interface MemberMapper {

    //로그인 처리 => 아이디를 전달받으면 해당하는 정보를 반환
    //아이디 암호 권한
    // @Select({"SELECT id, pw, role FROM member1 WHERE id=#{id}"})
    // public Member1 selectMember1One(String id);

    // //회원가입 INSERT, UPDATE, DELETE는 반환값이 int로 고정됨
    // @Insert({"INSERT INTO member1(id, pw, name, phone, age, role)",
    //         " VALUES(#{id}, #{pw}, #{name}, #{phone}, #{age}, #{role})" })
    // public int insertMember1One(Member1 obj);

    // //로그인 처리 -> 아이디를 전달받으면 해당하는 정보를 반환
    // //아이디, 암호, 권한
    // @Select({"SELECT id, pw, role FROM member1 WHERE id=#{id}"})
    // public Member1 selectMemberOne(String id);

    // store 테이블에서 사용자 조회
    @Select("SELECT store_id AS id, password AS pw, 'SELLER' AS role FROM store WHERE store_id = #{username}")
    Member1 selectMemberFromStore(String username);

    // customer 테이블에서 사용자 조회
    @Select("SELECT customer_email AS id, password AS pw, 'CUSTOMER' AS role FROM customer_member WHERE customer_email = #{username}")
    Member1 selectMemberFromCustomer(String username);

    // admin 테이블에서 사용자 조회
    @Select("SELECT admin_id AS id, password AS pw, 'ADMIN' AS role FROM admin WHERE admin_id = #{username}")
    Member1 selectMemberFromAdmin(String username);
}
