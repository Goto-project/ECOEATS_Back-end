package com.example.token;

import static io.jsonwebtoken.Jwts.builder;
import static io.jsonwebtoken.Jwts.parser;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;

@Component
public class TokenCreate {
    // private static final String secretKey = ;
    // private final Key key = Keys.hmacShaKeyFor(secretKey.getBytes());

    byte[] secretKeyBytes = Base64.getEncoder().encode(
            "keycefjkjfekljfelkjfeklfjeklfejklefjklefjfekljfekljfelkfejlkefjlefkjfelkjfelkfjelefjeflode입력".getBytes());
    SecretKey key = Keys.hmacShaKeyFor(secretKeyBytes);

    //토큰 생성하기
    public Map<String, Object> create(Map<String, Object> claims) {
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(30);
        Date _expireAt = Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant());
        Map<String, Object> map = new HashMap<>();

        // token 의 Expire 시간을 변환해 줍니다
        String token = builder()
                .signWith(key, SIG.HS256)
                .claims(claims)
                .expiration(_expireAt)
                .compact();
        map.put("token", token);
        map.put("expiretime", _expireAt);
        return map;
    }

    // 생성한 토큰 검증하기
    public String validate(String token) {
        try {
            JwtParser parser = parser().verifyWith(key).build();
            Jws<Claims> result = parser.parseSignedClaims(token);
            return (String) result.getPayload().get("user_id");

            // result.getPayload().forEach((key1, value1) -> log.info("key : {}, value :{}",
            // key1, value1));
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                throw new RuntimeException("JWT Token Invalid Exception");
            } else if (e instanceof ExpiredJwtException) {
                throw new RuntimeException("JWT Token Expired Exception");
            } else {
                throw new RuntimeException("JWT Exception");
            }
        }
    }

    // 판매자용 토큰 발행 <= 토큰에 심을 정보(기본키, 식당 이름,)
    public Map<String, Object> generateSellerToken(Map<String, Object> claims){

        //토큰에 만료시간 설정 (ex. 60*4 = 4시간)
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(60*4);
        Date _expireAt = Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant());
        
        // 토큰 생성 (키와 HS256알고리즘, claims = 토큰에 포함할 내용(Map 타입), _expireAt = 만료 시간)
        // 컨트롤러에서 claims에 담아줘야 함
        //claims.put("phone", "010")
        // claims.put("name", "가나다")
        String token = builder()
        .signWith(key, SIG.HS256)
        .claims(claims)
        .expiration(_expireAt)
        .compact();
        
        // 생성한 토큰과 만료시간을 전달하기
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expiretime", _expireAt);

        return map;
    }
    // 생성한 판매자용 토큰 검증하기 => 토큰이 전달되면 검증 후 토큰에 포함했던 내용 반환하기
    public Map<String, Object> validateSellerToken(String token) {
        try {
            //검증을 위한 키값 설정 == 발행 시의 키값 (반드시 일치해야 함)
            JwtParser parser = parser().verifyWith(key).build();
            Jws<Claims> result = parser.parseSignedClaims(token); //이게 꺼낸 것

            // 토큰에서 추출하기 (key값은 토큰을 생성할 때 적용했던 값과 일치해야 함)
            String storeId = (String) result.getPayload().get("storeId");
            String name = (String) result.getPayload().get("name");

            //추출한 정보 반환
            Map<String, Object> map = new HashMap<>();
            map.put("storeId", storeId);
            map.put("name", name);
            return map;
            // result.getPayload().forEach((key1, value1) -> log.info("key : {}, value :{}",
            // key1, value1));
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                throw new RuntimeException("JWT Token Invalid Exception");
            } else if (e instanceof ExpiredJwtException) {
                throw new RuntimeException("JWT Token Expired Exception");
            } else {
                throw new RuntimeException("JWT Exception");
            }
        }
    }



// 고객용 토큰 발행 <= 토큰에 심을 정보(기본키, 식당 이름,)
    public Map<String, Object> generateCustomerToken(Map<String, Object> claims){

        //토큰에 만료시간 설정 (ex. 60*4 = 4시간)
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(60*4);
        Date _expireAt = Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant());
        
        // 토큰 생성 (키와 HS256알고리즘, claims = 토큰에 포함할 내용(Map 타입), _expireAt = 만료 시간)
        // 컨트롤러에서 claims에 담아줘야 함
        //claims.put("phone", "010")
        // claims.put("name", "가나다")
        String token = builder()
        .signWith(key, SIG.HS256)
        .claims(claims)
        .expiration(_expireAt)
        .compact();
        
        // 생성한 토큰과 만료시간을 전달하기
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expiretime", _expireAt);

        return map;
    }

    // 생성한 고객용 토큰 검증하기 => 토큰이 전달되면 검증 후 토큰에 포함했던 내용 반환하기
    public Map<String, Object> validateCustomerToken(String token) {
        try {
            //검증을 위한 키값 설정 == 발행 시의 키값 (반드시 일치해야 함)
            JwtParser parser = parser().verifyWith(key).build();
            Jws<Claims> result = parser.parseSignedClaims(token); //이게 꺼낸 것
            
            // 토큰에서 추출하기 (key값은 토큰을 생성할 때 적용했던 값과 일치해야 함)
            String customerEmail = (String) result.getPayload().get("customerEmail");
            
            System.out.println("Parsed Claims: " + result.getPayload()); 
            //추출한 정보 반환
            Map<String, Object> map = new HashMap<>();
            map.put("customerEmail", customerEmail);
            
            return map;
            // result.getPayload().forEach((key1, value1) -> log.info("key : {}, value :{}",
            // key1, value1));
        } catch (Exception e) {
            if (e instanceof SignatureException) {
                throw new RuntimeException("JWT Token Invalid Exception");
            } else if (e instanceof ExpiredJwtException) {
                throw new RuntimeException("JWT Token Expired Exception");
            } else {
                throw new RuntimeException("JWT Exception"+ e.getMessage());
            }
        }
    }
}
