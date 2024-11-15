package com.example.restcontroller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.StoreView;
import com.example.repository.StoreViewRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/store")
@RequiredArgsConstructor
public class StoreViewRestController {

    final StoreViewRepository storeViewRepository;

    // 가게 상세보기
    // 127.0.0.1:8080/ROOT/api/store/storeId
    @GetMapping("/{storeId}")
    public StoreView storeDetailsGET(@PathVariable String storeId) {
        return storeViewRepository.findById(storeId).orElseThrow(() -> new RuntimeException("Store not found")); // 값이 없음
    }
}
