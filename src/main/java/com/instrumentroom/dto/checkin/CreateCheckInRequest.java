package com.instrumentroom.dto.checkin;

import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 创建签到请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCheckInRequest {

    @NotNull(message = "预约ID不能为空")
    private Long bookingId;

    private String note;
}
