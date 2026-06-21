package com.instrumentroom.dto.room;

import com.instrumentroom.entity.RoomStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 创建练习室请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {

    @NotBlank(message = "练习室名称不能为空")
    @Size(max = 100, message = "练习室名称不能超过100个字符")
    private String name;

    @NotBlank(message = "练习室位置不能为空")
    @Size(max = 200, message = "练习室位置不能超过200个字符")
    private String location;

    @NotNull(message = "容纳人数不能为空")
    @Min(value = 1, message = "容纳人数至少为1")
    private Integer capacity;

    private String equipment;

    @NotNull(message = "每小时价格不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "价格必须大于0")
    private BigDecimal hourlyPrice;

    @NotNull(message = "开放时间不能为空")
    private LocalTime openTime;

    @NotNull(message = "关闭时间不能为空")
    private LocalTime closeTime;

    private RoomStatus status = RoomStatus.OPEN;
}
