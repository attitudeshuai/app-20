package com.instrumentroom.controller;

import com.instrumentroom.dto.billing.BillResponse;
import com.instrumentroom.dto.billing.CreateBillRequest;
import com.instrumentroom.dto.billing.UpdatePaymentStatusRequest;
import com.instrumentroom.dto.common.ApiResponse;
import com.instrumentroom.dto.common.PageResponse;
import com.instrumentroom.entity.PaymentChannel;
import com.instrumentroom.entity.PaymentStatus;
import com.instrumentroom.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
@Tag(name = "账单管理", description = "账单生成、查询、支付状态管理")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping
    @Operation(summary = "生成账单", description = "为指定预约生成账单，根据实际使用时长计算应付金额")
    public ResponseEntity<ApiResponse<BillResponse>> createBill(
            @Valid @RequestBody CreateBillRequest request) {
        BillResponse response = billingService.createBill(request);
        return ResponseEntity.ok(ApiResponse.success("账单生成成功", response));
    }

    @GetMapping
    @Operation(summary = "获取账单列表", description = "分页获取账单列表，支持按用户和支付状态筛选（管理员可看全部）")
    public ResponseEntity<ApiResponse<PageResponse<BillResponse>>> getBills(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        PageResponse<BillResponse> response = billingService.getBills(
                userId, paymentStatus, page, size, sortBy, sortDir);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/mine")
    @Operation(summary = "获取我的账单", description = "获取当前登录用户的所有账单")
    public ResponseEntity<ApiResponse<PageResponse<BillResponse>>> getMyBills(
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResponse<BillResponse> response = billingService.getMyBills(paymentStatus, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取账单详情", description = "根据ID获取账单详细信息")
    public ResponseEntity<ApiResponse<BillResponse>> getBillById(
            @PathVariable Long id) {
        BillResponse response = billingService.getBillById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "根据预约ID获取账单", description = "根据预约ID获取对应的账单")
    public ResponseEntity<ApiResponse<BillResponse>> getBillByBookingId(
            @PathVariable Long bookingId) {
        BillResponse response = billingService.getBillByBookingId(bookingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/payment-status")
    @Operation(summary = "更新支付状态", description = "更新账单的支付状态（仅账单所属用户或管理员）")
    public ResponseEntity<ApiResponse<BillResponse>> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentStatusRequest request) {
        BillResponse response = billingService.updatePaymentStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success("支付状态更新成功", response));
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "发起支付", description = "发起支付请求，对接支付网关")
    public ResponseEntity<ApiResponse<BillResponse>> initiatePayment(
            @PathVariable Long id,
            @RequestParam PaymentChannel paymentChannel) {
        BillResponse response = billingService.initiatePayment(id, paymentChannel);
        return ResponseEntity.ok(ApiResponse.success("支付发起成功", response));
    }

    @PostMapping("/{id}/callback")
    @Operation(summary = "支付回调处理", description = "处理支付网关回调，更新账单支付状态")
    public ResponseEntity<ApiResponse<BillResponse>> processPaymentCallback(
            @PathVariable Long id) {
        BillResponse response = billingService.processPaymentCallback(id);
        return ResponseEntity.ok(ApiResponse.success("支付回调处理成功", response));
    }
}
