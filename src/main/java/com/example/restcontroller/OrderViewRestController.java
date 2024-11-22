package com.example.restcontroller;

import java.time.LocalTime;
import java.time.ZoneId;
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
    
    //날짜별로 주문 했던 내역 출력
    //127.0.0.1:8080/ROOT/api/customer/orderbydate
    @GetMapping("/orderbydate")
    public Map<String, Object> orderbydateGET(@RequestHeader(name = "Authorization") String token) {
        Map<String,Object> map = new HashMap<>();
        String rawToken = token.replace("Bearer", "").trim();

        try{
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            if(customerEmail == null){
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            //모든주문내역 가져오기
            List<OrderView> orderDetails = orderViewRepository.findAll();

            if (orderDetails.isEmpty()) {
                map.put("status", 404);
                map.put("message", "주문 내역이 없습니다.");
                return map;
            }
            // 주문 내역을 날짜별로 그룹화
            Map<String, List<OrderView>> orderbyDate = orderDetails.stream()
            .collect(Collectors.groupingBy(order -> 
                order.getOrderdate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString() // 날짜 부분만 추출 후 문자열로 변환
            ));

                    
                    System.out.println(orderbyDate);

            //결과 데이터 
            Map<String, Object> result = new HashMap<>();
            List<Map<String,Object>> groupOrder = new ArrayList<>();
            for(Map.Entry<String, List<OrderView>> entry : orderbyDate.entrySet()){
                Map<String, Object> dateMap = new HashMap<>();
                dateMap.put("order_date", entry.getKey()); // 날짜
                List<Map<String, Object>> orders = new ArrayList<>();

                for (OrderView order : entry.getValue()) {
                    Map<String, Object> orderSummary = new HashMap<>();
                    orderSummary.put("order_number", order.getOrderno());
                    orderSummary.put("menu_name", order.getMenuname());
                    orderSummary.put("quantity", order.getQuantity());
                    orderSummary.put("menu_price", order.getMenuprice());
                    orderSummary.put("total_price", order.getQuantity() * order.getMenuprice());
                    orders.add(orderSummary);
                }

                dateMap.put("orders", orders); // 주문 내역
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

    //가게이름마다 주문했던 내역 출력
    //127.0.0.1:8080/ROOT/api/customer/orderbystore
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
            List<OrderView> orderDetails = orderViewRepository.findAll(); 

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
                            orderSummary.put("order_number", order.getOrderno());
                            orderSummary.put("menu_name", order.getMenuname());
                            orderSummary.put("quantity", order.getQuantity());
                            orderSummary.put("menu_price", order.getMenuprice());
                            orderSummary.put("total_price", order.getQuantity() * order.getMenuprice());
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
    
    //결제완료 화면 출력
    //127.0.0.1:8080/ROOT/api/customer/payment
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

            List<OrderView> orderDetails = orderViewRepository.findAll();
            if(orderDetails.isEmpty()){
                map.put("status", 404);
                map.put("message", "주문 내역이 없습니다.");
                return map;
            }
            
            //주문내역 가격 총 계산
            List<Map<String, Object>> orderSummary = new ArrayList<>();
            int totalprice = 0;
            
            for(OrderView order: orderDetails){
                Map<String, Object> item = new HashMap<>();
                item.put("order_number", order.getOrderno());
                item.put("menu_name",order.getMenuname());
                item.put("quantity", order.getQuantity());
                item.put("menu_price", order.getMenuprice());
                item.put("total_price", order.getQuantity() * order.getMenuprice());
                totalprice += order.getQuantity() * order.getMenuprice();
                orderSummary.add(item);
            }

             // 픽업 시간 (예: startpickup과 endpickup 제공)
            LocalTime startPickup = orderDetails.get(0).getStartpickup();
            LocalTime endPickup = orderDetails.get(0).getEndpickup();

             // 결제 완료 화면에 필요한 데이터 구성
            Map<String, Object> result = new HashMap<>();
            result.put("nickname", orderDetails.get(0).getCustomernickname());
            result.put("order_summary", orderSummary);
            result.put("total_price", totalprice);
            result.put("pickup_time", "픽업시간: " + startPickup + " ~ " + endPickup);
            map.put("status", 200);
            map.put("data", result);
        }catch(Exception e){
            map.put("status", -1);
            System.out.println(e.getMessage());
        }
        return map;
    }
    
    
}
