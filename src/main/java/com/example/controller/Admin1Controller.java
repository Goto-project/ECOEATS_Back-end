package com.example.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.entity.CustomerMember;
import com.example.entity.Review;
import com.example.entity.Store;
import com.example.repository.CustomerMemberRepository;
import com.example.repository.ReviewRepository;
import com.example.repository.StoreRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/admin")
public class Admin1Controller {

    final HttpSession httpSession;
    final StoreRepository storeRepository;
    final CustomerMemberRepository customerMemberRepository;
    final ReviewRepository reviewRepository;

    // 127.0.0.1:8080/ROOT/admin/
    @GetMapping(value = { "/", "/home.do", "/main.do" })
    public String getAdminHome(
            @RequestParam(name = "menu", defaultValue = "0") int menu,
            @RequestParam(name = "text", defaultValue = "") String text,
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "storeId", required = false) String storeId,
            Model model) {
                
        String role = (String) httpSession.getAttribute("role");
        System.out.println("세션에서 가져온 role: " + role);
        if (!"ROLE_ADMIN".equals(role)) {
            return "redirect:/page403"; // 접근 불가 페이지로 리다이렉트
        }

        if (menu <= 0) {
            return "redirect:/admin/home.do?menu=1";
        }
        PageRequest pageRequest = PageRequest.of(page - 1, 10);

        if (menu == 1) {
            List<Store> storeList = storeRepository.findByStoreNameContainingOrderByStoreNameAsc(text, pageRequest);
            // 전체 개수(11=>2, 20=>2, 21=>3)
            long total = storeRepository.countByStoreNameContaining(text);

            model.addAttribute("menu", menu);
            model.addAttribute("list", storeList);
            model.addAttribute("pages", (total - 1) / 10 + 1);
        } else if (menu == 2) {
            List<CustomerMember> customerMemberList = customerMemberRepository
                    .findByCustomerEmailContainingOrderByCustomerEmailAsc(text, pageRequest);
            long total = customerMemberRepository.countByCustomerEmailContaining(text);

            model.addAttribute("menu", menu);
            model.addAttribute("list", customerMemberList);
            model.addAttribute("pages", (total - 1) / 10 + 1);

        } else if (menu == 3) {
            // 가게 정보 가져오기
            List<Store> stores = storeRepository.findAll(); // 가게 목록 조회
            model.addAttribute("stores", stores);
        
            // 페이지 요청 객체 생성
            Pageable pageRequest1 = PageRequest.of(page - 1, 10, Sort.by(Sort.Direction.DESC, "regdate"));
        
            if (storeId != null && !storeId.isEmpty()) {
                // Store 객체로 변환 필요
                Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new IllegalArgumentException("해당 가게를 찾을 수 없습니다."));
                
                // Store 객체를 사용하여 리뷰 조회
                Page<Review> reviewPage = reviewRepository.findReviewsByStoreId(store, pageRequest1);
                
                model.addAttribute("reviews", reviewPage.getContent());
                model.addAttribute("totalReviews", reviewPage.getTotalElements());
                model.addAttribute("pages", reviewPage.getTotalPages());
            } else {
                // 기존 전체 리뷰 조회 로직 유지
                Page<Review> reviewPage = reviewRepository.findAll(pageRequest1);
                model.addAttribute("reviews", reviewPage.getContent());
                model.addAttribute("totalReviews", reviewPage.getTotalElements());
                model.addAttribute("pages", reviewPage.getTotalPages());
            }
        
            model.addAttribute("storeId", storeId); // 선택된 가게 ID
            model.addAttribute("menu", menu); // 현재 메뉴 설정
            return "admin/adminhome";
        }

    model.addAttribute("menu", menu); // 현재 메뉴 설정
    return "admin/adminhome"; // 뷰 파일 경로
}

    @PostMapping(value = "/storedelete.do")
    public String storeDeletePOST(@RequestParam(name = "storeId") String storeId) {
        storeRepository.deleteById(storeId);

        return "redirect:/admin/home.do?menu=1";
    }

    @PostMapping(value = "/customerdelete.do")
    public String customerDeletePOST(@RequestParam(name = "customerEmail") String customerEmail) {
        customerMemberRepository.deleteById(customerEmail);

        return "redirect:/admin/home.do?menu=2";
    }

    @PostMapping(value = "/reviewdelete.do")
public String reviewDeletePOST(
    @RequestParam(name = "reviewNo") int reviewNo,
    @RequestParam(name = "storeId", required = false) String storeId,
    RedirectAttributes redirectAttributes
) {
    // 리뷰 삭제
    reviewRepository.deleteById(reviewNo);
    
    // 리다이렉트 시 상태 유지
    redirectAttributes.addAttribute("menu", 3);
    if (storeId != null && !storeId.isEmpty()) {
        redirectAttributes.addAttribute("storeId", storeId);
    }
    
    return "redirect:/admin/home.do";
}
}
