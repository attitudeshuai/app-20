package com.instrumentroom.dto.booking;

import com.instrumentroom.entity.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 更新预约状态请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingStatusRequest {

    @NotNull(message = "状态不能为空")
    private BookingStatus status;
}
