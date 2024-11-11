package com.example.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.BookMark;
import com.example.entity.CustomerMember;
import com.example.entity.Store;
import com.example.repository.BookMarkRepository;
import com.example.repository.CustomerMemberRepository;
import com.example.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/bookmark")
@RequiredArgsConstructor
public class BookMarkRestController {
    
    final BookMarkRepository bookMarkRepository;
    final CustomerMemberRepository customerMemberRepository;
    final StoreRepository storeRepository;


    

}
