package com.example.restcontroller;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.StoreImage;
import com.example.entity.StoreView;
import com.example.repository.StoreImageRepository;
import com.example.repository.StoreViewRepository;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/store")
@RequiredArgsConstructor
public class StoreViewRestController {

    final StoreViewRepository storeViewRepository;
    final StoreImageRepository storeImageRepository;
    final ResourceLoader resourceLoader;
    final TokenCreate tokenCreate;

    // 가게 상세보기
    // 127.0.0.1:8080/ROOT/api/store/detail
    @GetMapping("/detail")
    public Map<String, Object> storeDetailGET(@RequestHeader(name = "Authorization") String token){
        Map<String, Object> map = new HashMap<>();
        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            // 토큰 유효성 검사 및 storeId 추출
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");
            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

        // storeId에 해당하는 StoreView 조회
        StoreView storeView = storeViewRepository.findById(storeId).orElse(null);
        if (storeView == null) {
            map.put("status", 404);
            map.put("message", "해당 스토어를 찾을 수 없습니다.");
            return map;
        }

        // 해당 스토어의 이미지 조회
        StoreImage storeImage = storeImageRepository.findByStoreId_StoreId(storeId);  // findByStoreId 메서드를 추가했다고 가정
        if (storeImage != null) {
            storeView.setImageurl(storeView.getImageurl()+ storeImage.getStoreimageNo());  // 이미지 URL 설정
        }

        // 결과 반환
        map.put("status", 200);
        map.put("result", storeView);

        }catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
        }

        return map;
    }
}
