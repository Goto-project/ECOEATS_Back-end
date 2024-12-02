package com.example.restcontroller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.entity.DailyMenu;
import com.example.entity.Menu;
import com.example.entity.MenuImage;
import com.example.entity.Store;
import com.example.repository.DailyMenuRepository;
import com.example.repository.MenuImageRepository;
import com.example.repository.MenuRepository;
import com.example.repository.StoreRepository;
import com.example.token.TokenCreate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/dailymenu")
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequiredArgsConstructor
public class DailyMenuRestController {

    final TokenCreate tokenCreate;

    final MenuRepository menuRepository;
    final MenuImageRepository menuImageRepository;
    final DailyMenuRepository dailyMenuRepository;
    final StoreRepository storeRepository;

    // 127.0.0.1:8080/ROOT/api/menu/daily/list
    // 고객용
    @GetMapping("/list")
    public List<Map<String, Object>> dailyMenuListGET(@RequestParam String date, @RequestParam String storeId) {
        // 날짜 형식 검증 (yyyy-MM-dd 형식)
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("잘못된 날짜 형식입니다. yyyy-MM-dd 형식으로 입력하세요");
        }

        // storeId로 Store 객체 조회
        Store store = storeRepository.findByStoreId(storeId); // StoreRepository에서 Store를 조회합니다.
        if (store == null) {
            throw new IllegalArgumentException("존재하지 않는 가게 ID입니다.");
        }

        // storeId와 date를 기반으로 DailyMenu 목록 조회
        List<DailyMenu> dailyMenus = dailyMenuRepository.findByMenuNoStoreIdAndRegdate(store, parsedDate);

        // 결과를 Map 형태로 변환하여 반환 (Menu 정보 포함)
        List<Map<String, Object>> result = new ArrayList<>();
        for (DailyMenu dailyMenu : dailyMenus) {
            Map<String, Object> menuData = new HashMap<>();
            menuData.put("dailymenuNo", dailyMenu.getDailymenuNo());

            // Menu 엔티티에서 필요한 정보를 추출
            Menu menu = dailyMenu.getMenuNo(); // DailyMenu의 Menu 객체 가져오기
            if (menu != null) {
                MenuImage menuImage = menuImageRepository.findByMenu_menuNo(menu.getMenuNo());
                menuData.put("menuName", menu.getName());
                menuData.put("menuPrice", menu.getPrice());
                menuData.put("menuDiscountedPrice", dailyMenu.getPrice());
                menuData.put("menuQty", dailyMenu.getQty());
                menuData.put("menuImageUrl", menu.getImageurl() + menuImage.getMenuimageNo()); // 이미지 URL 설정
            }

            result.add(menuData);
        }

        return result;
    }

}
