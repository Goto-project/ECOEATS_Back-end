package com.example.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.CustomerMember;
import com.example.entity.Review;
import com.example.entity.Store;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByReviewNo(int reviewNo);

    // storeId를 기준으로 페이징된 리뷰를 조회하는 메소드
    Page<Review> findReviewsByStoreId(Store store, Pageable pageable);

    // 특정 가게에 대한 리뷰 개수 조회
    long countByStoreIdStoreId(String storeId);

    List<Review> findByStoreId(Store storeId);

    List<Review> findByCustomerEmail(CustomerMember customerEmail);
    

    
    
}
