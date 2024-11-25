package com.example.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;



@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/admin")
public class Admin1Controller {
    
    @Autowired
    HttpSession httpSession;
    
    @GetMapping("/")
    public String getAdminHome() {
        String role = (String) httpSession.getAttribute("role");
        if (!"ADMIN".equals(role)) {
            return "redirect:/page403.do"; // 접근 불가 페이지로 리다이렉트
        }
        return "admin/home"; // 관리자 페이지로 이동
    }
    
}
