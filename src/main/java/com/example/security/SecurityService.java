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
    //customer, seller, admin 테이블이 나눠져 있다면, 각 테이블 합쳐진 view를 만들어서 조회
    @Autowired
    MemberMapper memberMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Security Service => " + username);

        Member1 member = memberMapper.selectMember1One(username);
        if (member != null) {
            String[] strRole = {"ROLE_" + member.getRole()};
            Collection<GrantedAuthority> role = AuthorityUtils.createAuthorityList(strRole);

            // import org.springframework.security.core.userdetails.User;
            return new User(member.getId(), member.getPw(), role);
        }

        throw new UnsupportedOperationException("Unimplemented method 'loadUserByUsername'");
    }
}
