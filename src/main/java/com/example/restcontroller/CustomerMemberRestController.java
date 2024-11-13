package com.example.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CustomerMember;
import com.example.dto.CustomerToken;
import com.example.mapper.CustomerMemberMapper;
import com.example.mapper.TokenMapper;
import com.example.token.TokenCreate;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;




@RestController
@RequestMapping(value = "/api/customer")
@RequiredArgsConstructor
public class CustomerMemberRestController {
    
    final CustomerMemberMapper customerMemberMapper;
    final TokenCreate tokenCreate;
    final TokenMapper tokenMapper;
    final HttpSession httpSession;

    BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
    

    //정보수정(닉네임, 핸드폰)
    @PutMapping(value = "/update.do")
    public Map<String , Object> updatePUT(@RequestBody CustomerMember obj,
                                        @RequestHeader(name = "token")String token) {
        Map<String,Object> map = new HashMap<>();
        
        try{
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(token);
            String customerEmail = (String) tokenData.get("customerEmail");

            //DB에서
            CustomerMember customerMember = customerMemberMapper.selectCustomerMemberOne(customerEmail);

            if(customerMember ==null){ map.put("status", 403);
            map.put("message", "권한이 없습니다. 유효하지 않은 사용자입니다.");
            return map;
            }

            // 3. 업데이트할 필드 설정 (닉네임과 핸드폰 번호만 변경 가능)
            if (obj.getNickname() != null && !obj.getNickname().isEmpty()) {
                customerMember.setNickname(obj.getNickname());
            }
            if (obj.getPhone() != null && !obj.getPhone().isEmpty()) {
                customerMember.setPhone(obj.getPhone());
            }
            if (obj.getPassword() != null && !obj.getPassword().isEmpty()) {
                String encodedPassword = bcpe.encode(obj.getPassword());
                customerMember.setPassword(encodedPassword);
            }

            int result = customerMemberMapper.updateCustomer(customerMember);
            // 업데이트 결과 확인
            if (result > 0) {
                map.put("status", 200);
                map.put("message", "회원 정보가 성공적으로 업데이트되었습니다.");
            } else {
                map.put("status", 0);
                map.put("message", "회원 정보 업데이트에 실패하였습니다.");
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }


    //로그인
    @PostMapping(value = "/login.do")
    public Map<String , Object> loginPOST(@RequestBody CustomerMember obj) {
        Map<String ,Object> map = new HashMap<>();
        
        try{
            //DB에 저장된 아이디를 이용해 아이디와 비밀번호 불러옴
            CustomerMember customerMember = customerMemberMapper.selectCustomerMemberOne(obj.getCustomerEmail());
            System.out.println(obj);
            map.put("status", 0);

            //사용자가 입력한 암호와, DB의 암호 비교
            if(bcpe.matches(obj.getPassword() , customerMember.getPassword())){
                
                //토큰 발행할 데이터
                System.out.println("Customer Email: " + customerMember.getCustomerEmail());
                Map<String, Object> claims = new HashMap<>();
                claims.put("customerEmail", customerMember.getCustomerEmail()); // DB에서 가져온 정보
                claims.put("customerNickname", customerMember.getNickname());   // DB에서 가져온 닉네임
                claims.put("customerPhone", customerMember.getPhone()); 
                
                System.out.println("Claims for token generation: " + claims); 
                
                //토큰생성 map1 (아이디, 만료시간)
                Map<String, Object> map1 = tokenCreate.generateCustomerToken(claims);

                //DB에 추가
                CustomerToken ct = new CustomerToken();
                ct.setId(obj.getCustomerEmail());
                ct.setToken((String)map1.get("token") );
                ct.setExpiretime((Date)map1.get("expiretime"));
                tokenMapper.insertCustomerToken(ct);

                map.put("token", map1.get("token"));
                map.put("status", 200);
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }

    //회원가입
    @PostMapping(value = "/join.do")
    public Map<String ,Object> joinPOST(@RequestBody CustomerMember obj) {
        System.out.println(obj.toString());
        Map<String, Object> map = new HashMap<>();

        try{
            obj.setPassword(bcpe.encode(obj.getPassword()));

            int ret = customerMemberMapper.insertCustomerMemberOne(obj);
            map.put("status", 0);
            if(ret ==1){
                map.put("status", 200);
            }
        }catch(Exception e){
            System.err.println(e.getMessage());
            map.put("stauts", -1);
        }
        return map;
    }

    
    
}
