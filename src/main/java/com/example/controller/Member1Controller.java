package com.example.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dto.Member1;
import com.example.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/member1" , method = RequestMethod.GET)
public class Member1Controller {
    
    final MemberMapper memberMapper;
    //SecurityConfig에서 환경설정을 확인 후 사용
    BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();



    @GetMapping(value = "/join.do")
    public String joinGET() {
        return "join";
    }

    @PostMapping(value = "/joinaction.do")
    public String joinPOST(@ModelAttribute Member1 obj) {
        //암호는 암호화해서 DB에 보관
        obj.setPw(bcpe.encode(obj.getPw()));
        int ret = memberMapper.insertMember1One(obj);
        
        return "redirect:/home.do";
    }
    
    @GetMapping("/login.do")
    public String loginGET() {
        return "login";
    }
    
    
}
