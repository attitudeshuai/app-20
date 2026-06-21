package com.instrumentroom.dto.room;

import com.instrumentroom.entity.PracticeRoom;
import com.instrumentroom.entity.RoomStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 练习室响应DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {

    private Long id;
    private String name;
    private String location;
    private Integer capacity;
    private String equipment;
    private BigDecimal hourlyPrice;
    private LocalTime openTime;
    private LocalTime closeTime;
    private RoomStatus status;
    private LocalDateTime createdAt;

    public static RoomResponse fromEntity(PracticeRoom room) {
        return RoomResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .location(room.getLocation())
                .capacity(room.getCapacity())
                .equipment(room.getEquipment())
                .hourlyPrice(room.getHourlyPrice())
                .openTime(room.getOpenTime())
                .closeTime(room.getCloseTime())
                .status(room.getStatus())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
