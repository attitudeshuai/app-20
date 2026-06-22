package com.instrumentroom.dto.waitlist;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateWaitlistPriorityRequest {

    @NotNull(message = "优先级不能为空")
    @Min(value = 0, message = "优先级最小为0")
    @Max(value = 100, message = "优先级最大为100")
    private Integer priority;
}
