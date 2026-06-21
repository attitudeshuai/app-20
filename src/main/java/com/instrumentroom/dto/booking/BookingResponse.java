package com.instrumentroom.dto.booking;

import com.instrumentroom.dto.auth.UserResponse;
import com.instrumentroom.dto.room.RoomResponse;
import com.instrumentroom.entity.Booking;
import com.instrumentroom.entity.BookingStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约响应DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingResponse {

    private Long id;
    private Long roomId;
    private RoomResponse room;
    private Long userId;
    private UserResponse user;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private BookingStatus status;
    private LocalDateTime createdAt;

    public static BookingResponse fromEntity(Booking booking, boolean includeDetails) {
        BookingResponseBuilder builder = BookingResponse.builder()
                .id(booking.getId())
                .roomId(booking.getRoom().getId())
                .userId(booking.getUser().getId())
                .bookingDate(booking.getBookingDate())
                .startTime(booking.getStartTime())
                .endTime(booking.getEndTime())
                .purpose(booking.getPurpose())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt());

        if (includeDetails) {
            builder.room(RoomResponse.fromEntity(booking.getRoom()));
            builder.user(UserResponse.fromEntity(booking.getUser()));
        }

        return builder.build();
    }
}
