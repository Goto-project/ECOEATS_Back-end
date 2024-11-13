package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    // {"storeId":{"storeId":"store1"},"customerEmail":{ "customerEmail":"id1@test.com"},"orderno":{"orderno":1},"rating:": 1,"content":"리뷰작성입니다"}
    @PostMapping(value = "/insert.json")
        public Map<String, Object> insertPOST(@RequestBody Review obj) {
            System.out.println(obj.toString());
        
            Map<String, Object> map = new HashMap<>();
            try {
                reviewRepository.save(obj);
                
                // 상태 200 (성공)
                map.put("status", 200);
                
            } catch (Exception e) {
                System.err.println(e.getMessage());
                map.put("status", -1);  // 오류 상태
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
            // 1. 검증된 userid 확인
            String userid = (String) request.getAttribute("userid");
            
            if (userid == null || !userid.equals(obj.getCustomerEmail())) {
                // 토큰의 사용자와 리뷰 작성자가 일치하지 않으면 접근 거부
                map.put("status", 403);
                map.put("result", "수정 권한이 없습니다.");
                return map;
            }
    
            // 2. 기존 리뷰 조회 및 수정
            Review ret = reviewRepository.findById(obj.getReviewNo()).orElse(null);
            if (ret != null) {
                ret.setRating(obj.getRating());
                ret.setContent(obj.getContent());
                
                // 3. 데이터베이스에 변경 사항 저장
                reviewRepository.save(ret);
                map.put("status", 200);
                map.put("updatedReview", ret);
            } else {
                map.put("status", 404);
                map.put("message", "Review not found");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
            map.put("error", "오류가 발생했습니다.");
        }
    
        return map;
    }








    //리뷰 삭제
    // 127.0.0.1:8080/ROOT/api/review/delete.json
    //{"reviewNo":2}
@DeleteMapping(value = "/delete.json")
    public  Map<String, Object> deletePOST(@RequestBody Review obj) {
        System.out.println(obj.toString());
        Map<String, Object> map = new HashMap<>();
        try {
            reviewRepository.deleteById( obj.getReviewNo());
            map.put("status", 200);
        } catch (Exception e) {
            map.put("status", -1);
        }
        
        return map;
    }




    
}
