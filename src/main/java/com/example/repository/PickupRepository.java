package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.entity.Pickup;

public interface PickupRepository extends JpaRepository<Pickup,Integer>{
    
}
