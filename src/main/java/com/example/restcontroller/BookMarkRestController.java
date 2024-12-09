package com.example.restcontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.BookMark;
import com.example.entity.Store;
import com.example.entity.StoreView;
import com.example.repository.BookMarkRepository;
import com.example.repository.StoreRepository;
import com.example.repository.StoreViewRepository;
import com.example.token.TokenCreate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/bookmark")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
public class BookMarkRestController {

    final BookMarkRepository bookMarkRepository;
    final StoreViewRepository storeViewRepository;
    final StoreRepository storeRepository;
    final TokenCreate tokenCreate;

    // 127.0.0.1:8080/ROOT/api/bookmark/list
    @GetMapping("/list")
    public Map<String, Object> getBookmarkedStores(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> responseMap = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();

        // Bearer 접두사 제거
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            // 이메일이 없는 경우
            if (customerEmail == null) {
                responseMap.put("status", 401);
                responseMap.put("message", "로그인된 사용자 정보가 없습니다.");
                return responseMap;
            }

            // 북마크한 가게 목록 조회
            List<BookMark> bookmarks = bookMarkRepository.findByCustomerEmail_CustomerEmail(customerEmail);

            if (bookmarks.isEmpty()) {
                responseMap.put("status", 404);
                responseMap.put("message", "북마크한 가게가 없습니다.");
                return responseMap;
            }

            // 북마크한 가게 정보를 조회하여 리스트에 추가
            for (BookMark bookmark : bookmarks) {
                // storeView 조회 시 삭제되지 않은 가게만 선택
                Optional<StoreView> storeViewOptional = storeViewRepository
                        .findById(bookmark.getStoreId().getStoreId());

                if (storeViewOptional.isPresent()) {
                    StoreView storeView = storeViewOptional.get();

                    // 삭제되지 않은 가게만 처리
                    if (!storeView.isIsdeleted()) {
                        Map<String, Object> storeMap = new HashMap<>();
                        storeMap.put("storeId", storeView.getStoreid());
                        storeMap.put("storeName", storeView.getStoreName());
                        storeMap.put("address", storeView.getAddress());
                        storeMap.put("phone", storeView.getPhone());
                        storeMap.put("category", storeView.getCategory());

                        // storeimageno가 null이면 0으로 처리
                        String storeImageNo = (storeView.getStoreimageno() != null)
                                ? storeView.getStoreimageno().toString()
                                : "0";
                        String imageUrl = storeView.getImageurl() + storeImageNo;
                        storeMap.put("imageurl", imageUrl);

                        resultList.add(storeMap);
                    }
                }
            }
            responseMap.put("status", 200);
            responseMap.put("data", resultList);

        } catch (Exception e) {
            responseMap.put("status", -1);
            responseMap.put("message", "토큰 검증 중 오류가 발생했습니다.");
        }

        return responseMap;
    }

    // 127.0.0.1:8080/ROOT/api/bookmark/mybookmarks.json
    // 내 즐겨찾기 목록
    @GetMapping(value = "/mybookmarks.json")
    public Map<String, Object> getMyBookmarks(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            // JwtFilter에서 설정된 "customerEmail" 속성을 가져옵니다.
            String customerEmailValue = (String) request.getAttribute("customerEmail");
            System.out.println("토큰의 이메일: " + customerEmailValue);

            // 토큰 유효성 검사
            if (customerEmailValue == null) {
                map.put("status", 403);
                map.put("result", "유효하지 않은 토큰입니다.");
                return map;
            }

            // 즐겨찾기 목록 조회
            List<BookMark> bookmarks = bookMarkRepository.findByCustomerEmail_CustomerEmail(customerEmailValue);

            // 각 북마크에서 Store 정보를 추출
            List<Store> stores = bookmarks.stream()
                    .map(BookMark::getStoreId)
                    .toList();

            map.put("status", 200);
            map.put("stores", stores); // 즐겨찾기한 가게 리스트 반환
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "북마크 조회 중 오류가 발생했습니다.");
        }
        return map;
    }

    // 즐겨찾기검색
    @GetMapping(value = "/searchbookmark.json")
    public Map<String, Object> searchBookmark(
            @RequestParam(name = "storeId") String storeId,
            HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            // JwtFilter에서 설정된 "customerEmail" 속성 가져오기
            String customerEmailValue = (String) request.getAttribute("customerEmail");
            System.out.println("토큰의 이메일: " + customerEmailValue);

            // 토큰 유효성 검사
            if (customerEmailValue == null) {
                map.put("status", 403);
                map.put("result", "유효하지 않은 토큰입니다.");
                return map;
            }

            // 북마크에서 고객 이메일과 가게 ID로 검색
            List<BookMark> bookmarks = bookMarkRepository.findByCustomerEmail_CustomerEmailAndStoreId_StoreId(
                    customerEmailValue, storeId);
            System.out.println(bookmarks.toString());
            if (!bookmarks.isEmpty()) {
                // 가게가 북마크에 존재하는 경우
                map.put("status", 200);
                map.put("result", "가게가 북마크에 있습니다.");
                map.put("storeId", storeId);
            } else {
                // 가게가 북마크에 없는 경우
                map.put("status", 404);
                map.put("result", "가게가 북마크에 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "북마크 검색 중 오류가 발생했습니다.");
        }
        return map;
    }

    // 즐겨찾기 추가 (POST)
    // 127.0.0.1:8080/ROOT/api/bookmark/insert.json
    // { " customerEmail":{"customerEmail":"id1@test.com"},"storeId":{ "
    // storeId":"store1"}}
    @PostMapping(value = "/insert.json")
    public Map<String, Object> insertPOST(
            @RequestBody BookMark obj,
            @RequestHeader(name = "Authorization") String token) {

        Map<String, Object> map = new HashMap<>();

        try {
            // Bearer 접두사 제거하고 토큰만 추출
            String rawToken = token.replace("Bearer ", "").trim();

            // 토큰 검증
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            if (customerEmail == null) {
                map.put("status", 403);
                map.put("result", "유효하지 않은 토큰입니다.");
                return map;
            }

            // 요청된 북마크의 사용자 이메일과 토큰에서 추출한 이메일이 일치하는지 확인
            if (!customerEmail.equals(obj.getCustomerEmail().getCustomerEmail())) {
                map.put("status", 403);
                map.put("result", "북마크 추가 권한이 없습니다.");
                return map;
            }

            // 이미 북마크가 존재하는지 확인
            List<BookMark> existingBookmarks = bookMarkRepository
                    .findByCustomerEmail_CustomerEmailAndStoreId_StoreId(
                            obj.getCustomerEmail().getCustomerEmail(),
                            obj.getStoreId().getStoreId());

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
            @RequestBody Map<String, Object> requestData,
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

            // 요청에서 storeId 추출
            String storeId = (String) requestData.get("storeId");
            if (storeId == null || storeId.isEmpty()) {
                map.put("status", 400);
                map.put("result", "storeId가 누락되었습니다.");
                return map;
            }

            // storeId로 Store 객체 조회
            Store store = storeRepository.findByStoreId(storeId);
            if (store == null) {
                map.put("status", 404);
                map.put("result", "존재하지 않는 매장입니다.");
                return map;
            }

            // Store와 customerEmail을 사용하여 북마크 리스트 조회
            List<BookMark> bookmarks = bookMarkRepository
                    .findByCustomerEmail_CustomerEmailAndStoreId_StoreId(customerEmail, storeId);
            if (bookmarks.isEmpty()) {
                map.put("status", 404);
                map.put("result", "존재하지 않는 북마크입니다.");
                return map;
            }

            // 북마크 삭제
            for (BookMark bookmark : bookmarks) {
                bookMarkRepository.delete(bookmark);
            }

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
