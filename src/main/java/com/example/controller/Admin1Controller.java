package com.example.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.entity.CustomerMember;
import com.example.entity.Review;
import com.example.entity.Store;
import com.example.repository.CustomerMemberRepository;
import com.example.repository.ReviewRepository;
import com.example.repository.StoreRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.servlet.http.HttpSession;

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
            // 가게별 리뷰 조회
            System.out.println("storeId: " + storeId); // storeId 값을 확인

            if (storeId != null && !storeId.isEmpty()) {
                Page<Review> reviewPage = reviewRepository.findReviewsByStoreId(storeId, pageRequest);

                List<Review> reviews = reviewPage.getContent();
                long totalReviews = reviewPage.getTotalElements();

                model.addAttribute("storeId", storeId);
                model.addAttribute("reviews", reviews);
                model.addAttribute("totalReviews", totalReviews);
            } else {
                Page<Review> reviewPage = reviewRepository.findAll(pageRequest);

                List<Review> reviews = reviewPage.getContent();
                long totalReviews = reviewPage.getTotalElements();

                model.addAttribute("reviews", reviews);
                model.addAttribute("totalReviews", totalReviews);
            }
        }
        return "admin/adminhome"; // 관리자 페이지로 이동
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
    public String reviewDeletePOST(@RequestParam(name = "reviewNo") int reviewNo) {
        reviewRepository.deleteById(reviewNo);

        return "redirect:/admin/home.do?menu=3";
    }
}
