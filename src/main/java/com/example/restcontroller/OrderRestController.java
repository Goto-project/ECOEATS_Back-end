package com.example.restcontroller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CartRequestDTO;
import com.example.entity.Cart;
import com.example.entity.CustomerMember;
import com.example.entity.DailyMenu;
import com.example.entity.Menu;
import com.example.entity.Order;
import com.example.entity.Pickup;
import com.example.entity.Status;
import com.example.entity.Store;
import com.example.repository.CartRepository;
import com.example.repository.CustomerMemberRepository;
import com.example.repository.DailyMenuRepository;
import com.example.repository.MenuRepository;
import com.example.repository.OrderRepository;
import com.example.repository.PickupRepository;
import com.example.repository.StatusRepository;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderRestController {

    final OrderRepository orderRepository;
    final CartRepository cartRepository;
    final MenuRepository menuRepository;
    final DailyMenuRepository dailyMenuRepository;
    final CustomerMemberRepository customerMemberRepository;
    final PickupRepository pickupRepository;
    final StatusRepository statusRepository;

    final TokenCreate tokenCreate;

    @PostMapping("/cancel")
    public Map<String, Object> cancelOrder(
        @RequestHeader(name = "Authorization") String token,
        @RequestBody String orderNo) {
    Map<String, Object> map = new HashMap<>();
    try {
        // Bearer 접두사를 제거하고 토큰만 추출
        String rawToken = token.replace("Bearer ", "").trim();
        // 토큰 유효성 검사
        Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
        String customerEmail = (String) tokenData.get("customerEmail");

        if (customerEmail == null) {
            map.put("status", 401);
            map.put("message", "유효하지 않은 사용자입니다.");
            return map;
        }

        // 주문을 찾기
        Optional<Order> optionalOrder = orderRepository.findById(orderNo);
        if (!optionalOrder.isPresent()) {
            map.put("status", 404);
            map.put("message", "주문을 찾을 수 없습니다.");
            return map;
        }

        Order order = optionalOrder.get();

        // 주문 상태가 이미 취소되었으면 취소할 수 없음
        // if ("주문 취소".equals(order.getStatus())) {
        //     map.put("status", 400);
        //     map.put("message", "이미 취소된 주문입니다.");
        //     return map;
        // }

        // 주문 상태를 "주문 취소"로 변경

        //

        // 카트 아이템의 수량을 다시 더해주기
        List<Cart> cartItems = cartRepository.findByOrderno(order);
        for (Cart cart : cartItems) {
            Optional<DailyMenu> optDailyMenu = dailyMenuRepository.findById(cart.getDailymenuNo().getDailymenuNo());
            if (optDailyMenu.isPresent()) {
                DailyMenu dailyMenu = optDailyMenu.get();
                dailyMenu.setQty(dailyMenu.getQty() + cart.getQty()); // 취소된 카트 수량만큼 재고 회복
                dailyMenuRepository.save(dailyMenu);
            }
        }

        map.put("status", 200);
        map.put("message", "주문이 성공적으로 취소되었습니다.");
        return map;

    } catch (Exception e) {
        e.printStackTrace();
        map.put("status", -1);
        map.put("message", "서버 오류가 발생했습니다.");
    }
    return map;
}

    // 127.0.0.1:8080/ROOT/api/order/create
    @PostMapping("/create")
    public Map<String, Object> createOrderPOST(
            @RequestHeader(name = "Authorization") String token,
            @RequestBody List<CartRequestDTO> cartRequests) {
        Map<String, Object> map = new HashMap<>();
        try {
            // Bearer 접두사를 제거하고 토큰만 추출
            String rawToken = token.replace("Bearer ", "").trim();
            // 토큰 유효성 검사
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            if (customerEmail == null) {
                map.put("status", 401);
                map.put("message", "유효하지 않은 사용자입니다.");
                return map;
            }

            // 고객 이메일 가져오기 위해 고객 객체 생성
            CustomerMember customerMember = customerMemberRepository.findByCustomerEmail(customerEmail);

            // 주문 번호 생성
            String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            int serialNumber = orderRepository.countByOrdernoStartingWith(date) + 1;
            String orderNo = date + String.format("%04d", serialNumber);

            // 주문 생성
            Order order = new Order();
            order.setOrderno(orderNo); // 생성한 주문 번호 저장
            order.setRegdate(LocalDateTime.now());
            order.setPay(0);
            order.setTotalprice(0);
            order.setCustomeremail(customerMember); // 고객 정보 설정

            // Store 정보 설정
            // 처음 메뉴 정보에서 그 메뉴와 연결된 Store 찾아서 설정
            if (!cartRequests.isEmpty()) {
                int dailyMenuNo = cartRequests.get(0).getDailymenuNo();
                Optional<Menu> optMenu = menuRepository.findById(dailyMenuNo);
                if (optMenu.isPresent()) {
                    Menu menu = optMenu.get();
                    Store store = menu.getStoreId(); // 메뉴에서 Store 정보를 가져옴
                    order.setStoreid(store);
                } else {
                    map.put("status", 404);
                    map.put("message", "메뉴 정보를 찾을 수 없습니다.");
                    return map;
                }
            }

            orderRepository.save(order);

            // status 객체 생성
            Status status = new Status();
            status.setOrderno(order);
            status.setStatus("주문 완료");
            statusRepository.save(status);
            

            // 주문 관련된 pickup 객체 생성
            Pickup pickup = new Pickup();
            pickup.setOrderno(order);
            pickup.setPickup(0);
            pickup.setRegdate(LocalDateTime.now());
            pickupRepository.save(pickup);

            int totalPrice = 0;

            // 카트 저장
            for (CartRequestDTO request : cartRequests) {
                Cart cart = new Cart();
                cart.setDailymenuNo(new DailyMenu(request.getDailymenuNo()));
                cart.setQty(request.getQty());
                cart.setPrice(request.getQty() * request.getPrice());
                cart.setOrderno(order); // 주문에 카트 연결
                cartRepository.save(cart);

                totalPrice += cart.getPrice();

                Optional<DailyMenu> optDailyMenu = dailyMenuRepository.findById(request.getDailymenuNo());
                if (optDailyMenu.isPresent()) {
                    DailyMenu dailyMenu = optDailyMenu.get();
                    if (dailyMenu.getQty() >= request.getQty()) {
                        dailyMenu.setQty(dailyMenu.getQty() - request.getQty()); // 수량 차감
                        dailyMenuRepository.save(dailyMenu); // 변경된 수량 저장
                    } else {
                        map.put("status", 400);
                        map.put("message", "재고가 부족합니다.");
                        return map;
                    }
                } else {
                    map.put("status", 404);
                    map.put("message", "메뉴 정보를 찾을 수 없습니다.");
                    return map;
                }
            }

            // 총 금액 업데이트
            order.setTotalprice(totalPrice);
            orderRepository.save(order);

            map.put("status", 200);
            map.put("message", "주문이 성공적으로 생성되었습니다.");
            map.put("orderId", orderNo);
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류가 발생했습니다.");
        }
        return map;
    }

}
