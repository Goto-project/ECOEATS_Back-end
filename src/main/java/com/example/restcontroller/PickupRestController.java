package com.example.restcontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Pickup;
import com.example.repository.PickupRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/pickup")
@RequiredArgsConstructor
public class PickupRestController {
    
    final PickupRepository pickupRepository;
    @PutMapping("/update/{pickupNo}")
    public Map<String, Object> updatePickupPUT(@PathVariable int pickupNo) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 특정 Pickup 조회
            Optional<Pickup> optionalPickup = pickupRepository.findById(pickupNo);
            if (optionalPickup.isPresent()) {
                Pickup pickup = optionalPickup.get();
                pickup.setPickup(1); // pickup 상태를 1로 설정
                pickupRepository.save(pickup); // 변경 사항 저장

                response.put("status", 200);
                response.put("message", "픽업 상태가 성공적으로 변경되었습니다.");
            } else {
                response.put("status", 404);
                response.put("message", "해당 픽업 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            response.put("status", 500);
            response.put("message", "서버 오류가 발생했습니다.");
            response.put("error", e.getMessage());
        }
        return response;
    }
}
