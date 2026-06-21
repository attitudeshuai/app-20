package com.instrumentroom.dto.checkin;

import com.instrumentroom.dto.booking.BookingResponse;
import com.instrumentroom.entity.CheckIn;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 签到响应DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckInResponse {

    private Long id;
    private Long bookingId;
    private BookingResponse booking;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private String note;

    public static CheckInResponse fromEntity(CheckIn checkIn, boolean includeDetails) {
        CheckInResponseBuilder builder = CheckInResponse.builder()
                .id(checkIn.getId())
                .bookingId(checkIn.getBooking().getId())
                .checkInAt(checkIn.getCheckInAt())
                .checkOutAt(checkIn.getCheckOutAt())
                .note(checkIn.getNote());

        if (includeDetails) {
            builder.booking(BookingResponse.fromEntity(checkIn.getBooking(), true));
        }

        return builder.build();
    }
}
