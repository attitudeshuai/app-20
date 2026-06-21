package com.instrumentroom.dto.booking;

import com.instrumentroom.entity.BookingStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 更新预约请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateBookingRequest {

    private Long roomId;

    private LocalDate bookingDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Size(max = 500, message = "用途描述不能超过500个字符")
    private String purpose;

    private BookingStatus status;
}
