package com.example.restcontroller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.dto.KakaoSearchDTO;


public class KakaoSearchRestController {

    public KakaoSearchDTO getKakaoSearch(String searchKeyword) {
        //카카오 API키
        final String restAPIKey = "KakaoAK 37eb325ae6b4bfaebd25610dc18e6224";
        //요청 URL과 검색어를 담음
        String url = "https://dapi.kakao.com/v2/local/search/address.json?query="+searchKeyword;
        //RestTemplate를 이용해
        RestTemplate restTemplate = new RestTemplate();
        //HTTPHeader를 설정해줘야 하기때문에 생성함
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", restAPIKey);
        headers.set("Accept", "application/json");
        HttpEntity<?> entity = new HttpEntity<>(headers);

        //ResTemplate를 이용해 요청을 보내고 KakaoSearchDto로 받아 response에 담음
        ResponseEntity<KakaoSearchDTO> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                KakaoSearchDTO.class
        );

        return response.getBody();
    }
}