package com.instrumentroom.dto.stats;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 趋势统计响应DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendStats {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<DailyBooking> dailyBookings;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyBooking {
        private LocalDate date;
        private long count;
    }
}
