// package com.example.restcontroller;

// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.Map;

// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.RequestHeader;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;

// import com.example.entity.Cart;
// import com.example.entity.CustomerMember;
// import com.example.repository.CartRepository;
// import com.example.token.TokenCreate;

// import lombok.RequiredArgsConstructor;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;


// @RestController
// @RequestMapping(value = "/api/cart")
// @RequiredArgsConstructor
// public class CartRestController {
    
//     final CartRepository cartRepository;
//     final TokenCreate tokenCreate;
    
//     //장바구니 넣기
//     //127.0.0.1:8080/ROOT/api/cart/insert.do
//     // const no = [{no:1}, {no:2}]
//     @PostMapping("/insert.do")
//     public Map<String, Object> insertPOST(@RequestHeader(name = "Authorization") String token,
//                                         @RequestBody List<Integer> dailymenuNos) {

//         Map<String,Object> map = new HashMap<>();
//         Map<Integer, String> results = new HashMap<>(); // 카트 처리 결과 저장
        
//         String rawToken = token.replace("Bearer", "").trim();
//         try{
//             Map<String,Object> tokenData = tokenCreate.validateCustomerToken(rawToken);
//             String customerEmail = (String) tokenData.get("customerEmail");

//             if (customerEmail == null) {
//                 map.put("status", 401);
//                 map.put("message", "로그인된 사용자 정보가 없습니다.");
//                 return map;
//             }

//             for(int dailymenuNo : dailymenuNos){
                
//             }
//             map.put("status", 200);
//         }catch(Exception e){
//             map.put("status", -1);
//             System.err.println(e.getMessage());
//         }
//         return map;
//     }
    

//     //장바구니 삭제
//     //127.0.0.1:8080/ROOT/api/cart/delete.do


//     //장바구니 상세보기
//     //127.0.0.1:8080/ROOT/api/cart/details
//     @GetMapping(value = "/details.do")
//     public Map<String,Object> cartDetailGET(@RequestHeader(name = "Authorization") String token) {
//         Map<String,Object> map = new HashMap<>();
//         String rawToken = token.replace("Bearer", "").trim();
//         try{
//             Map<String,Object>tokenData = tokenCreate.validateCustomerToken(rawToken);
//             String customerEmail = (String) tokenData.get("customerEmail");

//             if(customerEmail == null){
//                 map.put("status", 401);
//                 map.put("message", "로그인된 사용자 정보가 없습니다.");
//                 return map;
//             }
//             List<Cart> cartDetails = cartRepository.findByCustomerEmail_CustomerEmail(customerEmail);

//             //데이터 출력
//             List<Map<String, Object>> result = new ArrayList<>();
//             int totalPrice = 0;
//             for(Cart cart:cartDetails){
//                 Map<String,Object> item = new HashMap<>();

//                  // 개별 메뉴의 합계 계산
//                 int itemTotal = cart.getDailymenuNo().getPrice() * cart.getQty();
//                 totalPrice += itemTotal;

//                 item.put("no", cart.getNo());
//                 item.put("menu_name", cart.getDailymenuNo().getMenuNo().getName());
//                 item.put("price", cart.getDailymenuNo().getPrice());
//                 item.put("qty", cart.getQty());
//                 item.put("customerEmail",cart.getCustomerEmail().getCustomerEmail());
//                 item.put("regdate", cart.getRegdate());
//                 item.put("item_total", itemTotal);

//                 result.add(item);
//             }   
//             map.put("status", 200);
//             map.put("data", result);
//             map.put("total_price", totalPrice);
//         }catch(Exception e){
//             map.put("status", -1);
//             System.out.println(e.getMessage());
//         }
//         return map;
//     }
    

// }
