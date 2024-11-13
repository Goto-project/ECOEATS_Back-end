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

    //ex) 판매자용 토큰 검증 => 토큰이 전달되면 검증후에 토큰에 포함했던 내용을 반환하기
    public Map<String, Object> validateSellerToken( String token) {
        try {
            // 검증을 위한 키값 설정 == 발행 시의 키값과 반드시 일치
            JwtParser parser = parser().verifyWith(key).build();
            Jws<Claims> result = parser.parseSignedClaims(token);

            //토큰에서 추출하기(key 값은 토큰을 생성할때 적용햇던 값과 일치)
            String phone = (String) result.getPayload().get("phone");
            String name = (String) result.getPayload().get("name");


            //추출한 정보 반환
            Map<String, Object> map = new HashMap<>();
            map.put("phone", phone);
            map.put("name",name );
            return map;    

            
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




    //ex 판매자,식당용 토큰 발행 <= 전화번호(PK), 이름
    public Map<String, Object> generateSellerToken( Map<String, Object> claims){

        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(60*4); //토큰 만료시간 4시간 설정
        Date _expireAt = Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant());


        //토큰 생성()
        // claims.put("phone", 010)
        // claims.put("name", "홍길동")
        String token =  builder()
                    .signWith(key, SIG.HS256)
                    .claims(claims)
                    .expiration(_expireAt)
                    .compact();

        //생성한 토큰 만료시간을 전달하기
        Map<String, Object> map = new HashMap<>();
        map.put("token", token);
        map.put("expiretime",_expireAt );
        return map;           
    }


    // 토큰 생성하기(A)
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
    // 생성한 토큰 검증
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
}
