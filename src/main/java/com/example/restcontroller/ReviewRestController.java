package com.example.restcontroller;



import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.example.entity.CustomerMember;
import com.example.entity.Review;
import com.example.entity.ReviewImage;
import com.example.entity.Store;
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
    final ResourceLoader resourceLoader;












// 127.0.0.1:8080/ROOT/api/review/selectall.json?storeId=a208
    //리뷰 전체보기
    @GetMapping(value = "/selectall.json")
    public Map<String, Object> selectallGET(@RequestParam(name = "storeId") String storeId) {
        Map<String, Object> map = new HashMap<>();
    try {
        // 특정 가게의 리뷰만 가져오기
        Store store = new Store();
        store.setStoreId(storeId);  // storeId를 이용해 Store 객체 생성
        List<Review> list = reviewRepository.findByStoreId(store); // 해당 store의 리뷰들만 가져옴
        
        // 각 리뷰에 대해 이미지 URL을 설정합니다.
        for (Review review : list) {
            // ReviewImageRepository의 findByReviewno 메서드를 사용하여 이미지 조회
            ReviewImage reviewImage = reviewImageRepository.findByReviewno(review);
            if (reviewImage != null) {
                // 이미지 URL을 리뷰 객체에 설정
                review.setImageurl(review.getImageurl() + reviewImage.getReviewimageNo());
            }
        }

        map.put("status", 200);
        map.put("list", list);
    } catch (Exception e) {
        System.err.println(e.getMessage());
        map.put("status", -1);
        map.put("message", e.getMessage());
    }
    return map;
    }


// 127.0.0.1:8080/ROOT/api/review/myreviews.json
    @GetMapping(value = "/myreviews.json")
    public Map<String, Object> getMyReviews(HttpServletRequest request) {
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
    
            // CustomerMember 객체 생성
            CustomerMember customerMember = new CustomerMember();
            customerMember.setCustomerEmail(customerEmail);
    
            // 해당 사용자가 작성한 리뷰 리스트 조회
            List<Review> reviews = reviewRepository.findByCustomerEmail(customerMember);
    
            // 각 리뷰에 이미지 URL 추가
            for (Review review : reviews) {
                ReviewImage reviewImage = reviewImageRepository.findByReviewno(review);
                if (reviewImage != null) {
                    review.setImageurl(review.getImageurl() + reviewImage.getReviewimageNo());
                }
            }
    
            map.put("status", 200);
            map.put("list", reviews);
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "리뷰 조회 중 오류가 발생했습니다.");
        }
        return map;
    }






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

        //System.out.println(obj.());
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
            //System.out.println(savedReview.());
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
        // System.out.println(obj.());
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

// http://127.0.0.1:8080/ROOT/image?no=3
    //<img th:src="/ROOT/seller/image?no=1" />
    //이미지 url 
    @GetMapping(value = "/image")
    public ResponseEntity<byte[]> imagePreview(@RequestParam(name = "no") int no) throws IOException {
        ReviewImage obj = reviewImageRepository.findById(no).orElse(null);
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<byte[]> response = null;
        
        // DB에 이미지가 있는 경우
        if (obj != null && obj.getFiledata() != null && obj.getFiledata().length > 0) {
            headers.setContentType(MediaType.parseMediaType(obj.getFiletype()));
            response = new ResponseEntity<>(obj.getFiledata(), headers, HttpStatus.OK);
            return response;
        }
        
        // DB에 이미지가 없는 경우 기본 이미지 반환
        InputStream in = resourceLoader.getResource("classpath:/static/img/default.png").getInputStream();
        headers.setContentType(MediaType.IMAGE_PNG);
        response = new ResponseEntity<>(in.readAllBytes(), headers, HttpStatus.OK);
        return response;
    }


    
}
