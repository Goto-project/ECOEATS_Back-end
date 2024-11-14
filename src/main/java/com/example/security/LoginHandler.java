package com.example.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//로그인 성공후 권한에 따라 다른 페이지 전환
// .succesHandler (내가만든 handler를 사용해야함 == AuthenticationSuccessHandler)
//extends(클래스로 제공해주는 것) , implements(인터페이스로 제공해주는 것) 사용해야함

public class LoginHandler implements AuthenticationSuccessHandler{

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

            //권한은 n개를 collection 타입으로 보관했기 떄문.
            //strRole => ROLE_CUSTOMER , ROLE_ADMIN , ROLE_SELLER
            String strRole = authentication.getAuthorities().toArray()[0].toString();

            if(strRole.equals("ROLE_CUSTOMER")){
                //권한이 고객이면 아래주소
                //127.0.0.1:8080/ROOT/customer/home.do
                response.sendRedirect(request.getContextPath()+"/customer/home.do");
            }
            else if(strRole.equals("ROLE_SELLER")){
                response.sendRedirect(request.getContextPath()+"/seller/home.do");
            }
            else if(strRole.equals("ROLE_ADMIN")){
                response.sendRedirect(request.getContextPath()+"/admin/home.do");
            }
            else{
                //127.0.0.1:8080/ROOT/home.do
                response.sendRedirect(request.getContextPath()+"/home.do");
            }
    }
    
}
