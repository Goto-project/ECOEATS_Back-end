package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Review;
import com.example.repository.ReviewImageRepository;
import com.example.repository.ReviewRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/review")
@RequiredArgsConstructor
public class ReviewRestController {

    final ReviewRepository reviewRepository;
    final ReviewImageRepository reviewImageRepository;
    



    //목록에서 상태화면으로 이동하면 1개의 게시글 표시
    // 127.0.0.1:8080/ROOT/api/review/selectlist.json?reviewNo=1
    @GetMapping(value = "/selectlist.json")
    public  Map<String, Object> selectlistGET(@RequestParam(name = "reviewNo")int reviewNo) {
        Map<String, Object> map = new HashMap<>();
        try {
            List<Review> list = reviewRepository.findByReviewNo(reviewNo);
            map.put("status", 200);
            map.put("result", list);
        } catch (Exception e) {
            map.put("status", -1);
        }
        
        return map;
    }





    //리뷰작성
    // 127.0.0.1:8080/ROOT/api/review/insert.json
    // *토큰 {"storeId":{"storeId":"store1"},"customerEmail":{ "customerEmail":"id1@test.com"},"orderno":{"orderno":1},"rating:": 1,"content":"리뷰작성입니다"}
    @PostMapping(value = "/insert.json")
public Map<String, Object> insertPOST(@RequestBody Review obj, HttpServletRequest request) {
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
        
        // 요청된 리뷰의 작성자 이메일과 토큰의 이메일이 일치하는지 확인
        if (!customerEmail.equals(obj.getCustomerEmail().getCustomerEmail())) {
            map.put("status", 403);
            map.put("result", "리뷰 작성 권한이 없습니다.");
            return map;
        }
        
        // 리뷰 저장
        Review savedReview = reviewRepository.save(obj);
        
        // 성공 응답
        map.put("status", 200);
        map.put("result", "리뷰가 성공적으로 등록되었습니다.");
        map.put("savedReview", savedReview);
        
    } catch (Exception e) {
        e.printStackTrace();
        map.put("status", -1);
        map.put("error", "리뷰 등록 중 오류가 발생했습니다.");
    }
    
    return map;
}


    // 127.0.0.1:8080/ROOT/api/review/selectall.json
    //리뷰 전체보기
    @GetMapping(value = "/selectall.json")
    public Map<String, Object> selectallGET() {
        Map<String, Object> map = new HashMap<>();
        try {
            List<Review> list = reviewRepository.findAll();
            System.out.println(list.toString());

            map.put("status", 200);
            map.put("list", list);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;


    }

    //리뷰 수정
    // 127.0.0.1:8080/ROOT/api/review/update.json?reviewNo=1
    // {"reviewNo": 1,"content": "수정된 리뷰 내용","rating": 4}
    @PutMapping(value = "update.json")
public Map<String, Object> updatePUT(@RequestBody Review obj, HttpServletRequest request) {
    Map<String, Object> map = new HashMap<>();
    
    try {
        // JwtFilter에서 설정한 "customerEmail" 속성 사용
        String customerEmail = (String) request.getAttribute("customerEmail");
        System.out.println("토큰의 이메일1: " + customerEmail);

        if (customerEmail == null) {
            map.put("status", 403);
            map.put("result", "유효하지 않은 토큰입니다.");
            return map;
        }

        // 기존 리뷰 조회
        System.out.println(obj.toString());
        Review ret = reviewRepository.findById(obj.getReviewNo()).orElse(null);
        
        if (ret == null) {
            map.put("status", 404);
            map.put("message", "Review not found");
            return map;
        }

        // 권한 체크 - 기존 리뷰의 작성자와 토큰의 이메일 비교
        if (!customerEmail.equals(ret.getCustomerEmail().getCustomerEmail())) {
            map.put("status", 403);
            map.put("result", "수정 권한이 없습니다.");
            return map;
        }

        // 리뷰 수정
        ret.setRating(obj.getRating());
        ret.setContent(obj.getContent());
        
        // 저장
        reviewRepository.save(ret);
        map.put("status", 200);
        map.put("updatedReview", ret);

    } catch (Exception e) {
        e.printStackTrace();
        map.put("status", -1);
        map.put("error", "오류가 발생했습니다.");
    }
    
    return map;
}








    //리뷰 삭제
    // 127.0.0.1:8080/ROOT/api/review/delete.json
    //{"reviewNo":2}
@DeleteMapping(value = "/delete.json")
    public  Map<String, Object> deletePOST(@RequestBody Review obj,HttpServletRequest request) {
        System.out.println(obj.toString());
    Map<String, Object> map = new HashMap<>();
    
    try {
        // JwtFilter에서 설정한 "customerEmail" 속성 사용
        String customerEmail = (String) request.getAttribute("customerEmail");
        System.out.println("토큰의 이메일: " + customerEmail);

        if (customerEmail == null) {
            map.put("status", 403);
            map.put("result", "유효하지 않은 토큰입니다.");
            return map;
        }

        // 기존 리뷰 조회
        Review ret = reviewRepository.findById(obj.getReviewNo()).orElse(null);
        if (ret == null) {
            map.put("status", 404);
            map.put("message", "Review not found");
            return map;
        }

        // 권한 체크 - 기존 리뷰의 작성자와 토큰의 이메일 비교
        if (!customerEmail.equals(ret.getCustomerEmail().getCustomerEmail())) {
            map.put("status", 403);
            map.put("result", "삭제 권한이 없습니다.");
            return map;
        }

        // 리뷰 삭제
        reviewRepository.delete(ret);
        map.put("status", 200);
        map.put("message", "리뷰가 삭제되었습니다.");

    } catch (Exception e) {
        e.printStackTrace();
        map.put("status", -1);
        map.put("error", "오류가 발생했습니다.");
    }
    
    return map;
}




    
}
