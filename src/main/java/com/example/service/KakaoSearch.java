package com.example.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.dto.KakaoSearchDTO;

public class KakaoSearch {

    // 카카오 API 키
    private static final String REST_API_KEY = "KakaoAK 5dcc181ba43c2d5e19b1632c7b363b3a";

    // 카카오 API를 통해 주소 검색하는 메서드
    public KakaoSearchDTO getKakaoSearch(String searchKeyword) {
        // 요청 URL과 검색어를 담음
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + searchKeyword;

        // RestTemplate을 사용하여 HTTP 요청을 보냄
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", REST_API_KEY); // 카카오 API 키
        headers.set("Accept", "application/json");

        HttpEntity<?> entity = new HttpEntity<>(headers);

        // API 호출 후 받은 데이터를 KakaoSearchDTO로 반환
        ResponseEntity<KakaoSearchDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, KakaoSearchDTO.class);

        return response.getBody(); // KakaoSearchDTO 객체 반환
    }
}
