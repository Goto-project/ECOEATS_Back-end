package com.example.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.CustomerMember;
import com.example.entity.Review;
import com.example.entity.Store;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer>{
    List<Review> findByReviewNo(int reviewNo);

    List<Review> findByStoreId(Store storeId);

    List<Review> findByCustomerEmail(CustomerMember customerEmail);
}
