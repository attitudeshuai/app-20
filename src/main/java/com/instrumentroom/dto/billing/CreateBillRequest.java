package com.instrumentroom.dto.billing;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBillRequest {

    @NotNull(message = "预约ID不能为空")
    private Long bookingId;

    private BigDecimal discountAmount;

    private String remark;
}
