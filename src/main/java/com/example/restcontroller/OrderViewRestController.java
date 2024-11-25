package com.example.restcontroller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.OrderView;
import com.example.repository.OrderViewRepository;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/orderview")
public class OrderViewRestController {

    final OrderViewRepository orderViewRepository;
    final TokenCreate tokenCreate;
    
    // 날짜별로 주문 했던 내역 출력(마이페이지 기능)
    // 127.0.0.1:8080/ROOT/api/orderview/orderbydate
    @GetMapping("/orderbydate")
    public Map<String, Object> orderbydateGET(@RequestHeader(name = "Authorization") String token) {
        Map<String,Object> map = new HashMap<>();
        String rawToken = token.replace("Bearer", "").trim();

        try{
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            Map<String, Object> tokenData1 = tokenCreate.validateSellerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");
            String storeId = (String) tokenData1.get("storeId");

            System.out.println(storeId);
            if (customerEmail == null && storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            //모든주문내역 가져오기
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

            // 주문 내역을 날짜별로 그룹화
            Map<String, List<OrderView>> orderbyDate = orderDetails.stream()
                .collect(Collectors.groupingBy(order -> 
                order.getOrdertime()
                .toLocalDate()
                .toString()
                ));
                    
            //결과 데이터 
            Map<String, Object> result = new HashMap<>();
            List<Map<String,Object>> groupOrder = new ArrayList<>();

            for(Map.Entry<String, List<OrderView>> entry : orderbyDate.entrySet()){
                Map<String, Object> dateMap = new HashMap<>();
                dateMap.put("order_date", entry.getKey()); // 날짜

                List<Map<String, Object>> orders = new ArrayList<>();
                int dailyTotal = 0; // 하루치 총 매출 계산

                for (OrderView order : entry.getValue()) {
                    Map<String, Object> orderSummary = new HashMap<>();
                    orderSummary.put("order_number", order.getOrdernumber());
                    orderSummary.put("menu_name", order.getMenuname());
                    orderSummary.put("quantity", order.getQuantity());
                    
                    if (customerEmail != null) {
                        // 고객일 경우 store_name 표시
                        orderSummary.put("store_name", order.getStorename());
                    } else if (storeId != null) {
                        // 판매자일 경우 customerEmail 표시
                        orderSummary.put("customer_email", order.getCustomeremail());
                    }
                    orderSummary.put("menu_price", order.getDailymenuprice());
                    orderSummary.put("unit_price", order.getUnitprice());

                    // 하루치 매출 합산
                    dailyTotal += order.getUnitprice();
                    orders.add(orderSummary);
                }

                dateMap.put("orders", orders); // 주문 내역
                dateMap.put("total_price", dailyTotal);
                groupOrder.add(dateMap);
            } 
            result.put("status", 200);
            result.put("data", groupOrder);
            map.put("status", 200);
            map.put("data", result);
        }catch(Exception e){
            System.out.println(e.getMessage());
            map.put("status", -1);
        }
        
        return map;
    }

    //가게이름마다 주문했던 내역 출력(마이페이지 기능)
    //127.0.0.1:8080/ROOT/api/orderview/ordersbystore
    @GetMapping(value = "/ordersbystore")
    public Map<String, Object> getOrdersByStore(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        String rawToken = token.replace("Bearer ", "").trim();

        try{
             // Token 유효성 검증
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            if (customerEmail == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            // 주문 내역 조회 (가게 이름별)
            List<OrderView> orderDetails = orderViewRepository.findByCustomeremail(customerEmail); 

            if (orderDetails.isEmpty()) {
                map.put("status", 404);
                map.put("message", "주문 내역이 없습니다.");
                return map;
            }

            // 주문 내역을 가게 이름별로 그룹화
            Map<String, List<OrderView>> ordersByStore = orderDetails.stream()
                    .collect(Collectors.groupingBy(OrderView::getStorename));
            Map<String, Object> result = new HashMap<>();
            List<Map<String, Object>> groupedOrders = new ArrayList<>();
            for (Map.Entry<String, List<OrderView>> entry : ordersByStore.entrySet()) {
                    Map<String, Object> storeMap = new HashMap<>();
                    storeMap.put("store_name", entry.getKey()); // 가게 이름
                    List<Map<String, Object>> orders = new ArrayList<>();
                    

                    for (OrderView order : entry.getValue()) {
                            Map<String, Object> orderSummary = new HashMap<>();
                            orderSummary.put("order_number", order.getOrdernumber());
                            orderSummary.put("menu_name", order.getMenuname());
                            orderSummary.put("quantity", order.getQuantity());
                            orderSummary.put("menu_price", order.getDailymenuprice());
                            orderSummary.put("total_price", order.getUnitprice());
                            orderSummary.put("regdate", order.getOrdertime());
                            
                            orders.add(orderSummary);
                        }
        
                        storeMap.put("orders", orders); // 주문 내역
                        groupedOrders.add(storeMap);
                    }
            result.put("status", 200);
            result.put("data", groupedOrders);
            map.put("status", 200);
            map.put("data", result);
        }catch(Exception e){
            map.put("status", -1);
            System.err.println(e.getMessage());
        }
        return map;
    }
     //결제완료 화면 출력(영수증)
    //127.0.0.1:8080/ROOT/api/orderview/payment
    @GetMapping(value = "/payment")
    public Map<String, Object> paymentGET(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        String rawToken = token.replace("Bearer", "").trim();
        try{
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");
            if(customerEmail == null){
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
                }

            // 고객 이메일을 기반으로 주문 내역 조회
            List<OrderView> orderDetails = orderViewRepository.findByCustomeremail(customerEmail);

            if (orderDetails.isEmpty()) {
                map.put("status", 404);
                map.put("message", "주문 내역이 없습니다.");
                return map;
            }

            List<Map<String, Object>> receiptData = new ArrayList<>();
            for(OrderView order : orderDetails){
                Map<String,Object> receipt = new HashMap<>();
                receipt.put("order_number", order.getOrdernumber());
                receipt.put("order_date", order.getOrdertime());
                receipt.put("pay_method", order.getPaymentstatus());
                receipt.put("total_price", order.getTotalprice());
                receipt.put("customer_email", order.getCustomeremail());
                receipt.put("store_name", order.getStorename());
                receipt.put("menu_name", order.getMenuname());
                receipt.put("dailymenu_price", order.getDailymenuprice());
                receipt.put("quantity", order.getQuantity());
                receipt.put("unit_price", order.getUnitprice());
                receipt.put("start_pickup", order.getStartpickup());
                receipt.put("end_pickup", order.getEndpickup());
                 // 픽업 상태, 픽업 시간, 주문 상태 처리
                String pickup = (order.getPickupstatus() == 1) ? "픽업 완료" : "픽업 대기";
                String pickupDate = (order.getPickupstatus() == 1) ? order.getPickuptime().toString() : "픽업 대기 중";
                String status = (order.getOrderstatus() == "주문 완료") ? "주문 완료" : "주문 취소";

                receipt.put("pickup", pickup);
                receipt.put("pickup_date", pickupDate);
                receipt.put("status", status);

                receiptData.add(receipt);
            }

            map.put("status", 200);
            map.put("data", receiptData);
        }catch(Exception e){
            map.put("status", -1);
            System.out.println(e.getMessage());
        }
        return map;
    }
    
    
}
