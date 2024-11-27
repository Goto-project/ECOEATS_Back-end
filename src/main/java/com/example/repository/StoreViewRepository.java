package com.example.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.entity.StoreView;

@Repository
public interface StoreViewRepository extends JpaRepository<StoreView, String> {

    // 1km이내 가게 보기 (카테고리별, 거리순, 별점 높은 순, 리뷰 많은 순)
    @Query(value = """
            SELECT s.storeid, s.storeemail, s.storename, s.address, s.phone, s.category,
                   s.latitude, s.longitude, s.startpickup, s.endpickup,
                   COALESCE(s.avgrating, 0) AS avgrating,
                   COALESCE(s.bookmarkcount, 0) AS bookmarkcount,
                   COALESCE(s.reviewcount, 0) AS reviewcount,
                   s.storeimageno
            FROM storedetailview s
            WHERE
                -- latitude, longitude가 null이 아닌 경우만 처리
                s.latitude IS NOT NULL AND s.longitude IS NOT NULL
                AND ST_Distance_Sphere(POINT(:customerLongitude, :customerLatitude), POINT(s.longitude, s.latitude)) <= 1000
                AND (:category IS NULL OR s.category = :category)
            ORDER BY
                CASE
                    WHEN :sortBy = 'distance' THEN COALESCE(ST_Distance_Sphere(POINT(:customerLongitude, :customerLatitude), POINT(s.longitude, s.latitude)), 0)
                    WHEN :sortBy = 'rating' THEN -COALESCE(s.avgrating, 0)
                    WHEN :sortBy = 'review' THEN -COALESCE(s.reviewcount, 0)
                    ELSE COALESCE(ST_Distance_Sphere(POINT(:customerLongitude, :customerLatitude), POINT(s.longitude, s.latitude)), 0)
                END
            """, 
            countQuery = """
            SELECT COUNT(*)
            FROM storedetailview s
            WHERE
                s.latitude IS NOT NULL AND s.longitude IS NOT NULL
                AND ST_Distance_Sphere(POINT(:customerLongitude, :customerLatitude), POINT(s.longitude, s.latitude)) <= 1000
                AND (:category IS NULL OR s.category = :category)
            """,
            nativeQuery = true)
            Page<StoreView> findStoresWithinRadiusPaged(BigDecimal customerLatitude, BigDecimal customerLongitude, String category, String sortBy, Pageable pageable);

}
