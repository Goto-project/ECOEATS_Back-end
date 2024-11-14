package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.entity.Review;
import com.example.entity.ReviewImage;
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




    @PostMapping(value = "/insert.json")
    public Map<String, Object> insertPOST(
            @RequestPart(value = "review") Review obj,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest request) {

        //System.out.println(obj.toString());
        //System.out.println(imageFile.getOriginalFilename());

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
    
    
            // 최신 리뷰 번호 조회
            
            
            // 리뷰 저장
            Review savedReview = reviewRepository.save(obj);
            //System.out.println("===============");
            //System.out.println(savedReview.toString());
            //System.out.println("aaaaa");
            
            
            // 이미지가 제공된 경우 처리
            if (imageFile != null && !imageFile.isEmpty()) {

                ReviewImage reviewImage = new ReviewImage();

                reviewImage.setFilename(imageFile.getOriginalFilename());
                reviewImage.setFiletype(imageFile.getContentType());
                reviewImage.setFilesize(imageFile.getSize());
                reviewImage.setFiledata(imageFile.getBytes());
                reviewImage.setReviewno(savedReview);
                
                // 이미지 저장
                reviewImageRepository.save(reviewImage);
            }
            
            
            
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
//     {
//     "reviewNo": 23,
//     "customerEmail": {
//     "customerEmail":"test1234@test.com"},
//     "rating": 3,
//     "content": "사진수정중입니다"
// }
    @PutMapping(value = "/update.json")
    public Map<String, Object> updatePUT(
            @RequestPart(value = "review") Review obj,
            @RequestPart(value = "image", required = false) MultipartFile imageFile,
            HttpServletRequest request) {
        // System.out.println(obj.toString());
        Map<String, Object> map = new HashMap<>();

        try {
            // 토큰에서 이메일 확인
            String customerEmail = (String) request.getAttribute("customerEmail");

            if (customerEmail == null || !customerEmail.equals(obj.getCustomerEmail().getCustomerEmail())) {
                map.put("status", 403);
                map.put("result", "수정 권한이 없습니다.");
                return map;
            }

            // 기존 리뷰 조회 및 수정
            Review ret = reviewRepository.findById(obj.getReviewNo()).orElse(null);
            if (ret != null) {
                ret.setRating(obj.getRating());
                ret.setContent(obj.getContent());

                // 이미지 처리
                if (imageFile != null && !imageFile.isEmpty()) {
                    // 기존 이미지가 있다면 삭제
                    ReviewImage existingImage = reviewImageRepository.findByReviewno(ret); // ret은 Review 객체라고 가정
                    if (existingImage != null) {
                        reviewImageRepository.delete(existingImage);
                    }

                    // 새 이미지 저장
                    ReviewImage reviewImage = new ReviewImage();
                    reviewImage.setFilename(imageFile.getOriginalFilename());
                    reviewImage.setFiletype(imageFile.getContentType());
                    reviewImage.setFilesize(imageFile.getSize());
                    reviewImage.setFiledata(imageFile.getBytes());
                    reviewImage.setReviewno(ret);

                    reviewImageRepository.save(reviewImage);
                    System.out.println(reviewImage.getFilename());
                }
                    
                // 데이터베이스에 변경 사항 저장
                reviewRepository.save(ret);
                map.put("status", 200);
                map.put("result", "리뷰가 성공적으로 수정되었습니다.");
                map.put("updatedReview", ret);
            } else {
                map.put("status", 404);
                map.put("result", "리뷰를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "리뷰 수정 중 오류가 발생했습니다.");
        }

        return map;
    }









    //리뷰 삭제
    // 127.0.0.1:8080/ROOT/api/review/delete.json
    //{"reviewNo":2}
    @DeleteMapping(value = "/delete.json")
    public Map<String, Object> deletePOST(
            @RequestBody Review obj,
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
            
            // 삭제하려는 리뷰 조회
            Optional<Review> existingReview = reviewRepository.findById(obj.getReviewNo());
            
            // 리뷰가 존재하지 않는 경우
            if (existingReview.isEmpty()) {
                map.put("status", 404);
                map.put("result", "존재하지 않는 리뷰입니다.");
                return map;
            }
            
            // 리뷰 작성자 확인
            if (!customerEmail.equals(existingReview.get().getCustomerEmail().getCustomerEmail())) {
                map.put("status", 403);
                map.put("result", "리뷰 삭제 권한이 없습니다.");
                return map;
            }
    
            // 리뷰 삭제
            reviewRepository.deleteById(obj.getReviewNo());
            
            // 성공 응답
            map.put("status", 200);
            map.put("result", "리뷰가 성공적으로 삭제되었습니다.");
            
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "리뷰 삭제 중 오류가 발생했습니다.");
        }
                
        return map;
    }



    
}
