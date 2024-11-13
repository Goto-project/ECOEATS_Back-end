package com.example.restcontroller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/naver")
public class CustomerNaverRestController {

    @RequestMapping(value = "/getInfo.json")
    public Map<String, Object> getInfo(@RequestHeader(name = "token") String token) {
        Map<String, Object> map = new HashMap<>();
        try {
            System.out.println(token);
            String header = "Bearer " + token; // Bearer 다음에 공백 추가
            String apiURL = "https://openapi.naver.com/v1/nid/me";

            Map<String, String> requestHeaders = new HashMap<>();
            requestHeaders.put("Authorization", header);
            String responseBody = get(apiURL, requestHeaders);


            //{"resultcode":"00","message":"success","response":{"id":"IH8PmHxxlntuO8oLa2lHOTPHW8AhOHwCG_COVMVNaic","email":"gmlsendol@gmail.com","name":"\uc774\ud61c\uc601"}}
            //{"id":"IH8PmHxxlntuO8oLa2lHOTPHW8AhOHwCG_COVMVNaic","email":"gmlsendol@gmail.com","name":"\uc774\ud61c\uc601"}
            System.out.println(responseBody);
            JSONObject jobj = new JSONObject(responseBody.toString());
            String s1 = jobj.getString("message");
            System.out.println(s1);

            JSONObject response = jobj.getJSONObject("response");
            String id = response.getString("id");
            System.out.println(id);

            String email = response.getString("email");
            System.out.println(email);

            String name = response.getString("name");
            System.out.println(name);

            // 네이버, 카카오로그인 사용하더라도 내 DB에 정보를 저장해야 함.

            map.put("message", s1);
            map.put("id", id);
            map.put("email", email);
            map.put("name", name);
            map.put("status", 200);

        } catch (Exception e) {
            map.put("status", -1);
        }
        return map;
    }

    
    //네이버에서 제공하는 함수들
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
