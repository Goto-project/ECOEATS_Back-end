package com.example.restcontroller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping(value = "/api")
public class SseRestController {

    //전체 사용자를 보관할 변수 생성
    final Map<String, SseEmitter> clients = new ConcurrentHashMap<>();

    //사용자 등록
    @GetMapping("/subscribe")
    public SseEmitter subscribe(String id){
        SseEmitter emitter = new SseEmitter(600 * 1000L); //10분간 접속 유지
        clients.put(id, emitter);
        System.out.println(id + "," + emitter.toString());

        emitter.onTimeout(() -> clients.remove(id));
        emitter.onCompletion(() -> clients.remove(id));

        return emitter;
    }

    //사용자가 보내는 메세지
    @GetMapping(value = "/publish")
    public void publish(String message){
        // 전체 사용자에게 보낼 메시지
        Map<String, Object> sendMap = new HashMap<>();
        sendMap.put("status", 200);
        sendMap.put("msg", message);

        //전체 사용자를 반복
        for (String id : clients.keySet()){
            // 사용자에 대한 emliter를 하나씩 꺼냄
            SseEmitter emitter = clients.get(id);
            try{
                emitter.send(sendMap, MediaType.APPLICATION_JSON);

            }catch(Exception e){
                // 오류발생시 사용자가 없거나 접속이 안 되어 있음
                System.err.println(e.getMessage());
                // 사용자를 삭제
                clients.remove(id);
            }
        }
    }
    
}
