package com.instrumentroom.controller;

import com.instrumentroom.dto.booking.*;
import com.instrumentroom.dto.common.ApiResponse;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.entity.BookingStatus;
import com.instrumentroom.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 预约管理控制器
 */
@RestController
@RequestMapping("/api/bookings")
@Tag(name = "预约管理", description = "预约的增删改查、状态管理")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * 创建预约
     */
    @PostMapping
    @Operation(summary = "创建预约", description = "创建新的练习室预约")
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.success("预约成功", response));
    }

    /**
     * 获取预约列表
     */
    @GetMapping
    @Operation(summary = "获取预约列表", description = "分页获取预约列表，支持搜索和筛选（管理员可看全部）")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) LocalDate bookingDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<BookingResponse> response = bookingService.getBookings(
                userId, roomId, status, bookingDate, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取我的预约
     */
    @GetMapping("/mine")
    @Operation(summary = "获取我的预约", description = "获取当前登录用户的所有预约")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getMyBookings(
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<BookingResponse> response = bookingService.getMyBookings(status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取预约详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取预约详情", description = "根据ID获取预约详细信息")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(
            @PathVariable Long id) {
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 更新预约
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新预约", description = "更新预约信息（仅预约创建者或管理员）")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBooking(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingRequest request) {
        BookingResponse response = bookingService.updateBooking(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功", response));
    }

    /**
     * 修改预约状态
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "修改预约状态", description = "修改预约状态（仅预约创建者或管理员）")
    public ResponseEntity<ApiResponse<BookingResponse>> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        BookingResponse response = bookingService.updateBookingStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("状态更新成功", response));
    }

    /**
     * 删除预约
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除预约", description = "删除预约（仅预约创建者或管理员）")
    public ResponseEntity<ApiResponse<Void>> deleteBooking(
            @PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
