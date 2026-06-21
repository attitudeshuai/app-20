package com.instrumentroom.controller;

import com.instrumentroom.dto.checkin.CheckInResponse;
import com.instrumentroom.dto.checkin.CreateCheckInRequest;
import com.instrumentroom.dto.checkin.UpdateCheckInRequest;
import com.instrumentroom.dto.common.ApiResponse;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.service.CheckInService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 签到管理控制器
 */
@RestController
@RequestMapping("/api/checkins")
@Tag(name = "签到管理", description = "签到的增删改查")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    /**
     * 创建签到
     */
    @PostMapping
    @Operation(summary = "创建签到", description = "根据预约ID创建签到记录")
    public ResponseEntity<ApiResponse<CheckInResponse>> createCheckIn(
            @Valid @RequestBody CreateCheckInRequest request) {
        CheckInResponse response = checkInService.createCheckIn(request);
        return ResponseEntity.ok(ApiResponse.success("签到成功", response));
    }

    /**
     * 获取签到列表
     */
    @GetMapping
    @Operation(summary = "获取签到列表", description = "分页获取签到列表，支持搜索和筛选")
    public ResponseEntity<ApiResponse<PageResponse<CheckInResponse>>> getCheckIns(
            @RequestParam(required = false) Long bookingId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "checkInAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<CheckInResponse> response = checkInService.getCheckIns(
                bookingId, roomId, userId, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取签到详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取签到详情", description = "根据ID获取签到详细信息")
    public ResponseEntity<ApiResponse<CheckInResponse>> getCheckInById(
            @PathVariable Long id) {
        CheckInResponse response = checkInService.getCheckInById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新签到（签出）
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新签到", description = "更新签到信息，可以执行签出操作")
    public ResponseEntity<ApiResponse<CheckInResponse>> updateCheckIn(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCheckInRequest request) {
        CheckInResponse response = checkInService.updateCheckIn(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 删除签到
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除签到", description = "删除签到记录")
    public ResponseEntity<ApiResponse<Void>> deleteCheckIn(
            @PathVariable Long id) {
        checkInService.deleteCheckIn(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
