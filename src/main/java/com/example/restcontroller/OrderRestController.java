package com.example.restcontroller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.example.dto.CartRequestDTO;
import com.example.dto.OrderDTO;
import com.example.dto.OrderRequestDTO;
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
import com.example.service.KakaoPayService;
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

    final KakaoPayService kakaoPayService;
    final RestTemplate restTemplate;

    // 127.0.0.1:8080/ROOT/api/order/cancel
    @PostMapping("/cancel")
    public Map<String, Object> cancelOrder(
            @RequestHeader(name = "Authorization") String token,
            @RequestBody OrderDTO orderDTO) {
        Map<String, Object> map = new HashMap<>();
        try {
            // orderNo를 DTO에서 추출
            String orderNo = orderDTO.getOrderNo();
            System.out.println("orderNo: " + orderNo);

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

            // 로그인한 사용자가 주문의 소유자인지 확인
            if (!order.getCustomeremail().getCustomerEmail().equals(customerEmail)) {
                map.put("status", 403);
                map.put("message", "사용자의 주문이 아닙니다.");
                return map;
            }

            // 주문 상태가 이미 취소되었으면 취소할 수 없음
            Optional<Status> latestStatus = statusRepository.findTopByOrdernoOrderByRegdateDesc(order);
            Status status = latestStatus.orElse(null);
            if (status != null && "주문 취소".equals(status.getStatus())) {
                map.put("status", 400);
                map.put("message", "이미 취소된 주문입니다.");
                return map;
            }

            // 주문 상태를 "주문 취소"로 변경
            Status cancelStatus = new Status();
            cancelStatus.setOrderno(order);
            cancelStatus.setStatus("주문 취소");
            statusRepository.save(cancelStatus);

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
            @RequestBody OrderRequestDTO orderRequest) {
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
            order.setPay(orderRequest.getPay());
            order.setTotalprice(0);
            order.setCustomeremail(customerMember); // 고객 정보 설정

            // Store 정보 설정
            // 처음 메뉴 정보에서 그 메뉴와 연결된 Store 찾아서 설정
            if (!orderRequest.getCartRequests().isEmpty()) {
                int dailyMenuNo = orderRequest.getCartRequests().get(0).getDailymenuNo();
                System.out.println("Requested DailyMenuNo: " + dailyMenuNo); // 디버깅용 출력
                Optional<DailyMenu> optDailyMenu = dailyMenuRepository.findById(dailyMenuNo);

                if (optDailyMenu.isPresent()) {
                    DailyMenu dailyMenu = optDailyMenu.get();
                    Menu menu = dailyMenu.getMenuNo(); // DailyMenu에서 Menu 정보를 가져옴
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
            for (CartRequestDTO request : orderRequest.getCartRequests()) {
                Cart cart = new Cart();
                cart.setDailymenuNo(new DailyMenu(request.getDailymenuNo()));
                cart.setQty(request.getQty());

                Optional<DailyMenu> optDailyMenu = dailyMenuRepository.findById(request.getDailymenuNo());

                if (optDailyMenu.isPresent()) {
                    DailyMenu dailyMenu = optDailyMenu.get();

                    // dailymenu의 가격과 주문 수량 곱해서 cart price 계산
                    int menuPrice = dailyMenu.getPrice();
                    cart.setPrice(request.getQty() * menuPrice);

                    // 재고 확인 및 차감
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

                cart.setOrderno(order); // 주문에 카트 연결
                cartRepository.save(cart); // 저장

                totalPrice += cart.getPrice();
            }

            // 총 금액 업데이트
            order.setTotalprice(totalPrice);
            orderRepository.save(order);

            if (order.getPay() == 1) { // 카카오페이
                KakaoPayService kakaoPayService = new KakaoPayService(restTemplate, orderRepository);
                Map<String, String> kakaoPayResponse = kakaoPayService.kakaoPayReady(order);
                map.put("paymentUrl", kakaoPayResponse.get("next_redirect_pc_url"));
            }

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

    @PostMapping("/kakaoPaySuccess")
    public Map<String, Object> kakaoPaySuccess(@RequestParam("orderno") String orderno,
            @RequestParam("pgToken") String pgToken) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 결제 승인
            Order order = orderRepository.findByOrderno(orderno);
            KakaoPayService kakaoPayService = new KakaoPayService(restTemplate, orderRepository);
            Map<String, String> approvalResponse = kakaoPayService.kakaoPayApprove(order.getTid(), pgToken, order);

            // 결제 승인 처리 후 상태 업데이트
            order.setPay(1); // 결제 완료 상태
            orderRepository.save(order);

            map.put("status", 200);
            map.put("message", "결제가 성공적으로 완료되었습니다.");
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "결제 승인에 실패했습니다.");
        }
        return map;
    }

    @PostMapping("/kakaoPayCancel")
    public Map<String, Object> kakaoPayCancel(@RequestParam("orderno") String orderno) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 400);
        map.put("message", "결제가 취소되었습니다.");
        return map;
    }

    @PostMapping("/kakaoPayFail")
    public Map<String, Object> kakaoPayFail(@RequestParam("orderno") String orderno) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", 400);
        map.put("message", "결제에 실패했습니다.");
        return map;
    }

}
