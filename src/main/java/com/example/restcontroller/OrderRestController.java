package com.example.restcontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Cart;
import com.example.entity.Order;
import com.example.repository.CartRepository;
import com.example.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderRestController {

    final OrderRepository orderRepository;
    final CartRepository cartRepository;

    @PostMapping("/create/{cartNo}")
    public Map<String, Object> createOrderPOST(@PathVariable int cartNo) {

        Map<String, Object> map = new HashMap<>();

        // 카트에서 주문 정보를 가져옴
        Optional<Cart> optionalCart = cartRepository.findById(cartNo);
        if (!optionalCart.isPresent()) {
            map.put("status", -1);
            map.put("message", "장바구니를 찾을 수 없습니다.");
            return map;
        }

        Cart cart = optionalCart.get(); // 카트가 존재하면 값 가져오기

        try {

            // 새로운 주문 객체 생성
            Order order = new Order();
            order.setCartno(cart); // 카트 정보 설정

            // 주문 저장
            Order savedOrder = orderRepository.save(order);
            if (savedOrder == null) {
                map.put("status", -1);
                map.put("message", "주문 저장 실패");
                return map;  // 실패 시 바로 반환
            }

            // 주문 후 카트 삭제
            cartRepository.delete(cart);

            map.put("message", "주문 완료");
            map.put("order", savedOrder); // 생성된 주문 정보 포함

        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", e.getMessage());
        }

        return map;
    }
}
