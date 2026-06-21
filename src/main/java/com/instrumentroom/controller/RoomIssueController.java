package com.instrumentroom.controller;

import com.instrumentroom.dto.common.ApiResponse;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.dto.issue.*;
import com.instrumentroom.entity.IssueStatus;
import com.instrumentroom.service.RoomIssueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 房间问题反馈控制器
 */
@RestController
@RequestMapping("/api/issues")
@Tag(name = "反馈管理", description = "房间问题反馈的增删改查、状态管理")
public class RoomIssueController {

    private final RoomIssueService issueService;

    public RoomIssueController(RoomIssueService issueService) {
        this.issueService = issueService;
    }

    /**
     * 创建反馈
     */
    @PostMapping
    @Operation(summary = "创建反馈", description = "提交房间问题反馈（如噪音投诉、设备损坏等）")
    public ResponseEntity<ApiResponse<IssueResponse>> createIssue(
            @Valid @RequestBody CreateIssueRequest request) {
        IssueResponse response = issueService.createIssue(request);
        return ResponseEntity.ok(ApiResponse.success("反馈提交成功", response));
    }

    /**
     * 获取反馈列表
     */
    @GetMapping
    @Operation(summary = "获取反馈列表", description = "分页获取反馈列表，支持搜索和筛选")
    public ResponseEntity<ApiResponse<PageResponse<IssueResponse>>> getIssues(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long reporterId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) String issueType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<IssueResponse> response = issueService.getIssues(
                roomId, reporterId, status, issueType, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取反馈详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取反馈详情", description = "根据ID获取反馈详细信息")
    public ResponseEntity<ApiResponse<IssueResponse>> getIssueById(
            @PathVariable Long id) {
        IssueResponse response = issueService.getIssueById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新反馈
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新反馈", description = "更新反馈信息（状态修改仅管理员）")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssue(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueRequest request) {
        IssueResponse response = issueService.updateIssue(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 修改反馈状态
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "修改反馈状态", description = "修改反馈处理状态（仅管理员）")
    public ResponseEntity<ApiResponse<IssueResponse>> updateIssueStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueStatusRequest request) {
        IssueResponse response = issueService.updateIssueStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", response));
    }

    /**
     * 删除反馈
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除反馈", description = "删除反馈记录")
    public ResponseEntity<ApiResponse<Void>> deleteIssue(
            @PathVariable Long id) {
        issueService.deleteIssue(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
