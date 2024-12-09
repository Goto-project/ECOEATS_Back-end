package com.example.restcontroller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CustomerMemberDTO;
import com.example.dto.CustomerToken;
import com.example.entity.Cart;
import com.example.entity.CustomerMember;
import com.example.mapper.CustomerMemberMapper;
import com.example.mapper.TokenMapper;
import com.example.repository.CartRepository;
import com.example.repository.CustomerMemberRepository;
import com.example.repository.CustomerTokenRepository;
import com.example.token.TokenCreate;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping(value = "/api/customer")
@RequiredArgsConstructor
public class CustomerMemberRestController {

    final CustomerMemberMapper customerMemberMapper;
    final CustomerTokenRepository customerTokenRepository;
    final TokenCreate tokenCreate;
    final TokenMapper tokenMapper;
    final HttpSession httpSession;
    final CartRepository cartRepository;
    final CustomerMemberRepository customerMemberRepository;

    BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();

    // 로그아웃
    // 127.0.0.1:8080/ROOT/api/customer/logout.do+
    @PostMapping(value = "/logout.do")
    public Map<String, Object> logoutPOST(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        String rawToken = token.replace("Bearer ", "").trim();
        try {
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEamil = (String) tokenData.get("customerEmail");

            if (customerEamil != null) {
                customerTokenRepository.deleteByToken(rawToken);
                map.put("status", 200);
                map.put("message", "로그아웃 성공");
            } else {
                map.put("status", 401);
                map.put("message", "유효하지 않은 사용자 정보");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
            map.put("message", "로그아웃 중 오류 발생");
        }
        return map;
    }

    // 회원탈퇴
    @PutMapping(value = "/delete.do")
    public Map<String, Object> deletePUT(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        String rawToken = token.replace("Bearer ", "").trim();
        try {
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEamil = (String) tokenData.get("customerEmail");

            if (customerEamil == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            CustomerMemberDTO customerMember = customerMemberMapper.selectCustomerMemberOne(customerEamil);

            if (customerMember == null) {
                map.put("status", 404);
                map.put("message", "회원 정보를 찾을 수 없습니다.");
                return map;
            }

            // 회원 삭제 쿼리 실행
            int result = customerMemberMapper.deleteCustomer(customerEamil);

            if (result > 0) {
                map.put("status", 200);
                System.out.println("회원 삭제 성공");
            } else {
                map.put("status", 400);
                System.out.println("회원 삭제 실패");
            }
        } catch (Exception e) {
            map.put("status", -1);
            System.err.println(e.getMessage());
        }
        return map;
    }

    // 마이페이지
    @GetMapping(value = "/mypage.do")
    public Map<String, Object> myPage(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        try {
            // Bearer 접두사를 제거하고 토큰만 추출
            String rawToken = token.replace("Bearer ", "").trim();
            // 토큰 유효성 검사
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            if (customerEmail == null) {
                map.put("status", 401);
                map.put("message", "유효하지 않은 사용자입니다.");
                return map;
            }

            // 이메일을 기반으로 사용자 정보를 조회
            CustomerMemberDTO customerMember = customerMemberMapper.selectCustomerMemberOne(customerEmail);

            if (customerMember != null) {
                map.put("customerEmail", customerMember.getCustomerEmail());
                map.put("nickname", customerMember.getNickname());
                map.put("phone", customerMember.getPhone());
                map.put("status", 200);
            } else {
                map.put("status", 404);
                map.put("message", "사용자 정보를 찾을 수 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류가 발생했습니다.");
        }
        return map;
    }

    // 정보수정(닉네임, 핸드폰)
    @PutMapping(value = "/update.do")
    public Map<String, Object> updatePUT(@RequestBody CustomerMemberDTO obj,
            @RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");

            // DB에서
            CustomerMemberDTO customerMember = customerMemberMapper.selectCustomerMemberOne(customerEmail);

            if (customerMember == null) {
                map.put("status", 403);
                map.put("message", "권한이 없습니다. 유효하지 않은 사용자입니다.");
                return map;
            }

            // 3. 업데이트할 필드 설정 (닉네임과 핸드폰 번호 비밀번호만 변경 가능)
            if (obj.getNickname() != null && !obj.getNickname().isEmpty()) {
                customerMember.setNickname(obj.getNickname());
            }
            if (obj.getPhone() != null && !obj.getPhone().isEmpty()) {
                customerMember.setPhone(obj.getPhone());
            }
            // if (obj.getPassword() != null && !obj.getPassword().isEmpty()) {
            // String encodedPassword = bcpe.encode(obj.getPassword());
            // customerMember.setPassword(encodedPassword);
            // }

            int result = customerMemberMapper.updateCustomer(customerMember);
            // 업데이트 결과 확인
            if (result > 0) {
                map.put("status", 200);
                map.put("message", "회원 정보가 성공적으로 업데이트되었습니다.");
            } else {
                map.put("status", 0);
                map.put("message", "회원 정보 업데이트에 실패하였습니다.");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }

    // 로그인 시 비밀번호 잊었을 때 재설정(이메일 맞으면 비밀번호 변경 가능)
    // 127.0.0.1:8080/ROOT/api/customer/forgotpassword.do
    @PutMapping(value = "/forgotpassword.do")
    public Map<String, Object> forgotpasswordPUT(@RequestParam String customerEmail,
            @RequestParam String newPwd) {
        Map<String, Object> map = new HashMap<>();
        System.out.println(customerEmail);

        // 아이디와 이메일을 확인하고, 비밀번호를 업데이트
        CustomerMemberDTO customerMemberDTO = customerMemberMapper.findCustomerMemberDTOByEmail(customerEmail);
        if (customerMemberDTO != null) {
            // 새 비밀번호 암호화
            String encodedPwd = bcpe.encode(newPwd);
            customerMemberDTO.setPassword(encodedPwd);
            int result = customerMemberMapper.updatePassword(customerMemberDTO);

            if (result > 0) {
                map.put("status", 200);
                map.put("message", "비밀번호가 재설정되었습니다.");
            } else {
                map.put("status", 400);
                map.put("message", "비밀번호 재설정에 실패했습니다.");
            }
        } else {
            map.put("status", -1);
            map.put("message", "아이디 또는 이메일이 잘못되었습니다.");
        }
        return map;
    }

    // 비밀번호 수정(현재 비밀번호 확인 후 변경 가능)
    // 127.0.0.1:8080/ROOT/api/customer/updatepassword.do
    @PutMapping(value = "/updatepassword.do")
    public Map<String, Object> updatePasswordPOST(@RequestHeader(name = "Authorization") String token,
            @RequestParam String currentPwd, @RequestParam String newPwd) {
        Map<String, Object> map = new HashMap<>();
        try {
            // Bearer 접두사를 제거하여 순수 토큰만 전달
            String rawToken = token.replace("Bearer ", "").trim();
            Map<String, Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
            String customerEmail = (String) tokenData.get("customerEmail");
            System.out.println(customerEmail);
            if (customerEmail == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            CustomerMemberDTO customerMemberDTO = customerMemberMapper.selectCustomerMemberOne(customerEmail);

            if (customerMemberDTO != null) {
                // 현재 비밀번호가 일치하는지 확인
                if (bcpe.matches(currentPwd, customerMemberDTO.getPassword())) {
                    // 새 비밀번호 암호화
                    String encodedNewPwd = bcpe.encode(newPwd);
                    customerMemberDTO.setPassword(encodedNewPwd);

                    // 비밀번호 변경
                    int result = customerMemberMapper.updatePassword(customerMemberDTO);

                    if (result > 0) {
                        map.put("status", 200);
                        map.put("message", "비밀번호가 변경되었습니다.");
                    } else {
                        map.put("status", 400);
                        map.put("message", "비밀번호 변경에 실패했습니다.");
                    }
                } else {
                    map.put("status", 400);
                    map.put("message", "현재 비밀번호가 올바르지 않습니다.");
                }
            } else {
                map.put("status", 404);
                map.put("message", "회원 정보를 찾을 수 없습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류");
        }
        return map;
    }

    // 로그인
    // 127.0.0.1:8080/ROOT/api/customer/login.do
    @PostMapping(value = "/login.do")
    public Map<String, Object> loginPOST(@RequestBody CustomerMemberDTO obj) {
        Map<String, Object> map = new HashMap<>();

        try {
            // DB에 저장된 아이디를 이용해 아이디와 비밀번호 불러옴
            CustomerMemberDTO customerMember = customerMemberMapper.selectCustomerMemberOne(obj.getCustomerEmail());
            System.out.println(obj);
            map.put("status", 0);

            // 사용자가 존재하지 않거나 삭제된 회원인 경우 처리
            if (customerMember == null) {
                map.put("status", 404);
                map.put("message", "사용자를 찾을 수 없습니다.");
                return map;
            }

            if (customerMember.isIsdeleted()) {
                map.put("status", 403);
                map.put("message", "삭제된 계정입니다.");
                return map;
            }

            // 사용자가 입력한 암호와, DB의 암호 비교
            if (bcpe.matches(obj.getPassword(), customerMember.getPassword())) {

                // 토큰 발행할 데이터
                System.out.println("Customer Email: " + customerMember.getCustomerEmail());
                Map<String, Object> claims = new HashMap<>();
                claims.put("customerEmail", customerMember.getCustomerEmail()); // DB에서 가져온 정보
                claims.put("customerNickname", customerMember.getNickname()); // DB에서 가져온 닉네임
                claims.put("customerPhone", customerMember.getPhone());

                System.out.println("Claims for token generation: " + claims);

                // 토큰생성 map1 (아이디, 만료시간)
                Map<String, Object> map1 = tokenCreate.generateCustomerToken(claims);

                // DB에 추가
                CustomerToken ct = new CustomerToken();
                ct.setId(obj.getCustomerEmail());
                ct.setToken((String) map1.get("token"));
                ct.setExpiretime((Date) map1.get("expiretime"));
                tokenMapper.insertCustomerToken(ct);

                map.put("token", map1.get("token"));
                map.put("status", 200);
            } else {
                map.put("status", 401); // 비밀번호 불일치
                map.put("message", "비밀번호가 일치하지 않습니다.");
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("status", -1);
        }
        return map;
    }

    // 127.0.0.1:8080/ROOT/api/customer/checkemail
    @GetMapping("/checkemail")
    public Map<String, Object> checkEmailGET(@RequestParam String customerEmail){
        boolean isDuplicate = customerMemberRepository.existsByCustomerEmail(customerEmail);
        Map<String, Object> map = new HashMap<>();

        try {
            if (!isDuplicate) {
                map.put("status", 200);
                map.put("message", "사용 가능한 이메일입니다.");
            } else {
                map.put("status", 0);
                map.put("message", "이미 사용 중인 이메일입니다.");
            }

        } catch (Exception e){
            System.err.println(e.getMessage());
            map.put("stauts", -1);
        }
        return map;
    }

    // 회원가입
    @PostMapping(value = "/join.do")
    public Map<String, Object> joinPOST(@RequestBody CustomerMemberDTO obj) {
        System.out.println(obj.toString());
        Map<String, Object> map = new HashMap<>();

        try {
            obj.setPassword(bcpe.encode(obj.getPassword()));

            int ret = customerMemberMapper.insertCustomerMemberOne(obj);
            map.put("status", 0);
            if (ret == 1) {
                map.put("status", 200);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            map.put("stauts", -1);
        }
        return map;
    }

}
