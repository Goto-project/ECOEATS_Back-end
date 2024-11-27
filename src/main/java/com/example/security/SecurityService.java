package com.example.security;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.dto.Member1;
import com.example.mapper.MemberMapper;

//로그인 화면에서 로그인 버튼 클릭 시 아이디와 암호가 전달되는 서비스
@Service
public class SecurityService implements UserDetailsService {
    // customer, seller, admin 테이블이 나눠져 있다면, 각 테이블 합쳐진 view를 만들어서 조회
    @Autowired
    MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("로그인 시도: " + username); // 로그인 시도 시 로그 출력

        // 각 테이블에서 사용자를 조회
        Member1 member = memberMapper.selectMemberFromStore(username);
        if (member == null) {
            member = memberMapper.selectMemberFromCustomer(username);
        }
        if (member == null) {
            member = memberMapper.selectMemberFromAdmin(username);
        }
        if (member != null) {
            String[] strRole = { "ROLE_" + member.getRole() };
            Collection<GrantedAuthority> role = AuthorityUtils.createAuthorityList(strRole);

            // import org.springframework.security.core.userdetails.User;
            return new User(member.getId(), member.getPw(), role);
        } else {
            System.out.println("role is null"); // role이 null인 경우 로그 출력
        }

        throw new UsernameNotFoundException("User not found with username: " + username);
    }
}
