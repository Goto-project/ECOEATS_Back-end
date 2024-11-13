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
    public Map<String, Object> updatePUT(@RequestBody Review obj, @RequestHeader(name = "token") String token, HttpServletRequest request) {
    Map<String, Object> map = new HashMap<>();

    try {
        // 1. 토큰에서 이메일 확인 (JwtFilter에서 설정한 "userid" 속성 사용)
        String userid = (String) request.getAttribute("userid");
        System.out.println("토큰의 이메일: " + userid);

        // 2. 토큰이 유효한지 확인 (토큰 검증 로직 필요)
        if (userid == null) {
            map.put("status", 401);
            map.put("message", "로그인 정보가 없습니다.");
            return map;
        }

        // 3. 기존 리뷰 조회
        Review ret = reviewRepository.findById(obj.getReviewNo()).orElse(null);
        if (ret == null) {
            map.put("status", 404);
            map.put("message", "Review not found");
            return map;
        }

        // 4. 권한 체크 - 기존 리뷰의 작성자와 토큰의 이메일 비교
        if (userid == null || !userid.equals(ret.getCustomerEmail().getCustomerEmail())) {
            map.put("status", 403);
            map.put("result", "수정 권한이 없습니다.");
            return map;
        }

        // 5. 리뷰 수정
        ret.setRating(obj.getRating());
        ret.setContent(obj.getContent());

        // 6. 저장
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
