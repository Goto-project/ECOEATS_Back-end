package com.example.restcontroller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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
import com.example.entity.OrderView;
import com.example.entity.Pickup;
import com.example.entity.Status;
import com.example.entity.Store;
import com.example.repository.CartRepository;
import com.example.repository.CustomerMemberRepository;
import com.example.repository.DailyMenuRepository;
import com.example.repository.MenuRepository;
import com.example.repository.OrderRepository;
import com.example.repository.OrderViewRepository;
import com.example.repository.PickupRepository;
import com.example.repository.StatusRepository;
import com.example.repository.StoreRepository;
import com.example.service.KakaoPayService;
import com.example.token.TokenCreate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RequestMapping("/api/order")
public class OrderRestController {

    final OrderRepository orderRepository;
    final CartRepository cartRepository;
    final MenuRepository menuRepository;
    final DailyMenuRepository dailyMenuRepository;
    final CustomerMemberRepository customerMemberRepository;
    final PickupRepository pickupRepository;
    final StatusRepository statusRepository;
    final StoreRepository storeRepository;
    final OrderViewRepository orderViewRepository;

    final TokenCreate tokenCreate;

    final KakaoPayService kakaoPayService;
    final RestTemplate restTemplate;


    // 매장별 오늘 주문 목록 조회
@GetMapping("/today")
public List<Map<String, Object>> getOrdersByStoreToday(@RequestHeader(name = "Authorization") String token) {
    List<Map<String, Object>> resultList = new ArrayList<>();
    String rawToken = token.replace("Bearer ", "").trim();

    try {
        // 토큰 검증 및 매장 정보 가져오기
        Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
        String storeId = (String) tokenData.get("storeId");

        if (storeId == null) {
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", 401);
            errorMap.put("message", "유효하지 않은 매장 정보입니다.");
            resultList.add(errorMap);
            return resultList;
        }

        // 오늘 날짜 범위
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 매장 ID와 날짜 범위로 모든 주문 상태 조회
        List<OrderView> orders = orderViewRepository.findByStoreidAndOrdertimeBetween(storeId, startOfDay, endOfDay);

        // 주문 상태 최신화: 각 orderNo별로 가장 최근 상태 가져오기
        Map<String, OrderView> latestOrders = new HashMap<>();
        for (OrderView order : orders) {
            String orderNo = order.getOrdernumber();
            if (!latestOrders.containsKey(orderNo) || 
                latestOrders.get(orderNo).getOrdertime().isBefore(order.getOrdertime())) {
                latestOrders.put(orderNo, order); // 최신 상태 업데이트
            }
        }

        // 결과 리스트 구성
        for (OrderView order : latestOrders.values()) {
            Map<String, Object> orderMap = new HashMap<>();
            orderMap.put("ordernumber", order.getOrdernumber());
            orderMap.put("paymentstatus", order.getPaymentstatus());
            orderMap.put("totalprice", order.getTotalprice());
            orderMap.put("customeremail", order.getCustomeremail());
            orderMap.put("orderstatus", order.getOrderstatus());
            orderMap.put("orderTime", order.getOrdertime());
            orderMap.put("storename", order.getStorename());
            orderMap.put("menuname", order.getMenuname());
            orderMap.put("dailymenuprice", order.getDailymenuprice());
            orderMap.put("quantity", order.getQuantity());
            orderMap.put("unitprice", order.getUnitprice());
            orderMap.put("pickupstatus", order.getPickupstatus());
            orderMap.put("startpickup", order.getStartpickup());
            orderMap.put("endpickup", order.getEndpickup());
            orderMap.put("storeid", order.getStoreid());

            resultList.add(orderMap);
        }

        return resultList;

    } catch (Exception e) {
        e.printStackTrace();
        Map<String, Object> errorMap = new HashMap<>();
        errorMap.put("status", 500);
        errorMap.put("message", "서버 오류가 발생했습니다.");
        resultList.add(errorMap);
        return resultList;
    }
}




    // 127.0.0.1:8080/ROOT/api/order/sellercancel
    @PostMapping("/sellercancel")
    public Map<String, Object> sellercancelOrder(
        @RequestHeader(name = "Authorization") String token,
        @RequestBody OrderDTO orderDTO) {
            Map<String, Object> map = new HashMap<>();
            try {
                // orderNo를 DTO에서 추출
                String orderNo = orderDTO.getOrderNo();
        
                // Bearer 접두사를 제거하고 토큰만 추출
                String rawToken = token.replace("Bearer ", "").trim();
                // 토큰 유효성 검사
                Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
                String storeId = (String) tokenData.get("storeId");
        
                if (storeId == null) {
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
        
                // 주문의 가게(storeid)와 로그인한 판매자(storeId)가 일치하는지 확인
                if (!order.getStoreid().getStoreId().equals(storeId)) {
                    map.put("status", 403); // 권한 없음
                    map.put("message", "이 주문은 해당 판매자가 관리하는 주문이 아닙니다.");
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
        
                // **pay 값에 따른 처리**
                if (order.getPay() == 1) {
                    // 카카오페이 결제 취소 로직 호출
                    try {
                        Map<String, String> kakaoCancelResponse = kakaoPayService.kakaoPayCancel(orderNo);
                        if (!"200".equals(kakaoCancelResponse.get("status"))) {
                            map.put("status", 500);
                            map.put("message", "카카오페이 결제 취소 중 오류가 발생했습니다.");
                            return map;
                        }
                        map.put("kakaoPayResponse", kakaoCancelResponse);
                    } catch (Exception e) {
                        e.printStackTrace();
                        map.put("status", 500);
                        map.put("message", "카카오페이 결제 취소 중 오류가 발생했습니다.");
                        return map;
                    }
                } else {
                    // 주문 상태를 "주문 취소"로 변경
                    Status cancelStatus = new Status();
                    cancelStatus.setOrderno(order);
                    cancelStatus.setStatus("주문 취소");
                    statusRepository.save(cancelStatus);
                }


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







    // 127.0.0.1:8080/ROOT/api/order/cancel
    @PostMapping("/cancel")
    public Map<String, Object> cancelOrder(
            @RequestHeader(name = "Authorization") String token,
            @RequestBody OrderDTO orderDTO) {
        Map<String, Object> map = new HashMap<>();
        try {
            // orderNo를 DTO에서 추출
            String orderNo = orderDTO.getOrderNo();

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

            // **pay 값에 따른 처리**
            if (order.getPay() == 1) {
                // 카카오페이 결제 취소 로직 호출
                try {
                    Map<String, String> kakaoCancelResponse = kakaoPayService.kakaoPayCancel(orderNo);
                    if (!"200".equals(kakaoCancelResponse.get("status"))) {
                        map.put("status", 500);
                        map.put("message", "카카오페이 결제 취소 중 오류가 발생했습니다.");
                        return map;
                    }
                    map.put("kakaoPayResponse", kakaoCancelResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    map.put("status", 500);
                    map.put("message", "카카오페이 결제 취소 중 오류가 발생했습니다.");
                    return map;
                }
            } else {
                // 주문 상태를 "주문 취소"로 변경
                Status cancelStatus = new Status();
                cancelStatus.setOrderno(order);
                cancelStatus.setStatus("주문 취소");
                statusRepository.save(cancelStatus);
            }

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

    private void saveOrderDetails(Order order, OrderRequestDTO orderRequest) {
        // 상태 객체 생성
        Status status = new Status();
        status.setOrderno(order);
        status.setStatus("주문 완료");
        statusRepository.save(status);

        // 주문 관련된 픽업 객체 생성
        Pickup pickup = new Pickup();
        pickup.setOrderno(order);
        pickup.setPickup(0);
        pickup.setRegdate(LocalDateTime.now());
        pickupRepository.save(pickup);

        // 카트 저장
        int totalPrice = 0;
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

                // 재고 차감
                dailyMenu.setQty(dailyMenu.getQty() - request.getQty());
                dailyMenuRepository.save(dailyMenu); // 변경된 재고 저장

                cart.setOrderno(order); // 주문에 카트 연결
                cartRepository.save(cart); // 저장

                totalPrice += cart.getPrice();
            }
        }

        // 총 금액 업데이트
        order.setTotalprice(totalPrice);
        orderRepository.save(order);
    }

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
            Optional<Store> optionalStore = storeRepository.findById(orderRequest.getStoreid());
            if (optionalStore.isPresent()) {
                order.setStoreid(optionalStore.get());
            } else {
                map.put("status", 404);
                map.put("message", "가게 정보를 찾을 수 없습니다.");
                return map;
            }

            boolean hasStockIssue = false;

            // 먼저 재고를 확인하고, 부족한 경우 전체 주문 취소
            for (CartRequestDTO request : orderRequest.getCartRequests()) {
                Optional<DailyMenu> optDailyMenu = dailyMenuRepository.findById(request.getDailymenuNo());

                if (optDailyMenu.isPresent()) {
                    if (optDailyMenu.get().getQty() < request.getQty()) {
                        hasStockIssue = true;
                        break;
                    }
                } else {
                    map.put("status", 404);
                    map.put("message", "메뉴 정보를 찾을 수 없습니다.");
                    return map;
                }
            }

            // 재고 부족 시 전체 주문 취소
            if (hasStockIssue) {
                map.put("status", 400);
                map.put("message", "하나 이상의 메뉴에 대해 재고가 부족합니다.");
                return map;
            }

            int totalPrice = 0;
            for (CartRequestDTO request : orderRequest.getCartRequests()) {
                Optional<DailyMenu> optDailyMenu = dailyMenuRepository.findById(request.getDailymenuNo());
                if (optDailyMenu.isPresent()) {
                    DailyMenu dailyMenu = optDailyMenu.get();
                    totalPrice += request.getQty() * dailyMenu.getPrice();
                } else {
                    map.put("status", 404);
                    map.put("message", "메뉴 정보를 찾을 수 없습니다.");
                    return map;
                }
            }

            // 총 금액을 order 객체에 저장
            order.setTotalprice(totalPrice);

            orderRepository.save(order);

            // pay가 0일 경우
            if (order.getPay() == 0) {
                order.setTid("none"); // TID를 기본값 설정
                // 상태, 픽업, 카트 저장
                saveOrderDetails(order, orderRequest);
            }

            if (order.getPay() == 1) { // 카카오페이
                KakaoPayService kakaoPayService = new KakaoPayService(restTemplate, orderRepository, statusRepository);
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
    public Map<String, Object> kakaoPaySuccess(
            @RequestParam("orderno") String orderno,
            @RequestParam("pg_token") String pgToken,
            @RequestBody OrderRequestDTO orderRequest) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 결제 승인
            Order order = orderRepository.findByOrderno(orderno);
            KakaoPayService kakaoPayService = new KakaoPayService(restTemplate, orderRepository, statusRepository);

            // 결제 승인 처리
            Map<String, String> approvalResponse = kakaoPayService.kakaoPayApprove(pgToken, orderno);

            // 상태, 픽업, 카트 데이터 저장
            saveOrderDetails(order, orderRequest);

            map.put("status", 200);
            map.put("message", "결제가 성공적으로 완료되었습니다.");
            map.put("paymentDetails", approvalResponse); // 결제 상세 정보 포함
            map.put("orderDetails", order); // 주문 정보 포함 가능
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "결제 승인에 실패했습니다.");
        }
        return map;
    }

    // @PostMapping("/kakaoPayCancel")
    // public Map<String, Object> kakaoPayCancel(@RequestParam("orderno") String orderno) {
    //     Map<String, Object> map = new HashMap<>();
    //     map.put("status", 400);
    //     map.put("message", "결제가 취소되었습니다.");
    //     return map;
    // }

    @PostMapping("/kakaoPayFail")
    public Map<String, Object> kakaoPayFail(@RequestParam("orderno") String orderno) {
        Map<String, Object> map = new HashMap<>();
        orderRepository.findByOrderno(orderno);
        map.put("status", 400);
        map.put("message", "결제에 실패했습니다.");
        return map;
    }

}
