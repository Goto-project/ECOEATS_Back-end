package com.example.service;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dto.KakaoSearchDTO;
import com.example.entity.CustomerAddress;
import com.example.entity.CustomerMember;
import com.example.repository.CustomerAddressRepository;
import com.example.repository.CustomerMemberRepository;

@Service
public class CustomerAddressService {

    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerMemberRepository customerMemberRepository;  // CustomerMemberRepository 추가

    @Autowired
    public CustomerAddressService(CustomerAddressRepository customerAddressRepository, 
                                    CustomerMemberRepository customerMemberRepository) {
        this.customerAddressRepository = customerAddressRepository;
        this.customerMemberRepository = customerMemberRepository;
    }

    public void saveCustomerAddress(String searchKeyword, String customerEmail) {
        // 카카오 API를 호출하여 주소 데이터를 가져옴
        KakaoSearch kakaoSearch = new KakaoSearch();
        KakaoSearchDTO kakaoSearchDTO = kakaoSearch.getKakaoSearch(searchKeyword);

        // 검색된 첫 번째 주소 정보 가져오기
        KakaoSearchDTO.Document document = kakaoSearchDTO.getDocuments().get(0);

        // CustomerMember 객체 조회 (이메일로 찾기)
        CustomerMember customerMember = customerMemberRepository.findByCustomerEmail(customerEmail);
        if (customerMember == null) {
            throw new IllegalArgumentException("고객 이메일이 존재하지 않습니다.");
        }

        // CustomerAddress 객체를 생성하여 카카오 API에서 받은 데이터 매핑
        CustomerAddress customerAddress = new CustomerAddress();
        customerAddress.setPostcode(document.getAddress_name());  // 주소명
        customerAddress.setAddress(document.getRoad_address_name());  // 도로명 주소
        customerAddress.setAddressdetail("");  // 상세 주소 (추후 추가 가능)
        customerAddress.setLatitude(new BigDecimal(document.getY()));  // 위도
        customerAddress.setLongitude(new BigDecimal(document.getX()));  // 경도
        customerAddress.setCustomeremail(customerMember);  // 고객 이메일로 연결

        // 저장
        customerAddressRepository.save(customerAddress);
    }
}