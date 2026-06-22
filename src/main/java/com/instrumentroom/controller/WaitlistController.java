package com.instrumentroom.controller;

import com.instrumentroom.dto.common.ApiResponse;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.dto.waitlist.CreateWaitlistRequest;
import com.instrumentroom.dto.waitlist.UpdateWaitlistPriorityRequest;
import com.instrumentroom.dto.waitlist.WaitlistResponse;
import com.instrumentroom.entity.WaitlistStatus;
import com.instrumentroom.service.waitlist.WaitlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/waitlists")
@Tag(name = "候补管理", description = "候补排队的增删改查、优先级调整、转正触发")
public class WaitlistController {

    private final WaitlistService waitlistService;

    public WaitlistController(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @PostMapping
    @Operation(summary = "提交候补申请", description = "当目标房间与时间段已满时，提交候补排队请求")
    public ResponseEntity<ApiResponse<WaitlistResponse>> createWaitlist(
            @Valid @RequestBody CreateWaitlistRequest request) {
        WaitlistResponse response = waitlistService.createWaitlist(request);
        return ResponseEntity.ok(ApiResponse.success("候补申请提交成功", response));
    }

    @GetMapping
    @Operation(summary = "获取候补列表", description = "分页获取候补列表，支持搜索和筛选（管理员可看全部）")
    public ResponseEntity<ApiResponse<PageResponse<WaitlistResponse>>> getWaitlists(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) WaitlistStatus status,
            @RequestParam(required = false) LocalDate bookingDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<WaitlistResponse> response = waitlistService.getWaitlists(
                userId, roomId, status, bookingDate, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mine")
    @Operation(summary = "获取我的候补", description = "获取当前登录用户的所有候补记录")
    public ResponseEntity<ApiResponse<PageResponse<WaitlistResponse>>> getMyWaitlists(
            @RequestParam(required = false) WaitlistStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<WaitlistResponse> response = waitlistService.getMyWaitlists(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取候补详情", description = "根据ID获取候补详细信息")
    public ResponseEntity<ApiResponse<WaitlistResponse>> getWaitlistById(
            @PathVariable Long id) {
        WaitlistResponse response = waitlistService.getWaitlistById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "取消候补", description = "取消候补申请（仅候补创建者或管理员）")
    public ResponseEntity<ApiResponse<WaitlistResponse>> cancelWaitlist(
            @PathVariable Long id) {
        WaitlistResponse response = waitlistService.cancelWaitlist(id);
        return ResponseEntity.ok(ApiResponse.success("候补已取消", response));
    }

    @PatchMapping("/{id}/priority")
    @Operation(summary = "调整候补优先级", description = "调整候补记录的优先级（仅候补创建者或管理员）")
    public ResponseEntity<ApiResponse<WaitlistResponse>> updatePriority(
            @PathVariable Long id,
            @Valid @RequestBody UpdateWaitlistPriorityRequest request) {
        WaitlistResponse response = waitlistService.updatePriority(id, request);
        return ResponseEntity.ok(ApiResponse.success("优先级更新成功", response));
    }

    @PostMapping("/trigger-promotion")
    @Operation(summary = "触发转正", description = "手动触发候补转正处理（仅管理员）")
    public ResponseEntity<ApiResponse<Integer>> triggerPromotion(
            @RequestParam Long roomId,
            @RequestParam LocalDate bookingDate,
            @RequestParam LocalTime startTime,
            @RequestParam LocalTime endTime) {
        int count = waitlistService.triggerPromotion(roomId, bookingDate, startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success("转正处理完成", count));
    }
}
