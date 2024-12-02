package com.example.service;

import java.util.HashMap;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.example.entity.Order;
//import com.example.ourhomepage.config.KakaoPayConfig;
import com.example.repository.OrderRepository;

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

    private final String KAKAO_PAY_READY_URL = "https://open-api.kakaopay.com/online/v1/payment/ready";
    private final String cid = "TC0ONETIME"; // 테스트용 CID

    @Value("${kakao.api.secret.key}")
    private String secretKey = "DEV977F5D1735A00B9674FD543085B2EBA9929C1";

    public KakaoPayService(RestTemplate restTemplate, OrderRepository orderRepository) {
        this.restTemplate = restTemplate;
        this.orderRepository = orderRepository;
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
                "http://localhost:3000/api/payments/kakaoPaySuccess?orderno=" + order.getOrderno());
        params.put("cancel_url", "http://localhost:3000/api/payments/kakaoPayCancel");
        params.put("fail_url", "http://localhost:3000/api/payments/kakaoPayFail");

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    KAKAO_PAY_READY_URL, HttpMethod.POST, body, Map.class);
            System.out.println("Response Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());

            if (response.getStatusCode().is2xxSuccessful()) {
                // JSON 응답을 Map 형태로 받음
                Map<String, String> responseBody = new HashMap<>();
                String tid = response.getBody().get("tid").toString();
                responseBody.put("tid", tid);
                responseBody.put("next_redirect_pc_url", response.getBody().get("next_redirect_pc_url").toString());
                responseBody.put("next_redirect_mobile_url",
                        response.getBody().get("next_redirect_mobile_url").toString());

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
    public Map<String, String> kakaoPayApprove(String tid, String pgToken, Order order) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        Map<String, Object> params = new HashMap<>();
        params.put("cid", "TC0ONETIME");
        params.put("tid", tid);
        params.put("partner_order_id", order.getOrderno().toString()); // 실제 partner_order_id를 입력
        params.put("partner_user_id", order.getCustomeremail().toString()); // 실제 partner_user_id를 입력
        params.put("pg_token", pgToken);

        HttpEntity<Map<String, Object>> body = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://open-api.kakaopay.com/online/v1/payment/approve", HttpMethod.POST, body, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody(); // 승인 성공 시 응답 본문 반환
            } else {
                throw new RuntimeException("Failed to approve payment: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to approve payment: " + e.getMessage());
        }
    }

}
