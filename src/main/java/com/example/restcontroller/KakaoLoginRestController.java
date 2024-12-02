package com.example.restcontroller;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@CrossOrigin(
    origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    allowedHeaders = "*", 
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS},
    allowCredentials = "true"
)
public class KakaoLoginRestController {

    final CustomerMemberMapper customerMemberMapper;
    final TokenCreate tokenCreate;
    final TokenMapper tokenMapper;

    

    @PostMapping(value = "/auth.do")
public Map<String, Object> loginOrJoin(@RequestBody CustomerMemberDTO obj) {
    Map<String, Object> map = new HashMap<>();

       // 카카오에서 받은 고유 ID를 customer_email로 사용
       String customerEmail = obj.getCustomerEmail();  // 카카오 로그인에서 받은 ID

        if (customerEmail == null || customerEmail.isEmpty()) {
        map.put("status", -1);
        map.put("message", "유효한 카카오 ID가 필요합니다.");
        return map;
    }

    try {
        // DB에서 사용자 조회
        CustomerMemberDTO existingMember = customerMemberMapper.selectCustomerMemberOne(obj.getCustomerEmail());

        if (existingMember == null) {
            // 회원가입 로직 수행
            int ret = customerMemberMapper.insertCustomerMemberOne(obj);
            if (ret != 1) {
                map.put("status", -1);
                map.put("message", "회원가입 실패");
                return map;
            }
        }

        // 로그인 처리
        CustomerMemberDTO customerMember = customerMemberMapper.selectCustomerMemberOne(customerEmail);
        if (customerMember == null) {
            map.put("status", -1);
            map.put("message", "로그인 실패: 사용자 정보를 찾을 수 없음");
            return map;
        }

        // 토큰 발행
        Map<String, Object> claims = new HashMap<>();
        claims.put("customerEmail", customerMember.getCustomerEmail());
        claims.put("customerNickname", customerMember.getNickname());
        claims.put("customerPhone", customerMember.getPhone());

        Map<String, Object> tokenData = tokenCreate.generateCustomerToken(claims);

        // DB에 토큰 저장
        CustomerToken ct = new CustomerToken();
        ct.setId(customerMember.getCustomerEmail());
        ct.setToken((String) tokenData.get("token"));
        ct.setExpiretime((Date) tokenData.get("expiretime"));
        tokenMapper.insertCustomerToken(ct);

        map.put("status", 200);
        map.put("token", tokenData.get("token"));
    } catch (Exception e) {
        System.err.println(e.getMessage());
        map.put("status", -1);
        map.put("message", "오류 발생: " + e.getMessage());
    }

    return map;
}





    // 로그인
    //127.0.0.1:8080/ROOT/api/kakaologin/login.do
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
    //127.0.0.1:8080/ROOT/api/kakaologin/join.do
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