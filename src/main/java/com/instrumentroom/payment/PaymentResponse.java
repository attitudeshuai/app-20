package com.instrumentroom.payment;

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
public class PaymentResponse {

    private boolean success;
    private String orderId;
    private String transactionId;
    private PaymentStatus paymentStatus;
    private PaymentChannel paymentChannel;
    private BigDecimal paidAmount;
    private LocalDateTime paymentTime;
    private String paymentUrl;
    private String errorCode;
    private String errorMessage;
}
