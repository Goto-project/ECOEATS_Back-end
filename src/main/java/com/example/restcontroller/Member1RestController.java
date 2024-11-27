// package com.example.restcontroller;

// import java.util.Date;
// import java.util.HashMap;
// import java.util.Map;

// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.dto.Member1;
// import com.example.dto.Token1;
// import com.example.mapper.MemberMapper;
// import com.example.mapper.TokenMapper;
// import com.example.token.TokenCreate;

// import lombok.RequiredArgsConstructor;

// //react, android 등의 프론트에서 연동
// @RestController
// @RequestMapping(value = "/api/member1")
// @RequiredArgsConstructor
// public class Member1RestController {

//     // 토큰 발행 및 검증용 컴포먼트 객체 작성
//     final TokenCreate tokenCreate;
    
//     final MemberMapper memberMapper;
//     final TokenMapper tokenMapper;
//     // 회원가입시에 암호화 방식
//     BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();


//     //리액트에서 아이디와 암호를 전달해줌 -> DB에 있는지 확인 ->  토큰 발행
//     //ex) const body = {"id:"aaa", "pw":"bbb"}
//     @PostMapping(value = "path")
//     public Map<String, Object> postMethodName(@RequestBody Member1 obj) {
//         Map<String, Object> map = new HashMap<>();
//         try {
//             //아이디를 이용해서 정보를 꺼냄 (아이디,암호,권한)
//             Member1 obj1 = memberMapper.selectMember1One(obj.getId());
//             //앞쪽이 사용자가 입력한 내용(암호x), 뒤쪽이 DB의 암호 (암호o)
//             if(bcpe.matches(obj.getPw(), obj1.getPw())) {
//                 // map1.put("phone",)
//                 // map1.put("name",)
//                 // tokenCreate.generateSellerToken(map1)
//             }
//         } catch (Exception e) {
//             System.err.println(e.getMessage());
//             map.put("status", -1);
//         }
        
//         return map;
//     }
    














//     //로그인 이후에(토큰이 검증되고 난 후) 회원정보 수정, 암호변경, 회원탈퇴
//     @PutMapping(value = "/update.do")
//     public Map<String, Object> updatePOST(@RequestBody Member1 obj){
//         Map<String, Object> map = new HashMap<>();
//         try{
            
//         } catch(Exception e){
//             System.err.println(e.getMessage());
//             map.put("status", -1);
//         }
//         return map;
//     }

//     //로그인 => 토큰 => DB에 보관
//     // 네이버, 카카오 로그인 => DB에 보관
//     @PostMapping(value = "/login.do")
//     public Map<String, Object> loginPOST(@RequestBody Member1 obj){
//         Map<String, Object> map = new HashMap<>();
//         try{
//             //아이디를 이용해서 아이디와 암호를 가져옴
//             Member1 mem1 = memberMapper.selectMember1One(obj.getId());
//             //사용자가 입력한 암호와 엔코더된 DB 암호 비교
//             map.put("status", 0);
//             if (bcpe.matches(obj.getPw(), mem1.getPw())) {
//                 //토큰 발행할 데이터 생성(아이디, 이름...)
//                 Map<String, Object> send1 = new HashMap<>();
//                 send1.put("user_id", obj.getId());

//                 //토큰 생성 map1 아이디, 만료시간
//                 Map<String, Object> map1 = tokenCreate.create(send1);

//                 //DB에 추가하고
//                 Token1 t1 = new Token1();
//                 t1.setId(obj.getId());
//                 t1.setToken((String)map1.get("token"));
//                 t1.setExpiretime((Date) map1.get("expiretime"));
//                 tokenMapper.insertToken(t1);

//                 //토큰값 전송
//                 map.put("token", map1.get("token"));
//                 map.put("status", 200);
                
//             }
//         }catch(Exception e){
//             System.err.println(e.getMessage());
//             map.put("status", -1);
//         }
//         return map;
//     }


//     // 회원가입
//     //{"id":"a201", "pw":"a201", "name":"가나다", "phone":"010", "age":12, "role":"CUSTOMER"}
//     @PostMapping(value = "/join.do")
//     public Map<String, Object> joinPOST(@RequestBody Member1 obj) {
//         System.out.println(obj.toString());
//         Map<String, Object> map = new HashMap<>();
//         try {
//             //전달받은 암호에서 암호화하여 obj에 다시 저장하기
//             obj.setPw(bcpe.encode(obj.getPw()));

//             int ret = memberMapper.insertMember1One(obj);
//             map.put("status", 0);
//             if (ret == 1) {
//                 map.put("status", 200);
//             }

//         } catch (Exception e) {
//             System.err.println(e.getMessage());
//             map.put("status", -1);
//         }
//         //@RestController 로 인해 jackson 라이브러리가 동작해서 map을 json으로 변경해줌
//         return map;
//     }
// }
