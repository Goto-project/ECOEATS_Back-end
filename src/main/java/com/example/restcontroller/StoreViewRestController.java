package com.example.restcontroller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ResourceLoader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    // 1km이내 가게 리스트 보기
    // 127.0.0.1:8080/ROOT/api/store/nearby
    @GetMapping("/nearby")
    public List<StoreView> nearbyStoresGET(
            @RequestParam BigDecimal customerLatitude,
            @RequestParam BigDecimal customerLongitude,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "distance") String sortBy) {

        if (customerLatitude == null || customerLongitude == null) {
            throw new IllegalArgumentException("Latitude and Longitude must not be null");
        }

        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "distance"; // 기본값 'distance'로 설정
        }

        // 1km 이내의 가게 목록 조회
        List<StoreView> storeViews = storeViewRepository.findStoresWithinRadius(customerLatitude, customerLongitude,
                category,
                sortBy);

        // 각 가게에 이미지 URL 추가
        for (StoreView storeView : storeViews) {
            // storeId로 이미지 조회
            StoreImage storeImage = storeImageRepository.findByStoreId_StoreId(storeView.getStoreid()); // findByStoreId
                                                                                                        // 메서드를 가정
            if (storeImage != null) {
                storeView.setImageurl("/api/store/image?no=" + storeImage.getStoreimageNo()); // 이미지 URL 설정
            } else {
                storeView.setImageurl(storeView.getImageurl() + "0"); // 기본 이미지용 번호
            }
        }

        return storeViews;
    }

    // 가게 상세보기
    // 127.0.0.1:8080/ROOT/api/store/detail/a208
    @GetMapping("/detail/{storeId}")
    public Map<String, Object> storeDetailGET(@PathVariable(name = "storeId") String storeId) {
        Map<String, Object> map = new HashMap<>();

        // storeId로 가게 정보 조회
        StoreView storeView = storeViewRepository.findById(storeId).orElse(null);

        try {
            if (storeId == null || storeId.isEmpty()) {
                map.put("status", 400);
                map.put("message", "해당 스토어를 찾을 수 없습니다.");
                return map;
            }

            // 해당 스토어의 이미지 조회
            StoreImage storeImage = storeImageRepository.findByStoreId_StoreId(storeId); // findByStoreId 메서드를 추가했다고 가정
            if (storeImage != null) {
                storeView.setImageurl(storeView.getImageurl() + storeImage.getStoreimageNo()); // 이미지 URL 설정
            } else {
                storeView.setImageurl(storeView.getImageurl() + "0"); // 기본 이미지용 번호
            }

            // 결과 반환
            map.put("status", 200);
            map.put("result", storeView);

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
        }

        return map;
    }
}
