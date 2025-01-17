package com.example.restcontroller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.StoreDTO;
import com.example.dto.StoreImage;
import com.example.dto.StoreToken;
import com.example.entity.Store;
import com.example.mapper.StoreImageMapper;
import com.example.mapper.StoreMapper;
import com.example.mapper.TokenMapper;
import com.example.repository.StoreTokenRepository;
import com.example.service.CustomerAddressService;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/seller")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3001")
public class StoreRestController {

    BCryptPasswordEncoder bcpe = new BCryptPasswordEncoder();
    final StoreMapper storeMapper;
    final StoreImageMapper storeImageMapper;
    final CustomerAddressService customerAddressService;

    // 토큰 발행 및 검증용 컴포넌트 객체 생성
    final TokenCreate tokenCreate;
    final TokenMapper tokenMapper;

    final StoreTokenRepository storeTokenRepository;

    // 회원 삭제
    // 127.0.0.1:8080/ROOT/api/seller/delete.do
    @PutMapping(value = "/delete.do")
    public Map<String, Object> deletePUT(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            // 토큰 유효성 검사 및 storeId 추출
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");
            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            StoreDTO store = storeMapper.selectStoreOne(storeId);
            if (store == null) {
                map.put("status", 404);
                map.put("message", "회원 정보를 찾을 수 없습니다.");
                return map;
            }

            // 회원 삭제 쿼리 실행
            int result = storeMapper.updateStoreToDeleted(storeId);

            if (result > 0) {
                map.put("status", 200);
                System.out.println("회원 삭제 성공");
            } else {
                map.put("status", 400);
                System.out.println("회원 삭제 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
        }
        return map;
    }

    // 로그인 시 비밀번호 잊었을 때 재설정(아이디, 이메일 맞으면 비밀번호 변경 가능)
    // 127.0.0.1:8080/ROOT/api/seller/forgotpassword.do
    @PutMapping(value = "/forgotpassword.do")
    public Map<String, Object> forgotPasswordPUT(@RequestParam String storeId,
            @RequestParam String storeEmail,
            @RequestParam String newPwd) {
        Map<String, Object> map = new HashMap<>();

        // 아이디와 이메일을 확인하고, 비밀번호를 업데이트
        StoreDTO store = storeMapper.findStoreByIdAndEmail(storeId, storeEmail);
        if (store != null) {
            // 새 비밀번호 암호화
            String encodedPwd = bcpe.encode(newPwd);
            store.setPassword(encodedPwd);
            int result = storeMapper.updatePassword(store);

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
    // 127.0.0.1:8080/ROOT/api/seller/updatepassword.do
    @PutMapping(value = "/updatepassword.do")
    public Map<String, Object> updatePasswordPOST(@RequestHeader(name = "Authorization") String token,
            @RequestParam String currentPwd, @RequestParam String newPwd) {
        Map<String, Object> map = new HashMap<>();
        try {
            // Bearer 접두사를 제거하여 순수 토큰만 전달
            String rawToken = token.replace("Bearer ", "").trim();
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");
            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            StoreDTO seller = storeMapper.selectStoreOne(storeId);

            if (seller != null) {
                // 현재 비밀번호가 일치하는지 확인
                if (bcpe.matches(currentPwd, seller.getPassword())) {
                    // 새 비밀번호 암호화
                    String encodedNewPwd = bcpe.encode(newPwd);
                    seller.setPassword(encodedNewPwd);

                    // 비밀번호 변경
                    int result = storeMapper.updatePassword(seller);

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

    // 정보 수정
    // 127.0.0.1:8080/ROOT/api/seller/update.do
    @PutMapping(value = "/update.do", consumes = { "multipart/form-data" })
public Map<String, Object> updatePUT(@RequestPart("store") Store store,
        @RequestHeader(name = "Authorization") String token,
        @RequestPart(value = "file", required = false) MultipartFile file) {
    Map<String, Object> map = new HashMap<>();
        System.out.println(store.toString());

        System.out.println("----------------------");

    // Bearer 접두사를 제거하여 순수 토큰만 전달
    String rawToken = token.replace("Bearer ", "").trim();

    try {
        Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
        String storeId = (String) tokenData.get("storeId");
        if (storeId == null) {
            map.put("status", 401);
            map.put("message", "로그인된 사용자 정보가 없습니다.");
            return map;
        }

        Store seller = storeMapper.selectStoreOne2(storeId);

        seller.setStoreId(storeId);

        if (store.getStoreName() != null && !store.getStoreName().isEmpty()) {
            seller.setStoreName(store.getStoreName());
        }

        // 입력값이 없을 때 DB에 null이 들어가지 않도록 처리
        if (store.getStoreName() != null && !store.getStoreName().isEmpty()) {
            seller.setStoreName(store.getStoreName());
        }

        if (store.getAddress() != null && !store.getAddress().isEmpty()) {
            seller.setAddress(store.getAddress());
            
            // 주소로부터 위도와 경도 가져오기
            try {
                // 주소로부터 위도, 경도 가져오기
                Map<String, BigDecimal> coordinates = customerAddressService.saveCustomerAddress(store.getAddress());
                seller.setLatitude(coordinates.get("latitude"));
                seller.setLongitude(coordinates.get("longitude"));
            } catch (IllegalArgumentException e) {
                map.put("status", 400);
                map.put("message", "주소를 찾을 수 없습니다.");
                return map;
            }
        }

        if (store.getPhone() != null && !store.getPhone().isEmpty()) {
            seller.setPhone(store.getPhone());
        }

        if (store.getCategory() != null && !store.getCategory().isEmpty()) {
            seller.setCategory(store.getCategory());
        }

        if (store.getStartPickup() != null) {
            seller.setStartPickup(store.getStartPickup());
        }

        if (store.getEndPickup() != null) {
            seller.setEndPickup(store.getEndPickup());
        }

        // if (store.getLatitude() != null) {
        //     seller.setLatitude(store.getLatitude());
        // }

        // if (store.getLongitude() != null) {
        //     seller.setLongitude(store.getLongitude());
        // }

        // 이미지 업데이트 로직
        if (file != null && !file.isEmpty()) {
            // 기존 이미지 삭제
            System.out.println(storeId);
            StoreImage storeImage = storeImageMapper.selectStoreImageByStoreId(storeId);
            // 이미지가 있으면 덮어쓰기 로직
            if (storeImage != null) {
                System.out.println(storeImage.getStoreId());
                System.out.println("storeImage : " + storeImage.getFilename());
                System.out.println(storeImage.getStoreimageNo());
                storeImage.setFilename(file.getOriginalFilename());
                storeImage.setFiletype(file.getContentType());
                storeImage.setFilesize(file.getSize());
                storeImage.setFiledata(file.getBytes());
                storeImageMapper.updateStoreImage(storeImage); // 이미지 정보 업데이트

            } else {
                // 이미지가 없으면 새로 추가하는 로직
                storeImage = new StoreImage();
                storeImage.setStoreId(storeId);
                storeImage.setFilename(file.getOriginalFilename());
                storeImage.setFiletype(file.getContentType());
                storeImage.setFilesize(file.getSize());
                storeImage.setFiledata(file.getBytes());
                storeImageMapper.insertStoreImage(storeImage); // 새 이미지 정보 추가
            }
        }

        // 정보 업데이트
        int result = storeMapper.updateStore(seller);

        // 업데이트 성공 여부 확인
        if (result > 0) {
            map.put("status", 200);
            map.put("message", "회원 정보 수정 성공");
        } else {
            map.put("status", 400);
            map.put("message", "회원 정보 수정 실패");
        }

    } catch (Exception e) {
        e.printStackTrace();
        map.put("status", -1);
    }
    return map;
} 

    // 로그아웃
    // 토큰으로 로그인했으므로 토큰 테이블에서 데이터 삭제 => 로그아웃됨
    // 127.0.0.1:8080/ROOT/api/seller/logout.do
    @DeleteMapping(value = "/logout.do")
    public Map<String, Object> logoutDELETE(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            // 토큰 유효성 검사 및 storeId 추출
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");
            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            // 회원 로그아웃 쿼리 실행
            int result = storeTokenRepository.deleteById_StoreId(storeId);

            if (result > 0) {
                map.put("status", 200);
                System.out.println("로그아웃 성공");
            } else {
                map.put("status", 400);
                System.out.println("로그아웃 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
        }
        return map;
    }

    // 리액트에서 아이디와 암호를 전달해줌 => DB에 있는지 확인 => 토큰 발행
    // const body = {"storeId":"a201", "password":"a201"} 키는 dto와 맞추기 값은 DB에 있는 걸
    // 해야함
    // 127.0.0.1:8080/ROOT/api/seller/login.do
    @PostMapping(value = "/login.do")
    public Map<String, Object> loginPOST(@RequestBody StoreDTO store) {
        Map<String, Object> map = new HashMap<>();
        try {
            // 아이디를 이용해서 아이디와 암호 가져오기
            StoreDTO seller = storeMapper.selectStoreOne(store.getStoreId());
            map.put("status", 0);

            // 사용자가 입력한 아이디가 DB에 존재하지 않거나 삭제된 계정인 경우 처리
            if (seller == null) {
                map.put("status", 404);
                map.put("message", "사용자를 찾을 수 없습니다.");
                return map;
            }

            if (seller.isIsdeleted()) { // isdeleted가 true일 경우
                map.put("status", 403);
                map.put("message", "삭제된 계정입니다.");
                return map;
            }

            // 사용자가 입력한 암호와 엔코더된 DB 암호 비교
            // 앞쪽이 사용자가 입력한 내용(암호화x), 뒷쪽이 DB의 암호(암호화o)
            if (bcpe.matches(store.getPassword(), seller.getPassword())) {
                // 토큰 발행할 데이터 생성(아이디, 이름...)
                Map<String, Object> send1 = new HashMap<>();
                send1.put("storeId", seller.getStoreId());
                send1.put("role", seller.getRole());

                // 토큰 생성 seller 아이디, 만료시간
                Map<String, Object> map1 = tokenCreate.generateSellerToken(send1);

                // DB에 추가하고
                StoreToken storeToken = new StoreToken();
                storeToken.setId(store.getStoreId());
                storeToken.setToken((String) map1.get("token"));
                storeToken.setExpiretime((Date) map1.get("expiretime"));
                tokenMapper.insertStoreToken(storeToken);

                // 토큰값 전송
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
    
    // 회원가입
    //KEY:store{"storeId":"bbq1", "storeEmail":"kfc1@store.com", "password":"1", "storeName":"BBQ Store", "address":"부산광역시 부산진구 중앙대로 681-1", 
    //"phone":"010-1234-5678", "category":"Fast Food", "startPickup":"08:00", "endPickup":"20:00"}
    //coordinate: "address":"부산광역시 부산진구 중앙대로 681-1" <위도 경도 키워드
    @PostMapping(value = "/join.do", consumes = { "multipart/form-data" }) 
public Map<String, Object> joinPOST(
    @RequestPart("store") Store store,
    @RequestPart(value = "file", required = false) MultipartFile file) {
    
    Map<String, Object> map = new HashMap<>();
    try {
        // 아이디 중복 체크
        int idExists = storeMapper.checkStoreIdExists(store.getStoreId());
        if (idExists > 0) {
            map.put("status", 409); // HTTP 409 Conflict
            map.put("message", "이미 존재하는 아이디입니다.");
            return map;
        }

        // 주소 처리 (store의 address로 좌표 변환)
        if (store.getAddress() != null && !store.getAddress().trim().isEmpty()) {
            try {
                Map<String, BigDecimal> coordinates = customerAddressService.saveCustomerAddress(store.getAddress());
                store.setLatitude(coordinates.get("latitude"));
                store.setLongitude(coordinates.get("longitude"));
            } catch (IllegalArgumentException e) {
                map.put("status", 400);
                map.put("message", "유효하지 않은 주소입니다.");
                return map;
            }
        }

        // 비밀번호 암호화
        store.setPassword(bcpe.encode(store.getPassword()));

        // Store 저장
        int ret = storeMapper.insertStoreOne(store);
        if (ret == 1) {
            map.put("status", 200);
            map.put("message", "가게 정보가 성공적으로 저장되었습니다.");

            // 이미지 저장
            if (file != null && !file.isEmpty()) {
                StoreImage storeImage = new StoreImage();
                storeImage.setStoreId(store.getStoreId());
                storeImage.setFilename(file.getOriginalFilename());
                storeImage.setFiletype(file.getContentType());
                storeImage.setFilesize(file.getSize());
                storeImage.setFiledata(file.getBytes());

                storeImageMapper.insertStoreImage(storeImage);
                map.put("image", "이미지가 성공적으로 저장되었습니다.");
            }
        } else {
            map.put("status", -1);
            map.put("message", "가게 정보 저장에 실패했습니다.");
        }
    } catch (Exception e) {
        e.printStackTrace();
        map.put("status", -1);
        map.put("message", "서버 오류가 발생했습니다.");
    }

    return map;
}

}
