package com.instrumentroom.payment;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    private String orderId;
    private String transactionId;
    private BigDecimal refundAmount;
    private String refundReason;
}
