package com.example.service;

import java.util.HashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.entity.Order;
import com.example.entity.Status;
//import com.example.ourhomepage.config.KakaoPayConfig;
import com.example.repository.OrderRepository;
import com.example.repository.StatusRepository;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class KakaoPayService {
    private final RestTemplate restTemplate;
    private final OrderRepository orderRepository; // OrderRepository 주입 추가
    private final StatusRepository statusRepository;

    private final String KAKAO_PAY_READY_URL = "https://open-api.kakaopay.com/online/v1/payment/ready";
    private final String cid = "TC0ONETIME"; // 테스트용 CID

    @Value("${kakao.api.secret.key}")
    private String secretKey = "DEV977F5D1735A00B9674FD543085B2EBA9929C1";

    public KakaoPayService(RestTemplate restTemplate, OrderRepository orderRepository, StatusRepository statusRepository) {
        this.restTemplate = restTemplate;
        this.orderRepository = orderRepository;
        this.statusRepository = statusRepository;
    }

    public Map<String, String> kakaoPayReady(Order order) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("partner_order_id", order.getOrderno().toString());
        params.put("partner_user_id", order.getCustomeremail().getCustomerEmail().toString());
        params.put("item_name", order.getOrderno().toString());
        params.put("quantity", 1);
        params.put("total_amount", order.getTotalprice());
        params.put("tax_free_amount", 0); // 비과세 금액
        params.put("approval_url",
                "http://localhost:3000/payment/success?orderNo=" + order.getOrderno());
        params.put("cancel_url", "http://localhost:3000/payment/cancel");
        params.put("fail_url", "http://localhost:3000/payment/fail");

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    KAKAO_PAY_READY_URL, HttpMethod.POST, body, Map.class);
            // System.out.println("Response Status Code: " + response.getStatusCode());
            // System.out.println("Response Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                // JSON 응답을 Map 형태로 받음
                Map<String, String> responseBody = new HashMap<>();
                String tid = response.getBody().get("tid").toString();
                responseBody.put("tid", tid);
                responseBody.put("next_redirect_pc_url", response.getBody().get("next_redirect_pc_url").toString());
                responseBody.put("next_redirect_mobile_url",

                        response.getBody().get("next_redirect_mobile_url").toString());

                System.out.println(response.getBody().toString());

                // tid를 payment 객체에 저장 후 DB에 업데이트
                order.setTid(tid);
                orderRepository.save(order); // tid를 저장한 payment 객체를 DB에 저장
                return responseBody;
            } else {
                throw new RuntimeException("Failed to initiate KakaoPay: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initiate KakaoPay: " + e.getMessage());
        }
    }

    // 결제 승인
    public Map<String, String> kakaoPayApprove(String pgToken, String orderNo) {
        System.out.println("kakaoPayApprove method called");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        Order order = orderRepository.findByOrderno(orderNo);
        String tid = order.getTid();

        Map<String, Object> params = new HashMap<>();
        params.put("cid", "TC0ONETIME");
        params.put("tid", tid);
        params.put("partner_order_id", orderNo); // 실제 partner_order_id를 입력
        params.put("partner_user_id", order.getCustomeremail().getCustomerEmail().toString()); // 실제 partner_user_id를 입력
        params.put("pg_token", pgToken);

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://open-api.kakaopay.com/online/v1/payment/approve", HttpMethod.POST, body, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, String> responseBody = response.getBody();

                // 승인된 주문의 정보를 업데이트
                orderRepository.save(order); // DB에 저장

                // 결제 완료 후 성공 메시지와 리다이렉트 URL 반환
                responseBody.put("status", "200");
                responseBody.put("message", "결제 승인 성공");
                return responseBody;
            } else {
                throw new RuntimeException("Failed to approve payment: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to approve payment: " + e.getMessage());
        }
    }

    public Map<String, String> kakaoPayCancel(String orderNo) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        // 주문 정보를 DB에서 조회
        Order order = orderRepository.findByOrderno(orderNo);
        if (order == null) {
            throw new RuntimeException("주문을 찾을 수 없습니다: " + orderNo);
        }

        String tid = order.getTid();
        if (tid == null || tid.isEmpty()) {
            throw new RuntimeException("카카오페이 결제 TID가 존재하지 않습니다: " + orderNo);
        }

        // 카카오페이 결제 취소 요청 파라미터 설정
        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tid", tid);
        params.put("cancel_amount", order.getTotalprice());
        params.put("cancel_tax_free_amount", 0); // 비과세 금액

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://open-api.kakaopay.com/v1/payment/cancel", HttpMethod.POST, body, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, String> responseBody = new HashMap<>();
                responseBody.put("status", "200");
                responseBody.put("message", "결제가 성공적으로 취소되었습니다.");
                responseBody.put("cancel_amount", response.getBody().get("cancel_amount").toString());
                responseBody.put("tid", tid);

                // DB에서 주문 상태를 '주문 취소'로 업데이트
                Status cancelStatus = new Status();
                cancelStatus.setOrderno(order);
                cancelStatus.setStatus("주문 취소");
                statusRepository.save(cancelStatus);
                
                return responseBody;
            } else {
                throw new RuntimeException("카카오페이 결제 취소 실패: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("카카오페이 결제 취소 중 오류 발생: " + e.getMessage());
        }
    }

}
