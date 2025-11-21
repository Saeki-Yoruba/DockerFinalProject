package com.supernovapos.finalproject.order.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.supernovapos.finalproject.common.model.ApiResponse;
import com.supernovapos.finalproject.order.dto.TempUserResponseDto;
import com.supernovapos.finalproject.order.model.TempUser;
import com.supernovapos.finalproject.order.service.TempUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 臨時用戶管理 API - 處理匿名用戶的建立與管理
 * 練習重點：
 * 1. 理解如何建立臨時用戶身份
 * 2. 學習在同一訂單組內暱稱不重複的邏輯
 * 3. 掌握基本的CRUD操作
 */
@RestController
@RequestMapping("/api/temp-user")
@Tag(name = "臨時用戶管理", description = "匿名用戶的建立、暱稱管理與查詢功能")
public class TempUserController {

    @Autowired
    private TempUserService tempUserService;

    /**
     * 建立臨時用戶請求 DTO
     */
    public static class CreateTempUserRequest {
        private String nickname;
        
        public String getNickname() {
            return nickname;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    /**
     * 更新暱稱請求 DTO
     */
    public static class UpdateNicknameRequest {
        private String nickname;
        
        public String getNickname() {
            return nickname;
        }
        
        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }

    // ===== 核心功能：建立臨時用戶身份 =====

    /**
     * 在指定訂單組中建立新的臨時用戶
     * 練習要點：這是客戶端掃QR Code後的第一步操作
     */
    @Operation(summary = "建立臨時用戶", description = "在指定的訂單組中建立新的臨時用戶，用於匿名點餐")
    @PostMapping("/{orderGroupId}")
    public ResponseEntity<ApiResponse<TempUserResponseDto>> createTempUser(
            @Parameter(description = "訂單組ID") @PathVariable UUID orderGroupId,
            @RequestBody CreateTempUserRequest request) {

        TempUser tempUser = tempUserService.createTempUserForOrderGroup(request.getNickname(), orderGroupId);
        TempUserResponseDto responseDto = TempUserResponseDto.from(tempUser);

        ApiResponse<TempUserResponseDto> response = new ApiResponse<>(
            true,
            "臨時用戶建立成功",
            responseDto
        );

        return ResponseEntity.ok(response);
    }

    // ===== 暱稱管理功能 =====

    /**
     * 檢查暱稱在訂單組內是否可用
     * 練習要點：前端輸入暱稱時的即時驗證
     */
    @Operation(summary = "檢查暱稱可用性", description = "檢查指定暱稱在訂單組內是否可用")
    @GetMapping("/{orderGroupId}/check-nickname/{nickname}")
    public ResponseEntity<Map<String, Object>> checkNicknameAvailability(
            @Parameter(description = "訂單組ID") @PathVariable UUID orderGroupId,
            @Parameter(description = "要檢查的暱稱") @PathVariable String nickname) {

        boolean isAvailable = tempUserService.isNicknameAvailableInOrderGroup(nickname, orderGroupId);

        Map<String, Object> response = new HashMap<>();
        response.put("available", isAvailable);
        response.put("nickname", nickname);
        
        if (isAvailable) {
            response.put("message", "暱稱可使用");
        } else {
            response.put("message", "暱稱已被使用，請選擇其他暱稱");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 更新臨時用戶的暱稱 - 修正版
     */
    @Operation(summary = "更新用戶暱稱", description = "更新臨時用戶的顯示暱稱")
    @PutMapping("/{tempUserId}/nickname")
    public ResponseEntity<Map<String, Object>> updateNickname(
            @Parameter(description = "臨時用戶ID") @PathVariable UUID tempUserId,
            @RequestBody UpdateNicknameRequest request) {

        TempUser updatedUser = tempUserService.updateNickname(request.getNickname(), tempUserId);
        TempUserResponseDto responseDto = TempUserResponseDto.from(updatedUser); // 使用 DTO 避免序列化問題

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "暱稱更新成功");
        response.put("tempUser", responseDto); // 回傳 DTO 而不是實體

        return ResponseEntity.ok(response);
    }

    // ===== 查詢功能 =====

    /**
     * 取得訂單組內的所有臨時用戶列表
     * 練習要點：顯示同桌點餐的所有匿名用戶
     */
    @Operation(summary = "取得訂單群組臨時用戶列表", description = "取得指定訂單組內的所有臨時用戶")
    @GetMapping("/{orderGroupId}/list")
    public ResponseEntity<Map<String, Object>> getTempUsersByOrderGroup(
            @Parameter(description = "訂單組ID") @PathVariable UUID orderGroupId) {

        List<TempUser> tempUsers = tempUserService.getTempUsersByOrderGroup(orderGroupId);

        List<TempUserResponseDto> tempUserDtos = new ArrayList<>();
        for (TempUser tempUser : tempUsers) {
            tempUserDtos.add(TempUserResponseDto.from(tempUser));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tempUsers", tempUserDtos); // 改為使用 DTO 列表
        response.put("totalCount", tempUserDtos.size());

        return ResponseEntity.ok(response);
    }

    /**
     * 根據ID查詢特定臨時用戶 - 修正版
     */
    @Operation(summary = "查詢臨時用戶", description = "根據用戶ID查詢臨時用戶詳細資訊")
    @GetMapping("/details/{tempUserId}")
    public ResponseEntity<Map<String, Object>> getTempUserById(
            @Parameter(description = "臨時用戶ID") @PathVariable UUID tempUserId) {

        TempUser tempUser = tempUserService.findById(tempUserId);
        TempUserResponseDto responseDto = TempUserResponseDto.from(tempUser); // 使用 DTO

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("tempUser", responseDto); // 回傳 DTO

        return ResponseEntity.ok(response);
    }


    // ===== 安全驗證功能 =====

    /**
     * 驗證臨時用戶是否屬於指定訂單組
     * 練習要點：安全性驗證，防止跨桌操作
     */
    @Operation(summary = "驗證用戶歸屬", description = "驗證臨時用戶是否屬於指定的訂單組")
    @GetMapping("/{tempUserId}/validate/{orderGroupId}")
    public ResponseEntity<Map<String, Object>> validateTempUserInOrderGroup(
            @Parameter(description = "臨時用戶ID") @PathVariable UUID tempUserId,
            @Parameter(description = "訂單組ID") @PathVariable UUID orderGroupId) {

        boolean isValid = tempUserService.validateTempUserInOrderGroup(tempUserId, orderGroupId);

        Map<String, Object> response = new HashMap<>();
        response.put("valid", isValid);
        response.put("message", isValid ? "用戶驗證通過" : "用戶不屬於此訂單組");

        return ResponseEntity.ok(response);
    }

    // ===== 用戶管理功能 =====

    /**
     * 刪除臨時用戶
     * 練習要點：用戶離開點餐時可以刪除自己的臨時身份
     */
    @Operation(summary = "刪除臨時用戶", description = "刪除指定的臨時用戶")
    @DeleteMapping("/{tempUserId}")
    public ResponseEntity<Map<String, String>> deleteTempUser(
            @Parameter(description = "臨時用戶ID") @PathVariable UUID tempUserId) {

        tempUserService.deleteTempUser(tempUserId);

        Map<String, String> response = new HashMap<>();
        response.put("success", "true");
        response.put("message", "臨時用戶已刪除");

        return ResponseEntity.ok(response);
    }

    // ===== 管理端功能（簡化版）=====

    /**
     * 清理指定訂單組的所有臨時用戶
     * 練習要點：管理端結束點餐會話時使用，一次清除所有臨時用戶
     */
    @Operation(summary = "清理訂單組臨時用戶", description = "清理指定訂單組的所有臨時用戶（管理端使用）")
    @DeleteMapping("/cleanup/{orderGroupId}")
    public ResponseEntity<Map<String, Object>> cleanupTempUsersByOrderGroup(
            @Parameter(description = "訂單組ID") @PathVariable UUID orderGroupId) {

        int deletedCount = tempUserService.cleanupTempUsersByOrderGroup(orderGroupId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "臨時用戶清理完成");
        response.put("deletedCount", deletedCount);

        return ResponseEntity.ok(response);
    }
}