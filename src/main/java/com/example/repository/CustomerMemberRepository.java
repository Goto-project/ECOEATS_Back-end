package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.CustomerMember;

@Repository
public interface CustomerMemberRepository extends JpaRepository<CustomerMember, String> {
    CustomerMember findByCustomerEmail(String customerEmail);  // 이메일로 회원 조회
    
    CustomerMember findByCustomerEmail(String customerEmail);
}
