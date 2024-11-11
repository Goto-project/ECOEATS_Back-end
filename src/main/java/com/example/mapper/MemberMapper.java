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
    @Select({"SELECT * FROM member1 WHERE id=#{id}"})
    public Member1 selectMember1One(String id);

    //회원가입 INSERT, UPDATE, DELETE는 반환값이 int로 고정됨
    @Insert({"INSERT INTO member1(id, pw, name, phone, age, role)",
            " VALUES(#{id}, #{pw}, #{name}, #{phone}, #{age}, #{role})" })
    public int insertMember1One(Member1 obj);

    //로그인 처리 -> 아이디를 전달받으면 해당하는 정보를 반환
    //아이디, 암호, 권한
    @Select({"SELECT id, pw, role FROM member1 WHERE id=#{id}"})
    public Member1 selectMemberOne(String id);
}
