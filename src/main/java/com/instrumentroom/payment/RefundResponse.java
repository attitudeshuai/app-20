package com.instrumentroom.payment;

import com.instrumentroom.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {

    private boolean success;
    private String orderId;
    private String transactionId;
    private String refundId;
    private PaymentStatus paymentStatus;
    private BigDecimal refundAmount;
    private LocalDateTime refundTime;
    private String errorCode;
    private String errorMessage;
}
