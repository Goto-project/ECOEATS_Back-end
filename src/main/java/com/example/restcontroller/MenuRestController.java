package com.example.restcontroller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.dto.DailyMenuDTO;
import com.example.dto.DailyMenuRequestDTO;
import com.example.dto.MenuDTO;
import com.example.dto.MenuImageDTO;
import com.example.entity.DailyMenu;
import com.example.entity.Menu;
import com.example.entity.MenuImage;
import com.example.entity.Store;
import com.example.mapper.DailyMenuMapper;
import com.example.mapper.MenuImageMapper;
import com.example.mapper.MenuMapper;
import com.example.repository.DailyMenuRepository;
import com.example.repository.MenuImageRepository;
import com.example.repository.MenuRepository;
import com.example.repository.StoreRepository;
import com.example.token.TokenCreate;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "http://localhost:3001", allowCredentials = "true")
@RequiredArgsConstructor
@Transactional
public class MenuRestController {

    final DailyMenuMapper dailyMenuMapper;

    final MenuMapper menuMapper;
    final MenuImageMapper menuImageMapper;
    final TokenCreate tokenCreate;

    final MenuRepository menuRepository;
    final MenuImageRepository menuImageRepository;
    final DailyMenuRepository dailyMenuRepository;
    final StoreRepository storeRepository;

    // ======당일 판매 메뉴 관리=======
    // 127.0.0.1:8080/ROOT/api/menu/daily/storelist
    // 가게용
    @GetMapping("/daily/storelist")
    public List<Map<String, Object>> dailyMenuStoreListGET(
            @RequestParam String date,
            @RequestHeader(name = "Authorization") String token) {

        // Authorization 헤더가 없거나 잘못된 형식일 경우 예외 처리
        if (token == null || !token.startsWith("Bearer ")) {
            throw new IllegalArgumentException("유효한 토큰을 제공해야 합니다.");
        }

        // Bearer 접두사를 제거하여 순수 토큰만 전달
        String rawToken = token.replace("Bearer ", "").trim();
        Map<String, Object> tokenData = tokenCreate.validateSellerToken(rawToken);
        String storeId = (String) tokenData.get("storeId");

        if (storeId == null) {
            throw new IllegalArgumentException("토큰에서 storeId를 추출할 수 없습니다.");
        }

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
        List<DailyMenu> dailyMenus = dailyMenuRepository.findByMenuNoStoreIdAndRegdateAndMenuNoIsdeletedFalse(store, parsedDate);

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

    // 당일 판매 메뉴 삭제
    // 127.0.0.1:8080/ROOT/api/menu/daily/delete
    @DeleteMapping("/daily/delete")
    public Map<String, Object> deleteDailyMenu(@RequestBody List<Integer> dailymenuNos) {
        Map<String, Object> map = new HashMap<>();
        StringBuilder resultMessage = new StringBuilder();

        try {
            for (Integer dailymenuNo : dailymenuNos) {
                Optional<DailyMenu> optionalDailyMenu = dailyMenuRepository.findById(dailymenuNo);
                if (optionalDailyMenu.isPresent()) {
                    // 메뉴 존재하면 삭제
                    dailyMenuRepository.deleteById(dailymenuNo);
                    resultMessage.append("메뉴 ID: ").append(dailymenuNo).append("는 삭제되었습니다.\n");
                } else {
                    // 메뉴 존재하지 않으면 메시지 추가
                    resultMessage.append("메뉴 ID: ").append(dailymenuNo).append("는 존재하지 않습니다.\n");
                }
            }
            map.put("status", 200);
            map.put("message", resultMessage.toString());

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류: " + e.getMessage());
        }
        return map;
    }

    // 당일 판매 메뉴 수정
    // 127.0.0.1:8080/ROOT/api/menu/daily/update
    @PutMapping("/daily/update")
    public Map<String, Object> updateDailyMenuPUT(@RequestBody List<DailyMenu> updateMenus) {
        Map<String, Object> map = new HashMap<>();
        StringBuilder resultMessage = new StringBuilder();

        try {
            for (DailyMenu updatedData : updateMenus) {
                Optional<DailyMenu> optionalDailyMenu = dailyMenuRepository.findById(updatedData.getDailymenuNo());

                if (!optionalDailyMenu.isPresent()) {
                    // 존재하지 않는 메뉴 ID 처리
                    resultMessage.append("메뉴가 존재하지 않습니다. 메뉴 ID: ")
                            .append(updatedData.getDailymenuNo())
                            .append("\n");

                } else {
                    DailyMenu dailyMenu = optionalDailyMenu.get();
                    boolean updated = false; // 수정 여부를 확인하는 변수

                    // 값이 있는 필드만 수정
                    if (updatedData.getPrice() != 0 && updatedData.getPrice() != dailyMenu.getPrice()) {
                        dailyMenu.setPrice(updatedData.getPrice());
                        updated = true;
                    }

                    if (updatedData.getQty() != 0 && updatedData.getQty() != dailyMenu.getQty()) {
                        dailyMenu.setQty(updatedData.getQty());
                        updated = true;
                    }

                    // 데이터베이스 저장
                    if (updated) {
                        // 수정된 경우
                        dailyMenuRepository.save(dailyMenu);
                        resultMessage.append("메뉴가 수정되었습니다. 메뉴 ID: ")
                                .append(updatedData.getDailymenuNo())
                                .append("\n");
                    } else {
                        // 수정되지 않은 경우
                        resultMessage.append("메뉴 ID ")
                                .append(updatedData.getDailymenuNo())
                                .append("번은 수정되지 않았습니다.");
                    }
                }
            }

            map.put("status", 200);
            map.put("message", resultMessage.toString());

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류: " + e.getMessage());
        }

        return map;
    }

    // 당일 판매 메뉴 등록
    // 127.0.0.1:8080/ROOT/api/menu/daily/add
    @PostMapping("/daily/add")
    public Map<String, Object> addDailyMenuPOST(
            @RequestBody DailyMenuRequestDTO dailyMenuRequest,
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

            List<Integer> menuNos = dailyMenuRequest.getMenuNos();
            List<Integer> successfulMenuNos = new ArrayList<>(); // 성공한 메뉴 번호
            List<Integer> failedMenuNos = new ArrayList<>(); // 실패한 메뉴 번호
            for (Integer menuNo : menuNos) {
                // 메뉴 존재하는지 확인
                MenuDTO menu = menuMapper.selectMenuByNo(menuNo);
                if (menu == null) {
                    failedMenuNos.add(menuNo);
                    continue; // 메뉴가 없으면 실패 목록에 추가하고 계속 진행
                }

                // 메뉴 넘버 세팅
                DailyMenuDTO dailyMenuDTO = new DailyMenuDTO();
                dailyMenuDTO.setMenuNo(menuNo);

                // 메뉴 등록
                int result = dailyMenuMapper.insertDailyMenu(dailyMenuDTO);
                if (result > 0) {
                    successfulMenuNos.add(menuNo); // 성공한 메뉴는 성공 목록에 추가
                } else {
                    failedMenuNos.add(menuNo); // 실패한 메뉴는 실패 목록에 추가
                }
            }
            // 성공과 실패 결과를 포함한 응답 반환
            if (!successfulMenuNos.isEmpty() || !failedMenuNos.isEmpty()) {
                // 성공한 메뉴와 실패한 메뉴가 모두 있을 수 있기 때문에
                // 실패한 메뉴가 하나라도 있으면 상태 코드를 400으로 설정
                map.put("status", failedMenuNos.isEmpty() ? 200 : 400);
                map.put("message", "성공한 메뉴: " + successfulMenuNos + ", 실패한 메뉴: " + failedMenuNos);
            } else {
                map.put("status", 404);
                map.put("message", "등록된 메뉴가 없습니다.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류: " + e.getMessage());
        }
        return map;
    }

    //

    // ======가게 전체 메뉴 관리=======
    // 메뉴 추가
    @PostMapping(value = "/add.do", consumes = { "multipart/form-data" })
    public Map<String, Object> addMenu(@RequestPart("menu") String menuJson,
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

            // 메뉴 정보를 JSON 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            MenuDTO menu = objectMapper.readValue(menuJson, MenuDTO.class);
            menu.setStoreId(storeId);

            int menuResult = menuMapper.insertMenu(menu);

            if (menuResult > 0) {
                map.put("status", 200);
                map.put("message", "메뉴 추가 성공");

                if (file != null && !file.isEmpty()) {
                    MenuImageDTO menuImage = new MenuImageDTO();
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
    @PutMapping("/delete/{menuNo}")
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
            @RequestPart("menu") MenuDTO menu,
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
                    MenuImageDTO menuImage = new MenuImageDTO();
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
    // 127.0.0.1:8080/ROOT/api/menu/list
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

            System.out.println("Store ID: " + storeId);
            // 메뉴 리스트 조회
            List<com.example.entity.Menu> menuList = menuRepository.findByStoreId_StoreIdAndIsdeletedFalse(storeId);
            System.out.println("Menu List Size: " + menuList.size());

            // 각 메뉴에 이미지 URL 추가
            for (Menu menu : menuList) {
                MenuImage menuImage = menuImageRepository.findByMenu_menuNo(menu.getMenuNo());

                if (menuImage != null) {
                    menu.setImageurl("/ROOT/store/menuimage?no=" + menuImage.getMenuimageNo());
                } else {
                    menu.setImageurl(menu.getImageurl() + "0");
                }
            }

            // 메뉴 데이터를 응답에 추가
            map.put("status", 200);
            map.put("menuList", menuList);

        } catch (Exception e) {
            e.printStackTrace();
            map.put("status", -1);
            map.put("message", "서버 오류: " + e.getMessage());
        }

        return map;
    }
}
