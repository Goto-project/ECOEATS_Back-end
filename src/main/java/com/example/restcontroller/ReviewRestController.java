package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Review;
import com.example.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/review")
@RequiredArgsConstructor
public class ReviewRestController {

    final ReviewRepository reviewRepository;


    // 127.0.0.1:8080/ROOT/api/store/selectall.json
    @GetMapping(value = "/api/review/selectall.json")
    public Map<String, Object> selectallGET() {
        Map<String, Object> map = new HashMap<>();
        try {
            List<Review> list = reviewRepository.findAll();

            map.put("status", 200);
            map.put("list", list);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }
    
}
