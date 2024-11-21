package com.example.restcontroller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.CustomerAddress;
import com.example.repository.CustomerAddressRepository;
import com.example.service.CustomerAddressService;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping(value = "/api/customerAddress")
@RequiredArgsConstructor  // Lombok 애너테이션을 사용해 생성자 주입 방식으로 필드 주입
public class CustomerAddressRestController {

    private final CustomerAddressService customerAddressService;  // final 필드로 선언된 서비스

    private final CustomerAddressRepository customerAddressRepository;

    // 주소 추가 API
    @PostMapping("/add")
    public Map<String, Object> addCustomerAddress(@RequestBody CustomerAddress obj) {
        System.out.println(obj.toString());
        Map<String, Object> map = new HashMap<>();
        try {
            Map<String, BigDecimal> coordinates = customerAddressService.saveCustomerAddress(obj.getAddress() );

            obj.setLatitude(coordinates.get("latitude") );
            obj.setLongitude(coordinates.get("longitude"));



            
    
            map.put("status", 200);
            map.put("data", coordinates);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "위도와 경도를 반환하는 중 오류가 발생했습니다.");
        }
        return map;
    }
    
}