package com.example.restcontroller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.CustomerAddress;
import com.example.service.CustomerAddressService;

import lombok.RequiredArgsConstructor;


// 오늘 할 섯 서비스 저장하지말고 map로 반환시키기 위도 경도
// 받아온 것으로 컨트롤러에 저장


@RestController
@RequestMapping(value = "/api/customerAddress")
@RequiredArgsConstructor  // Lombok 애너테이션을 사용해 생성자 주입 방식으로 필드 주입
public class CustomerAddressRestController {

    private final CustomerAddressService customerAddressService;  // final 필드로 선언된 서비스

    // 주소 추가 API
    @PostMapping("/add")
    public Map<String, Object> addCustomerAddress(@RequestBody CustomerAddress obj) {
        System.out.println(obj.toString());
        Map<String, Object> map = new HashMap<>();
        try {
            customerAddressService.saveCustomerAddress(obj.getAddress()+obj.getAddressdetail(), obj.getCustomeremail().getCustomerEmail());  // 주소 저장


            map.put("status", 200);
             // 성공 메시지
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "리뷰 수정 중 오류가 발생했습니다.");
        }
        return map;
    }
    
}