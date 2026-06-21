package com.instrumentroom.controller;

import com.instrumentroom.dto.common.ApiResponse;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.dto.room.*;
import com.instrumentroom.entity.RoomStatus;
import com.instrumentroom.service.PracticeRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 练习室管理控制器
 */
@RestController
@RequestMapping("/api/practicerooms")
@Tag(name = "练习室管理", description = "练习室的增删改查、状态管理")
public class PracticeRoomController {

    private final PracticeRoomService roomService;

    public PracticeRoomController(PracticeRoomService roomService) {
        this.roomService = roomService;
    }

    /**
     * 创建练习室
     */
    @PostMapping
    @Operation(summary = "创建练习室", description = "创建新的练习室（仅管理员）")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {
        RoomResponse response = roomService.createRoom(request);
        return ResponseEntity.ok(ApiResponse.success("创建成功", response));
    }

    /**
     * 获取练习室列表
     */
    @GetMapping
    @Operation(summary = "获取练习室列表", description = "分页获取练习室列表，支持搜索和筛选")
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> getRooms(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<RoomResponse> response = roomService.getRooms(
                name, location, status, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取练习室详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取练习室详情", description = "根据ID获取练习室详细信息")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoomById(
            @PathVariable Long id) {
        RoomResponse response = roomService.getRoomById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新练习室
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新练习室", description = "更新练习室信息（仅管理员）")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomRequest request) {
        RoomResponse response = roomService.updateRoom(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 修改练习室状态
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "修改练习室状态", description = "修改练习室开放/关闭/维护状态（仅管理员）")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoomStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoomStatusRequest request) {
        RoomResponse response = roomService.updateRoomStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", response));
    }

    /**
     * 删除练习室
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除练习室", description = "删除练习室（仅管理员）")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(
            @PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
