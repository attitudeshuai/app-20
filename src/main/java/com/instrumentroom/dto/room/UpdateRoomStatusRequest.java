package com.instrumentroom.dto.room;

import com.instrumentroom.entity.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * 更新练习室状态请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoomStatusRequest {

    @NotNull(message = "状态不能为空")
    private RoomStatus status;
}
