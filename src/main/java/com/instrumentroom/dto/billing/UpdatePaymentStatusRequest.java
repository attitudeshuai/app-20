package com.instrumentroom.dto.billing;

import com.instrumentroom.entity.PaymentChannel;
import com.instrumentroom.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePaymentStatusRequest {

    @NotNull(message = "支付状态不能为空")
    private PaymentStatus paymentStatus;

    private PaymentChannel paymentChannel;

    private BigDecimal paidAmount;

    private String transactionId;

    private LocalDateTime paymentTime;

    private String remark;
}
