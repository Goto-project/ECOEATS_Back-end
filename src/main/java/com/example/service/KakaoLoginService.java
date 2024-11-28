package com.example.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.entity.CustomerMember;
import com.example.entity.CustomerToken;
import com.example.repository.CustomerMemberRepository;
import com.example.repository.CustomerTokenRepository;

@Service
public class KakaoLoginService {
    @Autowired
    private CustomerMemberRepository customerMemberRepo;

    @Autowired
    private CustomerTokenRepository customerTokenRepo;

    /**
     * 고객 정보를 저장하거나 업데이트
     *
     * @param email    고객 이메일
     * @param nickname 고객 닉네임
     */
    public CustomerMember saveOrUpdateCustomer(String email, String nickname) {
        // 이메일로 고객 조회
        CustomerMember customer = customerMemberRepo.findById(email).orElse(null);
        if (customer == null) {
            customer = new CustomerMember();
            customer.setCustomerEmail(email);
            customer.setNickname(nickname);
            customerMemberRepo.save(customer);
        }
        return customer;
    }

    /**
     * 고객 토큰 정보를 저장하거나 업데이트
     *
     * @param customer  CustomerMember 엔티티
     * @param token     액세스 토큰
     * @param expireTime 토큰 만료 시간
     */
    public void saveOrUpdateToken(CustomerMember customer, String token, Date expireTime) {
        // 고객의 기존 토큰 조회
        CustomerToken customerToken = customerTokenRepo.findById(customer).orElse(null);
        if (customerToken == null) {
            customerToken = new CustomerToken();
            customerToken.setId(customer); // 관계 설정
        }

        // 토큰 정보 설정
        customerToken.setToken(token);
        customerToken.setExpiretime(expireTime);
        customerTokenRepo.save(customerToken);
    }
}

