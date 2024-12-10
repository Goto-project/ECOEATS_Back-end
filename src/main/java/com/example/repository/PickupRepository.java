package com.example.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.entity.Pickup;

@Repository
public interface PickupRepository extends JpaRepository<Pickup,Integer>{
    
    Optional<Pickup> findByOrderno_Orderno(String orderno);
}
