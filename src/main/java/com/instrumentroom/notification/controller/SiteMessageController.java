package com.instrumentroom.notification.controller;

import com.instrumentroom.dto.common.ApiResponse;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.notification.dto.SiteMessageResponse;
import com.instrumentroom.notification.service.SiteMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "站内信管理", description = "站内信查询、标记已读")
public class SiteMessageController {

    private final SiteMessageService siteMessageService;

    public SiteMessageController(SiteMessageService siteMessageService) {
        this.siteMessageService = siteMessageService;
    }

    @GetMapping
    @Operation(summary = "获取我的站内信", description = "分页获取当前用户的站内信列表")
    public ResponseEntity<ApiResponse<PageResponse<SiteMessageResponse>>> getMyMessages(
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<SiteMessageResponse> response = siteMessageService.getMyMessages(isRead, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "获取未读消息数", description = "获取当前用户的未读消息数")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUnreadCount() {
        Map<String, Object> response = siteMessageService.getUnreadCount();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "标记单条已读", description = "将指定消息标记为已读")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long id) {
        siteMessageService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success("标记成功", null));
    }

    @PatchMapping("/read-all")
    @Operation(summary = "全部标记已读", description = "将所有未读消息标记为已读")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        siteMessageService.markAllAsRead();
        return ResponseEntity.ok(ApiResponse.success("全部标记成功", null));
    }
}
