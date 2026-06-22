package com.instrumentroom.dto.waitlist;

import com.instrumentroom.dto.auth.UserResponse;
import com.instrumentroom.dto.room.RoomResponse;
import com.instrumentroom.entity.Waitlist;
import com.instrumentroom.entity.WaitlistStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaitlistResponse {

    private Long id;
    private Long roomId;
    private RoomResponse room;
    private Long userId;
    private UserResponse user;
    private LocalDate bookingDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String purpose;
    private WaitlistStatus status;
    private Integer priority;
    private Integer queuePosition;
    private LocalDateTime expireAt;
    private Long confirmedBookingId;
    private String failReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WaitlistResponse fromEntity(Waitlist waitlist, boolean includeDetails) {
        WaitlistResponseBuilder builder = WaitlistResponse.builder()
                .id(waitlist.getId())
                .roomId(waitlist.getRoom().getId())
                .userId(waitlist.getUser().getId())
                .bookingDate(waitlist.getBookingDate())
                .startTime(waitlist.getStartTime())
                .endTime(waitlist.getEndTime())
                .purpose(waitlist.getPurpose())
                .status(waitlist.getStatus())
                .priority(waitlist.getPriority())
                .queuePosition(waitlist.getQueuePosition())
                .expireAt(waitlist.getExpireAt())
                .confirmedBookingId(waitlist.getConfirmedBookingId())
                .failReason(waitlist.getFailReason())
                .createdAt(waitlist.getCreatedAt())
                .updatedAt(waitlist.getUpdatedAt());

        if (includeDetails) {
            builder.room(RoomResponse.fromEntity(waitlist.getRoom()));
            builder.user(UserResponse.fromEntity(waitlist.getUser()));
        }

        return builder.build();
    }
}
