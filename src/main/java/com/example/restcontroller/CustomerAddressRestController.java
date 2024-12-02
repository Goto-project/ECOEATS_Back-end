package com.example.restcontroller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.CustomerAddress;
import com.example.entity.CustomerMember;
import com.example.repository.CustomerAddressRepository;
import com.example.service.CustomerAddressService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/customerAddress")
@RequiredArgsConstructor // Lombok 애너테이션을 사용해 생성자 주입 방식으로 필드 주입
public class CustomerAddressRestController {

    private final CustomerAddressService customerAddressService; // final 필드로 선언된 서비스

    private final CustomerAddressRepository customerAddressRepository;

    // 주소 추가 API
    // 127.0.0.1:8080/ROOT/api/customerAddress/add.json
    // {"postcode":"47291","address":"부산광역시 부산진구 중앙대로
    // 714","addressdetail":"현풍칼국수","customeremail":{"customerEmail":"test1234@test.com"}}
    @PostMapping(value = "/add.json")

    public Map<String, Object> addCustomerAddress(@RequestBody CustomerAddress obj, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 1. JwtFilter에서 설정된 "customerEmail" 속성 가져오기
            String customerEmail = (String) request.getAttribute("customerEmail");
            System.out.println("인증된 사용자 이메일: " + customerEmail);

            // 2. 인증 여부 확인
            if (customerEmail == null) {
                map.put("status", 403);
                map.put("error", "유효하지 않은 토큰입니다. 인증이 필요합니다.");
                return map;
            }

            // 3. CustomerAddress 객체에 사용자 이메일 설정
            CustomerMember customerMember = new CustomerMember();
            customerMember.setCustomerEmail(customerEmail);
            obj.setCustomeremail(customerMember);

            // 4. 서비스에서 위도와 경도 가져오기
            Map<String, BigDecimal> coordinates = customerAddressService.saveCustomerAddress(obj.getAddress());

            // 5. 위도와 경도를 CustomerAddress 객체에 설정
            obj.setLatitude(coordinates.get("latitude"));
            obj.setLongitude(coordinates.get("longitude"));

            // 6. 데이터베이스에 저장
            customerAddressRepository.save(obj);

            // 7. 성공 응답 생성
            map.put("status", 200);
            map.put("data", coordinates);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("error", "주소 추가 중 오류가 발생했습니다.");
        }
        return map;
    }

    // 주소 수정 API (PUT)
    // 127.0.0.1:8080/ROOT/api/customerAddress/update.json
    // {"no":7,"postcode":"47286","address":"부산 부산진구 서면로68번길 13-1
    // ","addressdetail":"오리진커피서면점",
    // "customeremail":{"customerEmail":"test1234@test.com"}}
    @PutMapping(value = "/update.json")
    public Map<String, Object> updateCustomerAddress(@RequestBody CustomerAddress obj, HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 1. JwtFilter에서 설정된 "customerEmail" 속성 가져오기
            String customerEmail = (String) request.getAttribute("customerEmail");
            System.out.println("인증된 사용자 이메일: " + customerEmail);

            // 2. 인증 여부 확인
            if (customerEmail == null) {
                map.put("status", 403);
                map.put("error", "유효하지 않은 토큰입니다. 인증이 필요합니다.");
                return map;
            }

            // 3. 기존 주소 조회
            CustomerAddress existingAddress = customerAddressRepository.findById(obj.getNo())
                    .orElseThrow(() -> new IllegalArgumentException("주소를 찾을 수 없습니다."));

            // 4. 사용자가 자신의 주소인지 확인
            if (!existingAddress.getCustomeremail().getCustomerEmail().equals(customerEmail)) {
                map.put("status", 403);
                map.put("error", "권한이 없습니다. 자신의 주소만 수정할 수 있습니다.");
                return map;
            }

            // 5. 서비스에서 위도와 경도 가져오기 (새로운 주소로 수정)
            Map<String, BigDecimal> coordinates = customerAddressService.saveCustomerAddress(obj.getAddress());

            // 6. 기존 주소 업데이트
            existingAddress.setPostcode(obj.getPostcode());
            existingAddress.setAddress(obj.getAddress()); // 수정할 필드 (주소 등)
            existingAddress.setAddressdetail(obj.getAddressdetail());
            existingAddress.setLatitude(coordinates.get("latitude"));
            existingAddress.setLongitude(coordinates.get("longitude"));

            // 7. 수정된 주소 데이터베이스에 저장
            customerAddressRepository.save(existingAddress);

            // 8. 성공 응답 생성
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
    
    @DeleteMapping(value = "/delete.json")
    public Map<String, Object> deleteCustomerAddress(@RequestParam(name = "no") int no, HttpServletRequest request) {
    Map<String, Object> map = new HashMap<>();
    try {
        // 1. JwtFilter에서 설정된 "customerEmail" 속성 가져오기
        String customerEmail = (String) request.getAttribute("customerEmail");
        System.out.println("인증된 사용자 이메일: " + customerEmail);
        
        // 2. 인증 여부 확인
        if (customerEmail == null) {
            map.put("status", 403);
            map.put("error", "유효하지 않은 토큰입니다. 인증이 필요합니다.");
            return map;
        }

        // 3. 주소 존재 여부 확인
        Optional<CustomerAddress> addressOptional = customerAddressRepository.findById(no);
        if (addressOptional.isEmpty()) {
            map.put("status", 404);
            map.put("error", "해당 주소를 찾을 수 없습니다.");
            return map;
        }
        
        // 4. 주소의 소유자 확인
        CustomerAddress address = addressOptional.get();
        if (!address.getCustomeremail().getCustomerEmail().equals(customerEmail)) {
            map.put("status", 403);
            map.put("error", "이 주소를 삭제할 권한이 없습니다.");
            return map;
        }
        
        // 5. 주소 삭제
        customerAddressRepository.deleteById(no);
        
        // 6. 성공 응답 생성
        map.put("status", 200);
        map.put("message", "주소가 성공적으로 삭제되었습니다.");
        
    } catch (Exception e) {
        e.printStackTrace();
        map.put("status", -1);
        map.put("error", "주소 삭제 중 오류가 발생했습니다.");
    }
    return map;
}

}