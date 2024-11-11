package com.example.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/seller")
public class SellerController {
    
    @GetMapping(value = "/home.do")
    public String home(@AuthenticationPrincipal User user){
        System.out.println(user.toString());
        return "seller_home";
    }
}
