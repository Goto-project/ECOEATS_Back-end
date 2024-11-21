package com.example.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.entity.MenuImage;
import com.example.entity.StoreImage;
import com.example.repository.MenuImageRepository;
import com.example.repository.StoreImageRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping(value = "/store")
@RequiredArgsConstructor
public class StoreController {

    final StoreImageRepository storeImageRepository;
    final MenuImageRepository menuImageRepository;
    final ResourceLoader resourceLoader;

    @GetMapping(value = "/home.do")
    public String home(@AuthenticationPrincipal User user) {
        System.out.println(user.toString());
        return "store_home";
    }

    // http://127.0.0.1:8080/ROOT/store/image?no=3
    // 리액트: <img th:src="/ROOT/store/image?no=1" />
    @GetMapping(value = "/image")
    public ResponseEntity<byte[]> imagePreview(@RequestParam(name = "no", defaultValue = "0") int no) throws IOException {
        StoreImage obj = storeImageRepository.findById(no).orElse(null);
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<byte[]> response = null;

        // DB에 이미지가 있는 경우
        if (obj != null) {
            if (obj.getFiledata().length > 0) {
                headers.setContentType(MediaType.parseMediaType(obj.getFiletype()));
                response = new ResponseEntity<>(obj.getFiledata(), headers, HttpStatus.OK);
                System.out.println(response.toString());
                return response;
            }
        }

            // DB에 이미지가 없는 경우 : 기본 이미지 표시
            InputStream in = resourceLoader.getResource("classpath:/static/img/default.png").getInputStream();
            headers.setContentType(MediaType.IMAGE_PNG);
            response = new ResponseEntity<>(in.readAllBytes(), headers, HttpStatus.OK);


        return response;
    }

    // http://127.0.0.1:8080/ROOT/store/menuimage?no=3
    // 리액트: <img th:src="/ROOT/store/menuimage?no=1" />
    @GetMapping(value = "/menuimage")
    public ResponseEntity<byte[]> menuImagePreview(@RequestParam(name = "no", defaultValue = "0") int no) throws IOException {
        MenuImage obj = menuImageRepository.findById(no).orElse(null);
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<byte[]> response = null;

        // DB에 이미지가 있는 경우
        if (obj != null) {
            if (obj.getFiledata().length > 0) {
                headers.setContentType(MediaType.parseMediaType(obj.getFiletype()));
                response = new ResponseEntity<>(obj.getFiledata(), headers, HttpStatus.OK);
                System.out.println(response.toString());
                return response;
            }
        }

            // DB에 이미지가 없는 경우 : 기본 이미지 표시
            InputStream in = resourceLoader.getResource("classpath:/static/img/default.png").getInputStream();
            headers.setContentType(MediaType.IMAGE_PNG);
            response = new ResponseEntity<>(in.readAllBytes(), headers, HttpStatus.OK);


        return response;
    }
}
