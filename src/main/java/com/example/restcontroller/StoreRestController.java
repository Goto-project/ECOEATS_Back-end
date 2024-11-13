package com.example.restcontroller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.Store;
import com.example.dto.StoreToken;
import com.example.mapper.StoreMapper;
import com.example.mapper.TokenMapper;
import com.example.token.TokenCreate;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/seller")
@RequiredArgsConstructor
public class StoreRestController {

    BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
    final StoreMapper storeMapper;

    // 토큰 발행 및 검증용 컴포넌트 객체 생성
    final TokenCreate tokenCreate;

    final TokenMapper tokenMapper;
    final HttpSession httpSession;

    // 회원 삭제
    // 127.0.0.1:8080/ROOT/api/seller/delete.do
    @DeleteMapping(value = "/delete.do")
    public Map<String, Object> deleteDELETE(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        // String rawToken = token.replace("Bearer ", "").trim();

        try {
            // 토큰 유효성 검사 및 storeId 추출
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(token);
            String storeId = (String) tokenData.get("storeId");
            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            Store store = storeMapper.selectStoreOne(storeId);
            if (store == null) {
                map.put("status", 404);
                map.put("message", "회원 정보를 찾을 수 없습니다.");
                return map;
            }

            // 회원 삭제 쿼리 실행
            int result = storeMapper.deleteStore(storeId);

            if (result > 0) {
                map.put("status", 200);
                System.out.println("회원 삭제 성공");
            } else {
                map.put("status", 400);
                System.out.println("회원 삭제 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
        }
        return map;
    }

    // 정보 수정
    // 127.0.0.1:8080/ROOT/api/seller/update.do
    @PutMapping(value = "/update.do")
    public Map<String, Object> updatePUT(@RequestBody Store store,
            @RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");
            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            // 요청받은 객체에 로그인된 사용자 이메일을 설정
            store.setStoreId(storeId);

            // 비밀번호가 수정되는 경우에만 암호화 처리
            if (store.getPassword() != null && !store.getPassword().isEmpty()) {
                String encodedPassword = bcpe.encode(store.getPassword());
                store.setPassword(encodedPassword);
            }

            // 닉네임과 핸드폰 정보 업데이트
            int result = storeMapper.updateStore(store);

            // 업데이트 성공 여부 확인
            if (result > 0) {
                map.put("status", 200);
                map.put("message", "회원 정보 수정 성공");
            } else {
                map.put("status", 400);
                map.put("message", "회원 정보 수정 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
        }
        return map;
    }

    // 리액트에서 아이디와 암호를 전달해줌 => DB에 있는지 확인 => 토큰 발행
    // const body = {"storeId":"a201", "password":"a201"} 키는 dto와 맞추기 값은 DB에 있는 걸 해야
    // 함
    // 127.0.0.1:8080/ROOT/api/seller/login.do
    @PostMapping(value = "/login.do")
    public Map<String, Object> loginPOST(@RequestBody Store store) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 아이디를 이용해서 아이디와 암호 가져오기
            Store seller = storeMapper.selectStoreOne(store.getStoreId());
            map.put("status", 0);

            // 사용자가 입력한 암호와 엔코더된 DB 암호 비교
            // 앞쪽이 사용자가 입력한 내용(암호화x), 뒷쪽이 DB의 암호(암호화o)
            if (bcpe.matches(store.getPassword(), seller.getPassword())) {
                // 토큰 발행할 데이터 생성(아이디, 이름...)
                Map<String, Object> send1 = new HashMap<>();
                send1.put("storeId", seller.getStoreId());
                send1.put("name", seller.getStoreName());

                // 토큰 생성 seller 아이디, 만료시간
                Map<String, Object> map1 = tokenCreate.generateSellerToken(send1);

                // DB에 추가하고
                StoreToken storeToken = new StoreToken();
                storeToken.setId(store.getStoreId());
                storeToken.setToken((String) map1.get("token"));
                storeToken.setExpiretime((Date) map1.get("expiretime"));
                tokenMapper.insertStoreToken(storeToken);

                // 토큰값 전송
                map.put("token", map1.get("token"));
                map.put("status", 200);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }

    // 회원가입
    // 127.0.0.1:8080/ROOT/api/seller/join.do
    // {"storeId":"a201", "storeEmail":"abc@test.com", "password":"a201",
    // "storeName":"가나다", "address":"서면", "phone":"010", "category":"도시락",
    // "defaultPickup":"15:30"}
    @PostMapping(value = "/join.do")
    public Map<String, Object> joinPOST(@RequestBody Store store) {
        System.out.println(store.toString());
        Map<String, Object> map = new HashMap<>();

        try {
            // 전달받은 암호에서 암호화하여 obj에 다시 저장하기
            store.setPassword(bcpe.encode(store.getPassword()));

            int ret = storeMapper.insertStoreOne(store);
            map.put("status", 0);
            if (ret == 1) {
                map.put("status", 200);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }
}
