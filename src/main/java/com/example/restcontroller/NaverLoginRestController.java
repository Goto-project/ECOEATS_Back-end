package com.example.restcontroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.dto.CustomerMemberDTO;
import com.example.dto.CustomerToken;
import com.example.mapper.CustomerMemberMapper;
import com.example.mapper.TokenMapper;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;




@RestController
@RequestMapping(value = "/api/naver")
@RequiredArgsConstructor
public class NaverLoginRestController {

    final CustomerMemberMapper customerMemberMapper;
    final TokenCreate tokenCreate;
    final TokenMapper tokenMapper;

    @RequestMapping(value = "/getInfo.json")
    public Map<String, Object> getInfo(@RequestHeader(name = "token") String token) {
        System.out.println("222222");
        System.out.println(token);
        Map<String, Object> map = new HashMap<>();
        try {
            String header = "Bearer " + token; // Bearer 다음에 공백 추가
            String apiURL = "https://openapi.naver.com/v1/nid/me";
    
            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization", header);
            String responseBody = get(apiURL, requestHeaders);
    
            JSONObject jobj = new JSONObject(responseBody.toString());
            String s1 = jobj.getString("message");
    
            JSONObject jobj1 = jobj.getJSONObject("response");
            String email = jobj1.getString("email");
            String name = jobj1.getString("name");
            String mobile = jobj1.getString("mobile");
    
            CustomerMemberDTO obj = new CustomerMemberDTO();
            obj.setCustomerEmail(email);
            obj.setNickname(name);
            obj.setPhone(mobile);
    
            // 사용자 확인
            CustomerMemberDTO existingMember = customerMemberMapper.selectCustomerMemberOne(email);
    
            if (existingMember == null) {
                // 새 사용자 삽입
                customerMemberMapper.insertCustomerMemberOne(obj);
                System.out.println("새 사용자 등록: " + email);
            } else {
                // 기존 사용자 로그인 처리
                System.out.println("기존 사용자 로그인 처리: " + email);
            }
    
            // 토큰 발행
            Map<String, Object> claims = new HashMap<>();
            claims.put("customerEmail", email);
            claims.put("customerNickname", name);
            claims.put("customerPhone", mobile);
    
            Map<String, Object> tokenData = tokenCreate.generateCustomerToken(claims);
    
            CustomerToken ct = new CustomerToken();
            ct.setId(email);
            ct.setToken((String) tokenData.get("token"));
            ct.setExpiretime((Date) tokenData.get("expiretime"));
            tokenMapper.insertCustomerToken(ct);
    
            map.put("message", s1);
            map.put("email", email);
            map.put("name", name);
            map.put("status", 200);
            map.put("token", tokenData.get("token")); // 생성된 토큰 반환
        } catch (Exception e) {
            map.put("status", -1);
            map.put("message", "오류 발생: " + e.getMessage());
        }
        return map;
    }

    
    private static String get(String apiUrl, Map<String, String> requestHeaders) {
        HttpURLConnection con = connect(apiUrl);
        try {
            con.setRequestMethod("GET");
            for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
                con.setRequestProperty(header.getKey(), header.getValue());
            }

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
                return readBody(con.getInputStream());
            } else { // 에러 발생
                return readBody(con.getErrorStream());
            }
        } catch (IOException e) {
            throw new RuntimeException("API 요청과 응답 실패", e);
        } finally {
            con.disconnect();
        }
    }

    private static HttpURLConnection connect(String apiUrl) {
        try {
            URL url = new URL(apiUrl);
            return (HttpURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
        } catch (IOException e) {
            throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
        }
    }

    private static String readBody(InputStream body) {
        InputStreamReader streamReader = new InputStreamReader(body);

        try (BufferedReader lineReader = new BufferedReader(streamReader)) {
            StringBuilder responseBody = new StringBuilder();

            String line;
            while ((line = lineReader.readLine()) != null) {
                responseBody.append(line);
            }

            return responseBody.toString();
        } catch (IOException e) {
            throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
        }
    }
}
