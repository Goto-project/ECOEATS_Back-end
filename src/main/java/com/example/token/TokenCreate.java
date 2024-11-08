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
