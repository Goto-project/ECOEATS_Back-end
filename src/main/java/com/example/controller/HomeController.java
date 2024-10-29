package com.example.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//react와 연동하지않는 페이지 (ex 판매자 , 식당)
@Controller
public class HomeController {
    
    //127.0.0.1:8080/ROOT/home.do
    @GetMapping(value = {"/" , "/home.do" , "/main.do"})
    public String homeGet(){
        return "home";
    }

    //react => build => 표시
    @GetMapping(value = "/customer.do")
    public String customer() {
        return "forward:/react1/index.html";
    }
    
}