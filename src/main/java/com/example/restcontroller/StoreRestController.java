package com.example.restcontroller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.Store;
import com.example.dto.StoreToken;
import com.example.mapper.StoreMapper;
import com.example.mapper.TokenMapper;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/seller")
@RequiredArgsConstructor
public class StoreRestController {
    
    BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
    final StoreMapper storeMapper;
    final TokenCreate tokenCreate;
    final TokenMapper tokenMapper;

    // 로그인 => 토큰 => DB에 보관
    // 네이버, 카카오 로그인 => DB에 보관
    //127.0.0.1:8080/ROOT/api/seller/login.do
    @PostMapping(value = "/login.do")
    public Map<String, Object> loginPOST(@RequestBody Store store){
        Map<String, Object> map = new HashMap<>();
        try{
            //아이디를 이용해서 아이디와 암호 가져오기
            Store seller = storeMapper.selectStoreOne(store.getStoreId());
            map.put("status", 0);
            
            //사용자가 입력한 암호와 엔코더된 DB 암호 비교
            if (bcpe.matches(store.getPassword(), seller.getPassword())) {
                //토큰 발행할 데이터 생성(아이디, 이름...)
                Map<String, Object> send1 = new HashMap<>();
                map.put("store_id", store.getStoreId());

                //토큰 생성 seller 아이디, 만료시간
                Map<String, Object> map1 = tokenCreate.create(send1);

                //DB에 추가하고
                StoreToken st = new StoreToken();
                st.setId(store.getStoreId());
                st.setToken((String)map1.get("token"));
                st.setExpiretime((Date) map1.get("expiretime"));
                tokenMapper.insertStoreToken(st);

                //토큰값 전송
                map.put("token", map1.get("token"));
                map.put("status", 200);
            }

        }catch (Exception e){
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }
    

    // 회원가입
    //127.0.0.1:8080/ROOT/api/seller/join.do
    //{"storeId":"a201", "storeEmail":"abc@test.com", "password":"a201", "storeName":"가나다", "address":"서면", "phone":"010", "category":"도시락", "defaultPickup":"15:30"}
    @PostMapping(value = "/join.do")
    public Map<String, Object> joinPOST(@RequestBody Store store){
        System.out.println(store.toString());
        Map<String, Object> map = new HashMap<>();

        try{
            //전달받은 암호에서 암호화하여 obj에 다시 저장하기
            store.setPassword(bcpe.encode(store.getPassword()));

            int ret = storeMapper.insertStoreOne(store);
            map.put("status", 0);
            if (ret == 1) {
                map.put("status", 200);
            }

        }catch (Exception e){
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }
}
