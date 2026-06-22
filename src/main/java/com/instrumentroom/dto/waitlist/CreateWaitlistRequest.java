package com.instrumentroom.dto.waitlist;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateWaitlistRequest {

    @NotNull(message = "练习室ID不能为空")
    private Long roomId;

    @NotNull(message = "预约日期不能为空")
    private LocalDate bookingDate;

    @NotNull(message = "开始时间不能为空")
    private LocalTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalTime endTime;

    @Size(max = 500, message = "用途描述不能超过500个字符")
    private String purpose;

    private Integer priority;
}
