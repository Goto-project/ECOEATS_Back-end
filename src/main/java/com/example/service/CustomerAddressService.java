package com.example.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.dto.KakaoSearchDTO;

@Service
public class CustomerAddressService {

    public Map<String, BigDecimal> saveCustomerAddress(String searchKeyword) {
        // 카카오 API 호출
        KakaoSearch kakaoSearch = new KakaoSearch();
        KakaoSearchDTO kakaoSearchDTO = kakaoSearch.getKakaoSearch(searchKeyword);

        // documents 리스트 검증
        if (kakaoSearchDTO.getDocuments() == null || kakaoSearchDTO.getDocuments().isEmpty()) {
            throw new IllegalArgumentException("검색 결과가 없습니다. 검색어: " + searchKeyword);
        }

        // 검색된 첫 번째 주소 정보 가져오기
        KakaoSearchDTO.Document document = kakaoSearchDTO.getDocuments().get(0);

        // 위도와 경도만 반환
        Map<String, BigDecimal> result = new HashMap<>();
        result.put("latitude", new BigDecimal(document.getY()));
        result.put("longitude", new BigDecimal(document.getX()));

        return result;
    }
}