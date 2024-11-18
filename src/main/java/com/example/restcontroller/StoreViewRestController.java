package com.example.restcontroller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    // 127.0.0.1:8080/ROOT/api/store/detail/storeId
    @GetMapping("/detail/{storeId}")
    public ResponseEntity<StoreView> storeDetailsGET(@PathVariable String storeId) {
        
        StoreView storeView = storeViewRepository.findById(storeId).orElse(null);

        if (storeView == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(storeView, HttpStatus.OK);
    }
}
