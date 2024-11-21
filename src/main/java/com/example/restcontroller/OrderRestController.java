package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.Cart;
import com.example.entity.Order;
import com.example.repository.CartRepository;
import com.example.repository.OrderRepository;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order")
public class OrderRestController {

    final OrderRepository orderRepository;
    final CartRepository cartRepository;

    final TokenCreate tokenCreate;

    // http://localhost:8080/ROOT/api/order/create
    
}
