package com.example.restcontroller;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CustomerMemberDTO;
import com.example.dto.CustomerToken;
import com.example.mapper.CustomerMemberMapper;
import com.example.mapper.TokenMapper;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/kakaologin")
@RequiredArgsConstructor
public class KakaoLoginRestController {

    final CustomerMemberMapper customerMemberMapper;
    final TokenCreate tokenCreate;
    final TokenMapper tokenMapper;




    // 로그인
    @PostMapping(value = "/login.do")
    public Map<String, Object> loginPOST(@RequestBody CustomerMemberDTO obj) {
        Map<String, Object> map = new HashMap<>();

        try {
            // DB에 저장된 아이디를 이용해 아이디와 비밀번호 불러옴
            CustomerMemberDTO customerMember = customerMemberMapper.selectCustomerMemberOne(obj.getCustomerEmail());
            System.out.println(obj);
            map.put("status", 0);


                // 토큰 발행할 데이터
                System.out.println("Customer Email: " + customerMember.getCustomerEmail());
                Map<String, Object> claims = new HashMap<>();
                claims.put("customerEmail", customerMember.getCustomerEmail()); // DB에서 가져온 정보
                claims.put("customerNickname", customerMember.getNickname()); // DB에서 가져온 닉네임
                claims.put("customerPhone", customerMember.getPhone());

                System.out.println("Claims for token generation: " + claims);

                // 토큰생성 map1 (아이디, 만료시간)
                Map<String, Object> map1 = tokenCreate.generateCustomerToken(claims);

                // DB에 추가
                CustomerToken ct = new CustomerToken();
                ct.setId(obj.getCustomerEmail());
                ct.setToken((String) map1.get("token"));
                ct.setExpiretime((Date) map1.get("expiretime"));
                tokenMapper.insertCustomerToken(ct);

                map.put("token", map1.get("token"));
                map.put("status", 200);
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }




    // 회원가입
    @PostMapping(value = "/join.do")
    public Map<String, Object> joinPOST(@RequestBody CustomerMemberDTO obj) {
        System.out.println(obj.toString());
        Map<String, Object> map = new HashMap<>();

        try {
            obj.setPassword((obj.getPassword()));

            int ret = customerMemberMapper.insertCustomerMemberOne(obj);
            map.put("status", 0);
            if (ret == 1) {
                map.put("status", 200);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("stauts", -1);
        }
        return map;
    }

    
}