package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.BookMark;
import com.example.repository.BookMarkRepository;

import jakarta.servlet.http.HttpServletRequest;
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
public Map<String, Object> insertPOST(
        @RequestBody BookMark obj,
        HttpServletRequest request) {
    
    Map<String, Object> map = new HashMap<>();
    
    try {
        // JwtFilter에서 설정한 "customerEmail" 속성 사용
        String customerEmail = (String) request.getAttribute("customerEmail");
        System.out.println("토큰의 이메일: " + customerEmail);
        
        // 토큰 유효성 검사
        if (customerEmail == null) {
            map.put("status", 403);
            map.put("result", "유효하지 않은 토큰입니다.");
            return map;
        }
        
        // 요청된 북마크의 사용자 이메일과 토큰의 이메일이 일치하는지 확인
        if (!customerEmail.equals(obj.getCustomerEmail().getCustomerEmail())) {
            map.put("status", 403);
            map.put("result", "북마크 추가 권한이 없습니다.");
            return map;
        }

        // 이미 북마크가 존재하는지 확인
        List<BookMark> existingBookmarks = bookMarkRepository
            .findByCustomerEmail_CustomerEmailAndStoreId_StoreId(
                obj.getCustomerEmail().getCustomerEmail(), 
                obj.getStoreId().getStoreId()
            );

        if (!existingBookmarks.isEmpty()) {
            map.put("status", 400);
            map.put("result", "이미 북마크에 추가된 매장입니다.");
            return map;
        }
        
        // 북마크 저장
        BookMark savedBookmark = bookMarkRepository.save(obj);
        
        // 성공 응답
        map.put("status", 200);
        map.put("result", "북마크가 성공적으로 추가되었습니다.");
        map.put("savedBookmark", savedBookmark);
        
    } catch (Exception e) {
        e.printStackTrace();
        map.put("status", -1);
        map.put("error", "북마크 추가 중 오류가 발생했습니다.");
    }
    
    return map;
}
    
    // 즐겨찾기 삭제 (DeleteMapping)
    // 127.0.0.1:8080/ROOT/api/bookmark/delete.json
    @DeleteMapping(value = "/delete.json")
    public Map<String, Object> deletePOST(
            @RequestBody BookMark obj,
            HttpServletRequest request) {
        
        Map<String, Object> map = new HashMap<>();
        
        try {
            // JwtFilter에서 설정한 "customerEmail" 속성 사용
            String customerEmail = (String) request.getAttribute("customerEmail");
            System.out.println("토큰의 이메일: " + customerEmail);
            
            // 토큰 유효성 검사
            if (customerEmail == null) {
                map.put("status", 403);
                map.put("result", "유효하지 않은 토큰입니다.");
                return map;
            }
            
            // 삭제하려는 북마크 조회
            Optional<BookMark> existingBookmark = bookMarkRepository.findById(obj.getBookmarkNo());
            if (existingBookmark.isEmpty()) {
                map.put("status", 404);
                map.put("result", "존재하지 않는 북마크입니다.");
                return map;
            }
            
            // 북마크 소유자 확인
            if (!customerEmail.equals(existingBookmark.get().getCustomerEmail().getCustomerEmail())) {
                map.put("status", 403);
                map.put("result", "북마크 삭제 권한이 없습니다.");
                return map;
            }

            // 북마크 삭제
            bookMarkRepository.deleteById(obj.getBookmarkNo());
            
            // 성공 응답
            map.put("status", 200);
            map.put("result", "북마크가 성공적으로 삭제되었습니다.");
            
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "북마크 삭제 중 오류가 발생했습니다.");
        }
        
        return map;
    }
}
