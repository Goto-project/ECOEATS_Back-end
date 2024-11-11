package com.example.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // 환경설정, 서버가 구동될 때 실행시켜 오류가 있으면 서버가 중지됨
@EnableWebSecurity
public class SecurityConfig {

    @Bean // 자동 객체 생성
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.httpBasic(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests((authorize) -> authorize
            .requestMatchers("/admin", "/admin/").hasRole("ADMIN")
            .requestMatchers("/seller", "/seller/").hasAnyRole("ADMIN", "SELLER")
            .requestMatchers("/customer", "/customer/*").hasRole("CUSTOMER")
            .requestMatchers("/member1", "/member1/**").permitAll()
            .anyRequest().permitAll()
    );

        // 세션저장 설정 ALWAYS(세션 저장됨), NEVER(세션 저장 안됨), STATELESS(rest용 세션)
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS));

        // 접근권한 불가 페이지 표시
        http.exceptionHandling((exceptionHandling) -> exceptionHandling.accessDeniedPage("/page403.do"));

        // 로그인 페이지 설정
        http.formLogin((formLogin) -> formLogin
                .loginPage("/login.do")
                .loginProcessingUrl("/loginacion.do")
                .usernameParameter("userid")
                .passwordParameter("userpw")
                .defaultSuccessUrl("/home.do"));

        // 로그아웃 페이지 설정
        http.logout((logout) -> logout
                .logoutUrl("/logout.do")
                .logoutSuccessUrl("/home.do")
                .invalidateHttpSession(true));

        // rest용 security가 동작되지 않음
        http.csrf((csrf) -> csrf.ignoringRequestMatchers("/api/**", "/api1/**"));

        return http.build();
    }

    @Bean // 자동 객체 생성
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
