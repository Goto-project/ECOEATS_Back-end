package com.example.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.BookMark;
import com.example.repository.BookMarkRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/bookmark")
@RequiredArgsConstructor
public class BookMarkRestController {
    
    final BookMarkRepository bookMarkRepository;


    // 즐겨찾기 추가 (POST)
    //127.0.0.1:8080/ROOT/api/bookmark/insert.json
    // { " customerEmail":{"customerEmail":"id1@test.com"},"storeId":{ " storeId":"store1"}}
    @PostMapping(value = "/insert.json")
    public Map<String, Object> insertPOST(@RequestBody BookMark obj) {
    
        Map<String, Object> map = new HashMap<>();
        try {
            bookMarkRepository.save(obj);
            System.out.println(obj.toString());
            // 상태 200 (성공)
            map.put("status", 200);
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);  // 오류 상태
        }

        return map;
    }
    
    // 즐겨찾기 삭제 (DeleteMapping)
    @DeleteMapping(value = "/delete.json")
    public  Map<String, Object> deletePOST(@RequestBody BookMark obj) {
        // System.out.println(obj.toString());
        Map<String, Object> map = new HashMap<>();
        try {
            bookMarkRepository.deleteById( obj.getBookmarkNo());
            map.put("status", 200);
        } catch (Exception e) {
            map.put("status", -1);
        }
        
        return map;
    }

    

}
