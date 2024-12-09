package com.example.restcontroller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping("/monthlySales")
    public Map<String, Object> getMonthlySales(
            @RequestParam String month, // yyyy-MM 형식
            @RequestHeader(name = "Authorization") String token) {

        Map<String, Object> response = new HashMap<>();
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");

            if (storeId == null) {
                response.put("status", 401);
                response.put("message", "로그인된 매장 정보가 없습니다.");
                return response;
            }

            YearMonth yearMonth = YearMonth.parse(month); // yyyy-MM
            LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            List<OrderView> orders = orderViewRepository.findByStoreidAndOrdertimeBetween(storeId, start, end);

            Map<String, Integer> dailySales = new HashMap<>();
            int totalMonthlySales = 0;

            for (OrderView order : orders) {
                // String orderDate = order.getOrdertime().toLocalDate().toString();
                // dailySales.put(orderDate, dailySales.getOrDefault(orderDate, 0) +
                // order.getTotalprice());
                // totalMonthlySales += order.getTotalprice();
                // 주문 상태가 "주문 완료"인 경우에만 처리
                if ("주문 완료".equals(order.getOrderstatus())) {
                    String orderDate = order.getOrdertime().toLocalDate().toString();
                    dailySales.put(orderDate, dailySales.getOrDefault(orderDate, 0) + order.getUnitprice());
                    totalMonthlySales += order.getUnitprice();
                }
            }

            response.put("status", 200);
            response.put("dailySales", dailySales);
            response.put("totalMonthlySales", totalMonthlySales);

        } catch (Exception e) {
            response.put("status", -1);
            response.put("message", "오류 발생");
        }

        return response;
    }

    @GetMapping("/datestore")
    public Map<String, Object> getOrdersByStoreAndDate(
            @RequestParam String date, // 특정 날짜 (yyyy-MM-dd)
            @RequestHeader(name = "Authorization") String token) {

        Map<String, Object> response = new HashMap<>();

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            // 토큰 검증 후, 매장 ID 추출
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");

            // storeId가 null인 경우
            if (storeId == null) {
                response.put("status", 401); // 인증되지 않은 요청
                response.put("message", "로그인된 매장 정보가 없습니다.");
                return response;
            }

            // 날짜 변환
            LocalDateTime start = LocalDate.parse(date).atStartOfDay(); // 시작일
            LocalDateTime end = start.plusDays(1).minusSeconds(1); // 해당 날짜의 종료 시간

            // 매장 ID와 날짜에 맞는 주문 내역 조회
            List<OrderView> orders = orderViewRepository.findByStoreidAndOrdertimeBetween(storeId, start, end);

            // 주문 내역이 없을 경우
            if (orders.isEmpty()) {
                response.put("status", 200);
                response.put("message", "해당 날짜에 해당하는 주문 내역이 없습니다.");
                return response;
            }

            // 최신순 정렬
            orders.sort((o1, o2) -> o2.getOrdertime().compareTo(o1.getOrdertime()));

            // 주문 내역 처리 및 총 가격 계산
            List<Map<String, Object>> orderList = new ArrayList<>();
            int totalPrice = 0;

            for (OrderView order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("ordernumber", order.getOrdernumber());
                orderMap.put("paymentstatus", order.getPaymentstatus());
                orderMap.put("totalprice", order.getTotalprice());
                orderMap.put("orderstatus", order.getOrderstatus());
                orderMap.put("ordertime", order.getOrdertime());
                orderMap.put("storename", order.getStorename());
                orderMap.put("menuname", order.getMenuname());
                orderMap.put("dailymenuprice", order.getDailymenuprice());
                orderMap.put("quantity", order.getQuantity());
                orderMap.put("unitprice", order.getUnitprice());
                orderMap.put("pickupstatus", order.getPickupstatus());
                orderMap.put("pickuptime", order.getPickuptime());

                totalPrice += order.getTotalprice();
                orderList.add(orderMap);
            }

            // 결과 설정
            response.put("status", 200);
            response.put("message", "해당 날짜의 주문 내역 조회 성공");
            response.put("orders", orderList);
            response.put("totalPrice", totalPrice);

        } catch (Exception e) {
            // 예외 처리
            System.err.println(e.getMessage());
            response.put("status", -1);
            response.put("message", "토큰 검증 중 오류가 발생했습니다.");
        }

        return response;
    }

    // 127.0.0.1:8080/ROOT/api/orderview/list
    @GetMapping("/list")
    public List<Map<String, Object>> getOrdersByCustomerEmail(@RequestHeader(name = "Authorization") String token) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");
            // customerEmail이 null인 경우
            if (customerEmail == null) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", 401); // 인증되지 않은 요청
                errorMap.put("message", "로그인된 사용자 정보가 없습니다.");
                resultList.add(errorMap); // 에러 정보 추가
                return resultList;
            }
            // 이메일로 주문 내역 조회
            List<OrderView> orders = orderViewRepository.findByCustomeremail(customerEmail);

            // 주문 내역이 없을 경우
            if (orders.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("status", 404);
                map.put("message", "주문 내역이 없습니다.");
                resultList.add(map); // 결과에 추가
                return resultList;
            }

            orders.sort((o1, o2) -> o2.getOrdertime().compareTo(o1.getOrdertime()));
            
            // 주문 내역이 있을 경우
            for (OrderView order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("ordernumber", order.getOrdernumber());
                orderMap.put("paymentstatus", order.getPaymentstatus());
                orderMap.put("totalprice", order.getTotalprice());
                orderMap.put("orderstatus", order.getOrderstatus());
                orderMap.put("ordertime", order.getOrdertime());
                orderMap.put("storename", order.getStorename());
                orderMap.put("menuname", order.getMenuname());
                orderMap.put("dailymenuprice", order.getDailymenuprice());
                orderMap.put("quantity", order.getQuantity());
                orderMap.put("unitprice", order.getUnitprice());
                orderMap.put("pickupstatus", order.getPickupstatus());
                orderMap.put("startpickup", order.getStartpickup());
                orderMap.put("endpickup", order.getEndpickup());
                orderMap.put("storeid", order.getStoreid());

                resultList.add(orderMap); // 각 주문을 결과 리스트에 추가
            }

        } catch (Exception e) {
            // 예외 처리
            System.err.println(e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", -1);
            errorMap.put("message", "토큰 검증 중 오류가 발생했습니다.");
            resultList.add(errorMap); // 에러 정보 추가
        }

        return resultList;
    }

    // 127.0.0.1:8080/ROOT/api/orderview/date
    @GetMapping("/date")
    public List<Map<String, Object>> getOrdersByDate(
            @RequestParam String startDate, // 시작 날짜 (yyyy-MM-dd)
            @RequestParam String endDate, // 종료 날짜 (yyyy-MM-dd)
            @RequestHeader(name = "Authorization") String token) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            // 토큰 검증 후, 고객 이메일 추출
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            // customerEmail이 null인 경우
            if (customerEmail == null) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", 401); // 인증되지 않은 요청
                errorMap.put("message", "로그인된 사용자 정보가 없습니다.");
                resultList.add(errorMap); // 에러 정보 추가
                return resultList;
            }

            // 날짜 형식 변환
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay(); // 시작일을 LocalDateTime으로 변환
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59); // 종료일을 끝까지 포함하도록 설정

            // 고객 이메일과 날짜 범위에 맞는 주문 내역 조회
            List<OrderView> orders = orderViewRepository.findByCustomeremailAndOrdertimeBetween(customerEmail, start,
                    end);

            // 주문 내역이 없을 경우
            if (orders.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("status", 404);
                map.put("message", "해당 날짜 범위에 해당하는 주문 내역이 없습니다.");
                resultList.add(map); // 결과에 추가
                return resultList;
            }

            // 주문 내역이 있을 경우
            for (OrderView order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("ordernumber", order.getOrdernumber());
                orderMap.put("paymentstatus", order.getPaymentstatus());
                orderMap.put("totalprice", order.getTotalprice());
                orderMap.put("orderstatus", order.getOrderstatus());
                orderMap.put("ordertime", order.getOrdertime());
                orderMap.put("storename", order.getStorename());
                orderMap.put("menuname", order.getMenuname());
                orderMap.put("dailymenuprice", order.getDailymenuprice());
                orderMap.put("quantity", order.getQuantity());
                orderMap.put("unitprice", order.getUnitprice());
                orderMap.put("pickupstatus", order.getPickupstatus());
                orderMap.put("pickuptime", order.getPickuptime());

                resultList.add(orderMap); // 각 주문을 결과 리스트에 추가
            }

        } catch (Exception e) {
            // 예외 처리
            System.err.println(e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", -1);
            errorMap.put("message", "토큰 검증 중 오류가 발생했습니다.");
            resultList.add(errorMap); // 에러 정보 추가
        }

        return resultList;
    }

    // 127.0.0.1:8080/ROOT/api/orderview/status
    // 특정 고객의 주문 상태에 따른 주문 내역 조회
    @GetMapping("/status")
    public List<Map<String, Object>> getOrdersByStatus(
            @RequestParam String orderStatus,
            @RequestHeader(name = "Authorization") String token) {

        List<Map<String, Object>> resultList = new ArrayList<>();

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            // 토큰 검증 후, 고객 이메일 추출
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            // customerEmail이 null인 경우
            if (customerEmail == null) {
                Map<String, Object> errorMap = new HashMap<>();
                errorMap.put("status", 401); // 인증되지 않은 요청
                errorMap.put("message", "로그인된 사용자 정보가 없습니다.");
                resultList.add(errorMap); // 에러 정보 추가
                return resultList;
            }

            // 고객 이메일과 주문 상태에 맞는 주문 내역 조회
            List<OrderView> orders = orderViewRepository.findByCustomeremailAndOrderstatus(customerEmail, orderStatus);

            // 주문 내역이 없을 경우
            if (orders.isEmpty()) {
                Map<String, Object> map = new HashMap<>();
                map.put("status", 404);
                map.put("message", "해당 주문 상태의 주문 내역이 없습니다.");
                resultList.add(map); // 결과에 추가
                return resultList;
            }

            // 주문 내역이 있을 경우
            for (OrderView order : orders) {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("ordernumber", order.getOrdernumber());
                orderMap.put("paymentstatus", order.getPaymentstatus());
                orderMap.put("totalprice", order.getTotalprice());
                orderMap.put("orderstatus", order.getOrderstatus());
                orderMap.put("ordertime", order.getOrdertime());
                orderMap.put("storename", order.getStorename());
                orderMap.put("menuname", order.getMenuname());
                orderMap.put("dailymenuprice", order.getDailymenuprice());
                orderMap.put("quantity", order.getQuantity());
                orderMap.put("unitprice", order.getUnitprice());
                orderMap.put("pickupstatus", order.getPickupstatus());
                orderMap.put("pickuptime", order.getPickuptime());

                resultList.add(orderMap); // 각 주문을 결과 리스트에 추가
            }

        } catch (Exception e) {
            // 예외 처리
            System.err.println(e.getMessage());
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("status", -1);
            errorMap.put("message", "토큰 검증 중 오류가 발생했습니다.");
            resultList.add(errorMap); // 에러 정보 추가
        }

        return resultList;
    }

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
                Store store = storeRepository.findByStoreId(storeId); // StoreRepository로 storeId에 해당하는 Store 엔티티를 조회
                if (store != null) {
                    storeName = store.getStoreName(); // store 엔티티에서 상점 이름을 가져옴
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
            Map<String, Map<String, Map<String, List<OrderView>>>> ordersByDateStoreAndOrderNumber = orderDetails
                    .stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getOrdertime().toLocalDate().toString(), // 날짜로 그룹화
                            Collectors.groupingBy(
                                    OrderView::getStorename, // 가게 이름으로 그룹화
                                    Collectors.groupingBy(OrderView::getOrdernumber) // 주문 번호로 그룹화
                            )));

            // 결과 데이터
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> groupedOrders = new ArrayList<>();

            // 날짜별로 그룹화된 주문을 처리
            for (Map.Entry<String, Map<String, Map<String, List<OrderView>>>> dateEntry : ordersByDateStoreAndOrderNumber
                    .entrySet()) {
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

    // 127.0.0.1:8080/ROOT/api/orderview/payment
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
            List<OrderView> orderDetails = orderViewRepository
                    .findByCustomeremailAndOrdernumber(customerEmail, orderNumber)
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
