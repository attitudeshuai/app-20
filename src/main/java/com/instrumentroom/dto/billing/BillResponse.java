package com.instrumentroom.dto.billing;

import com.instrumentroom.dto.auth.UserResponse;
import com.instrumentroom.dto.booking.BookingResponse;
import com.instrumentroom.entity.Bill;
import com.instrumentroom.entity.PaymentChannel;
import com.instrumentroom.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillResponse {

    private Long id;
    private Long bookingId;
    private BookingResponse booking;
    private Long userId;
    private UserResponse user;
    private BigDecimal hourlyPrice;
    private Integer bookedDurationMinutes;
    private Integer actualDurationMinutes;
    private BigDecimal bookedAmount;
    private BigDecimal actualAmount;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal paidAmount;
    private PaymentStatus paymentStatus;
    private PaymentChannel paymentChannel;
    private String transactionId;
    private LocalDateTime paymentTime;
    private BigDecimal refundAmount;
    private LocalDateTime refundTime;
    private String refundReason;
    private String remark;
    private String paymentUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BillResponse fromEntity(Bill bill, boolean includeDetails) {
        BillResponseBuilder builder = BillResponse.builder()
                .id(bill.getId())
                .bookingId(bill.getBooking().getId())
                .userId(bill.getUser().getId())
                .hourlyPrice(bill.getHourlyPrice())
                .bookedDurationMinutes(bill.getBookedDurationMinutes())
                .actualDurationMinutes(bill.getActualDurationMinutes())
                .bookedAmount(bill.getBookedAmount())
                .actualAmount(bill.getActualAmount())
                .totalAmount(bill.getTotalAmount())
                .discountAmount(bill.getDiscountAmount())
                .paidAmount(bill.getPaidAmount())
                .paymentStatus(bill.getPaymentStatus())
                .paymentChannel(bill.getPaymentChannel())
                .transactionId(bill.getTransactionId())
                .paymentTime(bill.getPaymentTime())
                .refundAmount(bill.getRefundAmount())
                .refundTime(bill.getRefundTime())
                .refundReason(bill.getRefundReason())
                .remark(bill.getRemark())
                .createdAt(bill.getCreatedAt())
                .updatedAt(bill.getUpdatedAt());

        if (includeDetails) {
            builder.booking(BookingResponse.fromEntity(bill.getBooking(), true));
            builder.user(UserResponse.fromEntity(bill.getUser()));
        }

        return builder.build();
    }
}
