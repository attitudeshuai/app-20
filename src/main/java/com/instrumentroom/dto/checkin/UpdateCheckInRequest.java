package com.instrumentroom.dto.checkin;

import lombok.*;

/**
 * 更新签到请求DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCheckInRequest {

    private Boolean checkOut;

    private String note;
}
