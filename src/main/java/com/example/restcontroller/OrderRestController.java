package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Cart;
import com.example.entity.Order;
import com.example.repository.CartRepository;
import com.example.repository.OrderRepository;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderRestController {

    final OrderRepository orderRepository;
    final CartRepository cartRepository;

    final TokenCreate tokenCreate;

    // http://localhost:8080/ROOT/api/order/create
    @PostMapping("/create")
    public Map<String, Object> createOrderPOST(
            @RequestBody List<Integer> cartNos,
            @RequestHeader(name = "Authorization") String token) {

        Map<String, Object> map = new HashMap<>();
        Map<Integer, String> results = new HashMap<>(); // 카트 처리 결과 저장

        String rawToken = token.replace("Bearer", "").trim();
        try {

            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            if (customerEmail == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            for (int cartNo : cartNos) {
                // 카트에서 주문 정보를 가져옴
                Optional<Cart> optionalCart = cartRepository.findById(cartNo);
                if (!optionalCart.isPresent()) {
                    results.put(cartNo, "장바구니를 찾을 수 없습니다.");
                    continue; // 다음 카트 번호 확인
                }

                Cart cart = optionalCart.get(); // 카트가 존재하면 값 가져오기

                // 로그인 사용자와 카트 소유자 일치 여부 확인
                if (!cart.getCustomerEmail().equals(customerEmail)) {
                    results.put(cartNo, "해당 카트에 접근 권한이 없습니다.");
                    continue;
                }

                // 카트 상태가 "주문 완료"인 경우 주문을 받을 수 없도록 처리
                if ("주문 완료".equals(cart.getStatus())) {
                    results.put(cartNo, "이미 주문이 완료된 카트입니다.");
                    continue;
                }
                // 새로운 주문 객체 생성
                Order order = new Order();
                order.setCartno(cart); // 카트 정보 설정

                // 주문 저장
                Order savedOrder = orderRepository.save(order);
                if (savedOrder == null) {
                    results.put(cartNo, "주문 저장 실패");
                    continue;
                }

                // 주문 후 카트 상태 '주문 완료'로 변경
                cart.setStatus("주문 완료");
                cartRepository.save(cart);

                results.put(cartNo, "주문 완료");
            }
            map.put("status", 200);
            map.put("message", "주문 처리 완료");
            map.put("results", results); // 각 카트의 처리 결과 포함

        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", e.getMessage());
        }

        return map;
    }
}
