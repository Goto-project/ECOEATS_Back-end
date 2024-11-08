package com.example.token;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//filter, 인터셉터 aop
// filter => controller, restcontroller
// OncePerRequestFilter => doFilterInternal
@Component
public class JwtFilter extends OncePerRequestFilter {

    //map을 json으로 변경하기 위한 객체
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    TokenCreate tokenCreate = new TokenCreate();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
    throws ServletException, IOException {

        //jsp에서 json으로 변경하기
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Map<String, Object> map = new HashMap<>();

        try {
            System.out.println("========filter========");
            System.out.println(request.getRequestURI());
            System.out.println("========filter========");

            //Authorization: Bearer 실제토큰
            //const headers = {"Quthorization":"Bearer 토큰정보"}
            String token = request.getHeader("Authorization");

            if (token == null) {//headers를 안 넣었다면 null
                map.put("status", 0);
                map.put("result", "토큰 키값이 없습니다.");
                String json = objectMapper.writeValueAsString(map);
                response.getWriter().write(json);
                return;
            }

            if (token.length() <= 0) {
                map.put("status", 0);
                map.put("result", "토큰 값이 없습니다.");
                String json = objectMapper.writeValueAsString(map);
                response.getWriter().write(json);
                return;
            }

            if(!token.startsWith("Bearer ")){
                map.put("status", 0);
                map.put("result", "토큰 구조가 다릅니다.");
                String json = objectMapper.writeValueAsString(map);
                response.getWriter().write(json);
                return;
            }

            //실제 토큰
            String token1 = token.substring(7);

            //토큰 검증, 유효하지 않거나 기타 등등하면 catch로 넘어가서 처리됨
            String userid = tokenCreate.validate(token1);
            request.setAttribute("userid", userid);

            //아래 명령어가 실행되면 controller, restcontroller로 넘어감
            filterChain.doFilter(request, response);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
            map.put("staus", -1);
            map.put("result", "토큰이 유효하지 않습니다.");

            //map => json으로 변경하기
            String json = objectMapper.writeValueAsString(map);
            response.getWriter().write(json);
        }
    }
}
