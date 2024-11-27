package com.example.restcontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.OrderView;
import com.example.entity.Store;
import com.example.repository.OrderViewRepository;
import com.example.repository.StoreRepository;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/orderview")
public class OrderViewRestController {

    final OrderViewRepository orderViewRepository;
    final StoreRepository storeRepository;
    final TokenCreate tokenCreate;

    // 날짜별로 주문 했던 내역 출력(마이페이지 기능)
    // 127.0.0.1:8080/ROOT/api/orderview/orderbydate    
    @GetMapping("/orderbydate")
    public Map<String, Object> orderbydateGET(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        String rawToken = token.replace("Bearer", "").trim();

    try {
        Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
        Map<String, Object> tokenData1 = tokenCreate.validateSellerToken(rawToken);
        String customerEmail = (String) tokenData.get("customerEmail");
        String storeId = (String) tokenData1.get("storeId");

        if (customerEmail == null && storeId == null) {
            map.put("status", 401);
            map.put("message", "로그인된 사용자 정보가 없습니다.");
            return map;
        }

        // storeId로 상점 이름을 조회 (예시: StoreRepository 사용)
        String storeName = null;
        if (storeId != null) {
            Store store = storeRepository.findByStoreId(storeId);  // StoreRepository로 storeId에 해당하는 Store 엔티티를 조회
            if (store != null) {
                storeName = store.getStoreName();  // store 엔티티에서 상점 이름을 가져옴
            }
        }

        List<OrderView> orderDetails;

        // customerEmail이 유효하면 고객 기준으로 주문 내역 조회
        if (customerEmail != null) {
            orderDetails = orderViewRepository.findByCustomeremail(customerEmail);
        }
        // storeId가 유효하면 가게 기준으로 주문 내역 조회
        else if (storeId != null) {
            orderDetails = orderViewRepository.findByStoreid(storeId);
        }
        // 두 값 모두 없으면 빈 리스트 반환
        else {
            map.put("status", 404);
            map.put("message", "주문 내역이 없습니다.");
            return map;
        }

        if (orderDetails.isEmpty()) {
            map.put("status", 404);
            map.put("message", "주문 내역이 없습니다.");
            return map;
        }

        // 주문 내역을 날짜 및 주문 번호 기준으로 그룹화
        Map<String, Map<String, List<OrderView>>> orderByDateAndOrderNumber = orderDetails.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrdertime().toLocalDate().toString(), // 날짜로 그룹화
                        Collectors.groupingBy(OrderView::getOrdernumber) // 주문번호로 그룹화
                ));

        // 결과 데이터
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> groupOrder = new ArrayList<>();

        // 날짜별로 그룹화된 주문을 처리
        for (Map.Entry<String, Map<String, List<OrderView>>> dateEntry : orderByDateAndOrderNumber.entrySet()) {
            Map<String, Object> dateMap = new HashMap<>();
            dateMap.put("order_date", dateEntry.getKey()); // 날짜

            List<Map<String, Object>> orders = new ArrayList<>();
            int dailyTotal = 0; // 하루치 총 매출 계산

            // 주문 번호별로 그룹화된 주문들 처리
            for (Map.Entry<String, List<OrderView>> orderNumberEntry : dateEntry.getValue().entrySet()) {
                Map<String, Object> orderSummary = new HashMap<>();
                orderSummary.put("order_number", orderNumberEntry.getKey()); // 주문 번호

                List<OrderView> orderList = orderNumberEntry.getValue();
                boolean hasCancelledOrder = orderList.stream()
                        .anyMatch(order -> "주문 취소".equals(order.getOrderstatus()));

                List<OrderView> filteredOrders = hasCancelledOrder
                        ? orderList.stream()
                                .filter(order -> "주문 취소".equals(order.getOrderstatus()))
                                .collect(Collectors.toList())
                        : orderList.stream()
                                .filter(order -> "주문 완료".equals(order.getOrderstatus()))
                                .collect(Collectors.toList());

                // 각 주문 내역을 추가
                List<Map<String, Object>> orderDetailsList = new ArrayList<>();
                int orderTotal = 0; // 각 주문 번호별 합계

                for (OrderView order : filteredOrders) {
                    Map<String, Object> orderDetail = new HashMap<>();
                    orderDetail.put("menu_name", order.getMenuname());
                    orderDetail.put("quantity", order.getQuantity());
                    orderDetail.put("orderstatus", order.getOrderstatus());
                    orderDetail.put("menu_price", order.getDailymenuprice());
                    orderDetail.put("unit_price", order.getUnitprice());
                    // orderDetail.put("store_name", order.getStorename());
                     // 로그인한 사용자가 고객이면 store_name을 포함
                    if (customerEmail != null) {
                        orderDetail.put("store_name", order.getStorename());
                    }
                    orderDetail.put("order_time", order.getOrdertime());

                    // '주문 완료'인 경우 매출 합산
                    if ("주문 완료".equals(order.getOrderstatus())) {
                        orderTotal += order.getUnitprice(); // 각 주문 번호의 총 매출
                    }

                    // '주문 취소'인 경우 매출에서 제외
                    orderDetailsList.add(orderDetail);
                }

                // 주문 번호별 총 금액 추가
                if (!orderDetailsList.isEmpty()) {
                    orderSummary.put("orders", orderDetailsList); // 해당 주문 번호의 주문 내역
                    orderSummary.put("order_total_price", orderTotal); // 주문 번호별 총 금액
                    orders.add(orderSummary);

                    // 하루 총 매출에 더함
                    if ("주문 완료".equals(filteredOrders.get(0).getOrderstatus())) {
                        dailyTotal += orderTotal; // 하루 총 매출 합산
                    }
                }
            }

            // 주문 내역이 있을 때만 추가
            if (!orders.isEmpty()) {
                dateMap.put("orders", orders); // 주문 내역
                dateMap.put("total_price", dailyTotal); // 총 매출

                // storeId로 조회한 경우에만 storename을 포함
                if (storeName != null) {
                    dateMap.put("storename", storeName); // storeId에 해당하는 storeName을 넣음
                }

                groupOrder.add(dateMap); // 날짜별 결과에 추가
            }
        }

        // 날짜별로 내림차순으로 정렬 (최신순)
        groupOrder.sort((a, b) -> b.get("order_date").toString().compareTo(a.get("order_date").toString()));

        result.put("status", 200);
        result.put("data", groupOrder);
        map.put("status", 200);
        map.put("data", result);
    } catch (Exception e) {
        System.out.println(e.getMessage());
        map.put("status", -1);
    }

    return map;
}


@GetMapping("/ordersbystore")
public Map<String, Object> getOrdersByStore(@RequestHeader(name = "Authorization") String token) {
    Map<String, Object> map = new HashMap<>();
    String rawToken = token.replace("Bearer", "").trim();

    try {
        // Token 유효성 검증
        Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
        String customerEmail = (String) tokenData.get("customerEmail");

        if (customerEmail == null) {
            map.put("status", 401);
            map.put("message", "로그인된 사용자 정보가 없습니다.");
            return map;
        }

        // 주문 내역 조회
        List<OrderView> orderDetails = orderViewRepository.findByCustomeremail(customerEmail);

        if (orderDetails.isEmpty()) {
            map.put("status", 404);
            map.put("message", "주문 내역이 없습니다.");
            return map;
        }

        // 주문 내역을 날짜 기준으로 그룹화
        Map<String, Map<String, Map<String, List<OrderView>>>> ordersByDateStoreAndOrderNumber = orderDetails.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrdertime().toLocalDate().toString(), // 날짜로 그룹화
                        Collectors.groupingBy(
                                OrderView::getStorename, // 가게 이름으로 그룹화
                                Collectors.groupingBy(OrderView::getOrdernumber) // 주문 번호로 그룹화
                        )
                ));

        // 결과 데이터
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> groupedOrders = new ArrayList<>();

        // 날짜별로 그룹화된 주문을 처리
        for (Map.Entry<String, Map<String, Map<String, List<OrderView>>>> dateEntry : ordersByDateStoreAndOrderNumber.entrySet()) {
            Map<String, Object> dateMap = new HashMap<>();
            dateMap.put("order_date", dateEntry.getKey()); // 날짜

            List<Map<String, Object>> storeOrders = new ArrayList<>();
            int dailyTotal = 0; // 하루 총 매출 계산

            // 가게별로 그룹화된 주문을 처리
            for (Map.Entry<String, Map<String, List<OrderView>>> storeEntry : dateEntry.getValue().entrySet()) {
                Map<String, Object> storeMap = new HashMap<>();
                storeMap.put("storename", storeEntry.getKey()); // 가게 이름

                List<Map<String, Object>> orders = new ArrayList<>();
                int storeTotal = 0; // 가게별 총 매출 계산

                // 주문 번호별로 그룹화된 주문 처리
                for (Map.Entry<String, List<OrderView>> orderNumberEntry : storeEntry.getValue().entrySet()) {
                    Map<String, Object> orderSummary = new HashMap<>();
                    orderSummary.put("order_number", orderNumberEntry.getKey()); // 주문 번호

                    List<OrderView> orderList = orderNumberEntry.getValue();
                    boolean hasCancelledOrder = orderList.stream()
                            .anyMatch(order -> "주문 취소".equals(order.getOrderstatus()));

                    List<OrderView> filteredOrders = hasCancelledOrder
                            ? orderList.stream()
                                    .filter(order -> "주문 취소".equals(order.getOrderstatus()))
                                    .collect(Collectors.toList())
                            : orderList.stream()
                                    .filter(order -> "주문 완료".equals(order.getOrderstatus()))
                                    .collect(Collectors.toList());

                    // 각 주문 내역 추가
                    List<Map<String, Object>> orderDetailsList = new ArrayList<>();
                    int orderTotal = 0; // 각 주문 번호의 합계 계산

                    for (OrderView order : filteredOrders) {
                        Map<String, Object> orderDetail = new HashMap<>();
                        orderDetail.put("menu_name", order.getMenuname());
                        orderDetail.put("quantity", order.getQuantity());
                        orderDetail.put("orderstatus", order.getOrderstatus());
                        orderDetail.put("menu_price", order.getDailymenuprice());
                        orderDetail.put("unit_price", order.getUnitprice());
                        orderDetail.put("order_time", order.getOrdertime());

                        // '주문 완료'인 경우 매출 합산
                        if ("주문 완료".equals(order.getOrderstatus())) {
                            orderTotal += order.getUnitprice(); // 각 주문 번호의 총 매출
                        }

                        orderDetailsList.add(orderDetail);
                    }

                    // 주문 번호별 총 금액 추가
                    if (!orderDetailsList.isEmpty()) {
                        orderSummary.put("orders", orderDetailsList); // 해당 주문 번호의 주문 내역
                        orderSummary.put("order_total_price", orderTotal); // 주문 번호별 총 금액
                        orders.add(orderSummary);

                        // 가게별 총 매출에 더함
                        storeTotal += orderTotal; // 가게별 매출 합산
                    }
                }

                // 주문 내역이 있을 때만 추가
                if (!orders.isEmpty()) {
                    storeMap.put("orders", orders); // 가게의 주문 내역
                    // storeMap.put("total_price", storeTotal); // 가게별 총 매출
                    storeOrders.add(storeMap);

                    // 하루 총 매출에 더함
                    dailyTotal += storeTotal; // 하루 총 매출 합산
                }
            }

            // 날짜별로 주문 내역이 있을 때만 추가
            if (!storeOrders.isEmpty()) {
                dateMap.put("stores", storeOrders); // 날짜별 주문 내역
                dateMap.put("total_price", dailyTotal); // 하루 총 매출
                groupedOrders.add(dateMap);
            }
        }

        result.put("status", 200);
        result.put("data", groupedOrders);
        map.put("status", 200);
        map.put("data", result);
    } catch (Exception e) {
        map.put("status", -1);
        System.err.println(e.getMessage());
    }

    return map;
}

@GetMapping(value = "/payment")
public Map<String, Object> paymentGET(
        @RequestHeader(name = "Authorization") String token,
        @RequestParam(name = "orderNumber") String orderNumber) {
    Map<String, Object> map = new HashMap<>();
    String rawToken = token.replace("Bearer", "").trim();
    try {
        Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
        String customerEmail = (String) tokenData.get("customerEmail");
        if (customerEmail == null) {
            map.put("status", 401);
            map.put("message", "로그인된 사용자 정보가 없습니다.");
            return map;
        }

        // 특정 주문 번호와 고객 이메일로 주문 내역 조회
        List<OrderView> orderDetails = orderViewRepository.findByCustomeremailAndOrdernumber(customerEmail, orderNumber)
        .stream()
        .filter(order -> "주문 완료".equals(order.getOrderstatus()))
        .collect(Collectors.toList());

        if (orderDetails.isEmpty()) {
            map.put("status", 404);
            map.put("message", "해당 주문 번호에 대한 주문 내역이 없습니다.");
            return map;
        }

        // 영수증 데이터 구성
        Map<String, Object> receipt = new HashMap<>();
        receipt.put("order_number", orderNumber);
        receipt.put("order_date", orderDetails.get(0).getOrdertime());
        receipt.put("customer_email", orderDetails.get(0).getCustomeremail());
        receipt.put("store_name", orderDetails.get(0).getStorename());
        receipt.put("total_price", orderDetails.stream().mapToInt(OrderView::getUnitprice).sum());
        receipt.put("pay_method", orderDetails.get(0).getPaymentstatus());
        receipt.put("start_pickup", orderDetails.get(0).getStartpickup());
        receipt.put("end_pickup", orderDetails.get(0).getEndpickup());

        // 개별 메뉴 정보
        List<Map<String, Object>> menuDetails = new ArrayList<>();
        for (OrderView order : orderDetails) {
            Map<String, Object> menuItem = new HashMap<>();
            menuItem.put("menu_name", order.getMenuname());
            menuItem.put("unit_price", order.getDailymenuprice());
            menuItem.put("quantity", order.getQuantity());
            menuItem.put("menu_total_price", order.getUnitprice());
            menuItem.put("order_status", order.getOrderstatus());
            menuDetails.add(menuItem);
        }
        receipt.put("menu_details", menuDetails);

        map.put("status", 200);
        map.put("data", receipt);
    } catch (Exception e) {
        map.put("status", -1);
        map.put("message", e.getMessage());
        e.printStackTrace();
    }
    return map;
    }
}
