package com.example.restcontroller;

import java.util.HashMap;
import java.util.List;
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
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");

            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            menu.setStoreId(storeId);
            int menuResult = menuMapper.insertMenu(menu);

            if (menuResult > 0) {
                map.put("status", 200);
                map.put("message", "메뉴 추가 성공");

                if (file != null && !file.isEmpty()) {
                    MenuImage menuImage = new MenuImage();
                    menuImage.setMenuNo(menu.getMenuNo());
                    menuImage.setFilename(file.getOriginalFilename());
                    menuImage.setFiletype(file.getContentType());
                    menuImage.setFilesize(file.getSize());
                    menuImage.setFiledata(file.getBytes());

                    int imageResult = menuImageMapper.insertMenuImage(menuImage);
                    map.put("imageStatus", imageResult > 0 ? "이미지 저장 성공" : "이미지 저장 실패");
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
            int result = menuMapper.deleteMenu(menuNo);
            map.put("status", result > 0 ? 200 : 400);
            map.put("message", result > 0 ? "메뉴 삭제 성공" : "메뉴 삭제 실패");

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류: " + e.getMessage());
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
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");

            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            menu.setStoreId(storeId);
            menu.setMenuNo(menuNo);
            int menuResult = menuMapper.updateMenu(menu);

            if (menuResult > 0) {
                map.put("status", 200);
                map.put("message", "메뉴 수정 성공");

                if (file != null && !file.isEmpty()) {
                    MenuImage menuImage = new MenuImage();
                    menuImage.setMenuNo(menuNo);
                    menuImage.setFilename(file.getOriginalFilename());
                    menuImage.setFiletype(file.getContentType());
                    menuImage.setFilesize(file.getSize());
                    menuImage.setFiledata(file.getBytes());

                    menuImageMapper.deleteMenuImageByMenuNo(menuNo);
                    int imageResult = menuImageMapper.insertMenuImage(menuImage);
                    map.put("imageStatus", imageResult > 0 ? "이미지 저장 성공" : "이미지 저장 실패");
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

    // 메뉴 조회
    @GetMapping("/list")
    public Map<String, Object> getMenuList(@RequestHeader(name = "Authorization") String token) {
        Map<String, Object> map = new HashMap<>();
        String rawToken = token.replace("Bearer ", "").trim();

        try {
            Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
            String storeId = (String) tokenData.get("storeId");

            if (storeId == null) {
                map.put("status", 401);
                map.put("message", "로그인된 사용자 정보가 없습니다.");
                return map;
            }

            List<Menu> menuList = menuMapper.selectMenuList(storeId);
            map.put("status", !menuList.isEmpty() ? 200 : 404);
            map.put("message", !menuList.isEmpty() ? "메뉴 조회 성공" : "메뉴가 없습니다.");
            map.put("menuList", menuList);

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류: " + e.getMessage());
        }

        return map;
    }
}
