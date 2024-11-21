package com.example.restcontroller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
            // 1. 서비스에서 위도와 경도 가져오기
            Map<String, BigDecimal> coordinates = customerAddressService.saveCustomerAddress(obj.getAddress());

            // 2. 위도와 경도를 CustomerAddress 객체에 설정
            obj.setLatitude(coordinates.get("latitude"));
            obj.setLongitude(coordinates.get("longitude"));

            // 3. 데이터베이스에 저장
            customerAddressRepository.save(obj);

            // 4. 성공 응답 생성
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


    // 주소 수정 API (PUT)
    @PutMapping("/update")
    public Map<String, Object> updateCustomerAddress(@RequestBody CustomerAddress obj) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 1. 기존 주소 조회
            CustomerAddress existingAddress = customerAddressRepository.findById(obj.getNo())
                .orElseThrow(() -> new IllegalArgumentException("주소를 찾을 수 없습니다."));

            // 2. 서비스에서 위도와 경도 가져오기 (새로운 주소로 수정)
            Map<String, BigDecimal> coordinates = customerAddressService.saveCustomerAddress(obj.getAddress());

            // 3. 기존 주소 업데이트
            existingAddress.setAddress(obj.getAddress());  // 수정할 필드 (주소 등)
            existingAddress.setAddressdetail(obj.getAddressdetail());
            existingAddress.setLatitude(coordinates.get("latitude"));
            existingAddress.setLongitude(coordinates.get("longitude"));

            // 4. 수정된 주소 데이터베이스에 저장
            customerAddressRepository.save(existingAddress);

            // 5. 성공 응답 생성
            map.put("status", 200);
            map.put("data", coordinates);
        } catch (IllegalArgumentException e) {
            map.put("status", -1);
            map.put("error", e.getMessage());
        } catch (Exception e) {
            map.put("status", -1);
            map.put("error", "주소를 수정하는 중 오류가 발생했습니다.");
        }
        return map;
    }
    
}