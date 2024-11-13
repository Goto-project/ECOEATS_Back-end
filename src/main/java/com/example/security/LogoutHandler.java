package com.example.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class LogoutHandler implements LogoutSuccessHandler{

    @Override
    public void onLogoutSuccess(
            HttpServletRequest request, 
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {
                
                response.sendRedirect(request.getContextPath()+"/home.do");
    }
    
}
