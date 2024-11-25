package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.entity.Admin;
import com.example.repository.AdminRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


//react와 연동하지않는 페이지 (ex 판매자 , 식당)
@Controller
public class HomeController {
    
    @Autowired
    AdminRepository adminRepository;

    @Autowired
    HttpSession httpSession;

    BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
    
    @GetMapping(value = "/join.do")
    public String joinGET() {
        return "join";
    }
    
    @PostMapping(value = "/joinaction.do")
    public String joinPOST(@ModelAttribute Admin obj) {
        System.out.println(obj.toString());
        
        // 암호는 암호화해서 DB에 보관
        obj.setPassword(bcpe.encode(obj.getPassword()));
        adminRepository.save(obj);
        
        return "redirect:/login.do";
    }
    

    @GetMapping("/login.do")
    public String loginGET() {
        return "login"; 
    }

    @PostMapping("/login.do")
    public String loginPOST(@ModelAttribute Admin obj) {
        System.out.println(obj.toString());

        // 입력받은 ID로 관리자 계정 검색
        Admin admin = adminRepository.findById(obj.getAdminId()).orElse(null);

        if (admin != null) {
            // 비밀번호 비교 (암호화된 비밀번호와 평문 비교)
            if (bcpe.matches(obj.getPassword(), admin.getPassword())) {
                // 세션에 값 저장
                httpSession.setAttribute("login", 1);
                httpSession.setAttribute("loginid", obj.getAdminId());
                httpSession.setAttribute("role", "ROLE_ADMIN"); // role 저장
                System.out.println("로그인 성공: 세션 저장 완료");
                return "redirect:/admin/home"; // 홈 페이지로 이동
            } else {
                System.out.println("비밀번호가 일치하지 않습니다.");
            }
        } else {
            System.out.println("관리자 계정을 찾을 수 없습니다.");
        }

    // 로그인 실패 시 다시 로그인 페이지로 이동 (오류 메시지 전달)
    return "redirect:/login.do?error=invalid";
}
    

    @PostMapping(value = "/logout.do")
    public String logoutPOST() {
        httpSession.invalidate();
        
        return "redirect:/login.do";
    }
    
    // 비밀번호 재설정 요청 화면
    @GetMapping("/forgotpassword.do")
    public String forgotPasswordGET() {

        return "forgotpassword"; // 비밀번호 재설정 화면
    }

    // 비밀번호 재설정 처리
    @PostMapping("/reset-password.do")
    public String resetPasswordPOST(@RequestParam("adminId") String adminId,
                                    @RequestParam("newPassword") String newPassword) {
        // 입력받은 ID로 관리자 계정 검색
        Admin admin = adminRepository.findById(adminId).orElse(null);

        if (admin == null) {
            // ID가 없는 경우 처리
            System.out.println("해당 ID의 관리자가 존재하지 않습니다.");
            return "redirect:/forgot-password?error=notfound";
        }

        // 비밀번호 업데이트 (암호화 적용)
        admin.setPassword(bcpe.encode(newPassword));
        adminRepository.save(admin);

        System.out.println("비밀번호가 성공적으로 변경되었습니다.");
        return "redirect:/login.do?reset=success"; // 로그인 페이지로 리다이렉트
    }

    //127.0.0.1:8080/ROOT/home.do
    // @GetMapping(value = {"/" , "/home.do" , "/main.do"})
    // public String homeGet(){
    //     return "home";
    // }

    
    // //react => build => 표시
    // @GetMapping(value = "/customer.do")
    // public String customer() {
    //     return "forward:/react1/index.html";
    // }

    // //react => build => 표시
    // @GetMapping(value = "/page403.do")
    // public String page403() {
    //     return "page403";
    // }

    // @GetMapping(value = "/sse.do")
    // public String sse() {
    //     return "sse";
    // }
    
    
}