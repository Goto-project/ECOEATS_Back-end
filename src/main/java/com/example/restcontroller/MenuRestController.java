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

    // 메뉴 삭제
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

    // 메뉴 수정
    @PutMapping(value = "/update/{menuNo}", consumes = { "multipart/form-data" })
    public Map<String, Object> updateMenu(@PathVariable("menuNo") int menuNo,
            @RequestPart("menu") Menu menu,
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

            // 메뉴 번호를 통해 기존 메뉴를 조회
            menu.setStoreId(storeId);
            menu.setMenuNo(menuNo);

            int menuResult = menuMapper.updateMenu(menu);

            if (menuResult > 0) {
                map.put("status", 200);
                map.put("message", "메뉴 수정 성공");

                // 이미지가 있을 경우 메뉴 이미지 수정
                if (file != null && !file.isEmpty()) {
                    MenuImage menuImage = new MenuImage();
                    menuImage.setMenuNo(menuNo);
                    menuImage.setFilename(file.getOriginalFilename());
                    menuImage.setFiletype(file.getContentType());
                    menuImage.setFilesize(file.getSize());
                    menuImage.setFiledata(file.getBytes());

                    // 기존 이미지를 삭제하고 새 이미지를 저장하는 로직
                    menuImageMapper.deleteMenuImageByMenuNo(menuNo); // 기존 이미지 삭제
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
                map.put("message", "메뉴 수정 실패");
            }

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류: " + e.getMessage());
        }

        return map;
    }

}
