package com.instrumentroom.payment;

import com.instrumentroom.entity.PaymentChannel;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {

    private String orderId;
    private BigDecimal amount;
    private String subject;
    private String description;
    private PaymentChannel paymentChannel;
    private String returnUrl;
    private String notifyUrl;
}
