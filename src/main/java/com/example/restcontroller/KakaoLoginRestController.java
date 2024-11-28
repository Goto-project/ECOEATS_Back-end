package com.example.restcontroller;

import java.util.Date;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.entity.CustomerMember;
import com.example.service.KakaoLoginService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class KakaoLoginRestController {

    private final KakaoLoginService kakaoLoginService;
    private final String KAKAO_REST_API_KEY = "5dcc181ba43c2d5e19b1632c7b363b3a";

    

    /**
     * 카카오 로그인 처리
     *
     * @param data 클라이언트에서 전달받은 데이터 (액세스 토큰 포함)
     * @return 로그인 결과
     */
    @PostMapping("/kakao-login")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> data) {
        String token = data.get("token");

        // Kakao API 호출
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        String requestUrl = "https://kapi.kakao.com/v2/user/me";
        ResponseEntity<Map> response = restTemplate.exchange(
            requestUrl,
            HttpMethod.GET,
            entity,
            Map.class
        );

        // Kakao에서 반환된 사용자 정보 추출
        Map<String, Object> kakaoAccount = (Map<String, Object>) response.getBody().get("kakao_account");
        String email = (String) kakaoAccount.get("email");
        String nickname = (String) ((Map<String, Object>) response.getBody().get("properties")).get("nickname");

        // 토큰 만료 시간 설정
        Date expireTime = new Date(System.currentTimeMillis() + 1000L * 60 * 60); // 1시간 후

        // 1. 고객 정보 저장 또는 업데이트
        CustomerMember customer = kakaoLoginService.saveOrUpdateCustomer(email, nickname);

        // 2. 고객 토큰 정보 저장 또는 업데이트
        kakaoLoginService.saveOrUpdateToken(customer, token, expireTime);

        return ResponseEntity.ok("로그인 성공");
    }
}