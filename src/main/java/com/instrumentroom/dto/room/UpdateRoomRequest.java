package com.instrumentroom.dto.room;

import com.instrumentroom.entity.RoomStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 更新练习室请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateRoomRequest {

    @Size(max = 100, message = "练习室名称不能超过100个字符")
    private String name;

    @Size(max = 200, message = "练习室位置不能超过200个字符")
    private String location;

    @Min(value = 1, message = "容纳人数至少为1")
    private Integer capacity;

    private String equipment;

    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    private BigDecimal hourlyPrice;

    private LocalTime openTime;

    private LocalTime closeTime;

    private RoomStatus status;
}
