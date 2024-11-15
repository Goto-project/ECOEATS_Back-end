package com.example.token;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//환경설정 + @Bean은 한 세트라고 봐야 함

//환경설정 => 서버를 구동 시 확인함. 오류가 있으면 서버는 중지됨
@Configuration
public class FilterConfig {

    // 회원정보변경, 탈퇴, 암호 변경....
    @Bean // 자동으로 객체를 생성하기 위해서
    public FilterRegistrationBean<JwtFilter> filterRegistrationBean(JwtFilter jwtFilter){
        FilterRegistrationBean<JwtFilter> filterReg = new FilterRegistrationBean<>();

        filterReg.setFilter(jwtFilter);

        //여기가 필터를 통과시킬 URL 설정 부분
        // 전체 한 번에 필터 설정하려면 /api/member1/* (지금은 로그인, 회원가입은 빼야해서 *쓰면 안됨)
        filterReg.addUrlPatterns("/api/member1/update.do", 
                                    "/api/member1/delete.do" ,
                                    "/api/customer/update.do",
                                    "/api/customer/delete.do",
                                    "/api/customer/logout.do"
                                    );

        return filterReg;
    }

    //  이런 식으로 n개 만들 수 있음
    // 판매자 정보 변경, 탈퇴, 암호 변경....
    @Bean // 자동으로 객체를 생성하기 위해서
    public FilterRegistrationBean<JwtFilter1> filterRegistrationBean1(JwtFilter1 jwtFilter1){
        FilterRegistrationBean<JwtFilter1> filterReg = new FilterRegistrationBean<>();

        filterReg.setFilter(jwtFilter1);

        //여기가 필터를 통과시킬 URL 설정 부분
        // 전체 한 번에 필터 설정하려면 /api/member1/* (지금은 로그인, 회원가입은 빼야해서 *쓰면 안됨)
        filterReg.addUrlPatterns("/api/seller/update.do", "/api/seller/delete.do");

        return filterReg;
    }
}
