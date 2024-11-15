package com.example.restcontroller;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.Menu;
import com.example.dto.MenuImage;
import com.example.mapper.MenuImageMapper;
import com.example.mapper.MenuMapper;
import com.example.token.TokenCreate;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "http://localhost:3001")
@RequiredArgsConstructor
@Transactional
public class MenuRestController {

    final MenuMapper menuMapper;
    final MenuImageMapper menuImageMapper;
    final TokenCreate tokenCreate;

    // 메뉴 추가
    @PostMapping(value = "/add.do", consumes = { "multipart/form-data" })
    public Map<String, Object> addMenu(@RequestPart("menu") Menu menu,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestHeader(name = "Authorization") String token) {

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

            // 메뉴 추가 데이터에 storeId 설정
            menu.setStoreId(storeId);
            // 메뉴 삽입하고, 삽입된 메뉴의 번호(menu_no)를 가져옵니다.
            int menuResult = menuMapper.insertMenu(menu);

            if (menuResult > 0) {
                map.put("status", 200);
                map.put("message", "메뉴 추가 성공");

                // 메뉴 번호가 제대로 할당되었는지 확인
                if (menu.getMenuNo() == 0) {
                    map.put("status", 400);
                    map.put("message", "메뉴 번호가 제대로 할당되지 않았습니다.");
                    return map;
                }

                // 메뉴 번호를 메뉴 이미지에 추가
                int menuNo = menu.getMenuNo(); // 메뉴 번호 추출

                // 이미지가 있을 경우 메뉴 이미지 추가
                if (file != null && !file.isEmpty()) {
                    MenuImage menuImage = new MenuImage();
                    menuImage.setMenuNo(menuNo); // 방금 삽입된 메뉴의 번호 설정
                    menuImage.setFilename(file.getOriginalFilename());
                    menuImage.setFiletype(file.getContentType());
                    menuImage.setFilesize(file.getSize());
                    menuImage.setFiledata(file.getBytes());

                    int imageResult = menuImageMapper.insertMenuImage(menuImage);
                    if (imageResult > 0) {
                        map.put("imageStatus", "이미지 저장 성공");
                    } else {
                        map.put("imageStatus", "이미지 저장 실패");
                    }
                } else {
                    map.put("imageStatus", "이미지 없음");
                }

            } else {
                map.put("status", 400);
                map.put("message", "메뉴 추가 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류: " + e.getMessage());
        }
        return map;
    }

    @DeleteMapping("/delete/{menuNo}")
    public Map<String, Object> deleteMenu(@PathVariable("menuNo") int menuNo) {
        Map<String, Object> map = new HashMap<>();

        try {
            // menuNo를 전달하여 해당 메뉴 삭제
            int result = menuMapper.deleteMenu(menuNo);

            if (result > 0) {
                map.put("status", 200);
                map.put("message", "메뉴 삭제 성공");
            } else {
                map.put("status", 400);
                map.put("message", "메뉴 삭제 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류");
        }

        return map;
    }

    @PutMapping("/update/{menuNo}")
    public Map<String, Object> updateMenu(@PathVariable("menuNo") int menuNo,
            @RequestBody Menu menu,  // @RequestBody를 사용하여 Menu 객체를 JSON 형식으로 받음
            @RequestParam(value = "image", required = false) MultipartFile image) {
        Map<String, Object> map = new HashMap<>();
        System.out.println(menuNo);
    
        try {
            // Menu 객체의 menuNo 값을 URL 경로에서 전달된 menuNo로 설정
            menu.setMenuNo(menuNo);
    
            // 메뉴 수정 로직 호출
            int result = menuMapper.updateMenu(menu);
    
            if (result > 0) {
                // 이미지가 있다면 이미지 수정 처리
                if (image != null && !image.isEmpty()) {
                    // 새 이미지를 저장하는 로직
                    MenuImage menuImage = new MenuImage();
                    menuImage.setMenuNo(menuNo);
                    menuImage.setFilename(image.getOriginalFilename());
                    menuImage.setFiletype(image.getContentType());
                    menuImage.setFilesize(image.getSize());
                    menuImage.setFiledata(image.getBytes());
                    menuImage.setRegdate(new Date());
    
                    // 기존 이미지 삭제 (새 이미지가 없는 경우, 기존 이미지를 삭제해야 하므로 추가)
                    int deleteResult = menuMapper.deleteMenuImage(menuNo);
                    if (deleteResult > 0) {
                        // 이미지 수정 로직 호출
                        int imageResult = menuMapper.updateMenuImage(menuImage);
                        if (imageResult > 0) {
                            map.put("status", 200);
                            map.put("message", "메뉴 및 이미지 수정 성공");
                        } else {
                            map.put("status", 400);
                            map.put("message", "메뉴 수정 성공, 이미지 수정 실패");
                        }
                    } else {
                        map.put("status", 400);
                        map.put("message", "기존 이미지 삭제 실패");
                    }
                } else {
                    // 이미지가 없으면 기존 이미지를 삭제
                    int deleteResult = menuMapper.deleteMenuImage(menuNo);
                    if (deleteResult > 0) {
                        map.put("status", 200);
                        map.put("message", "메뉴 수정 성공, 이미지 삭제됨");
                    } else {
                        map.put("status", 400);
                        map.put("message", "메뉴 수정 성공, 이미지 삭제 실패");
                    }
                }
            } else {
                map.put("status", 400);
                map.put("message", "메뉴 수정 실패");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류");
        }
    
        return map;
    }
    

}
