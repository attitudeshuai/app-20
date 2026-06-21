package com.instrumentroom.dto.stats;

import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * 总览统计响应DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverviewStats {

    private long totalUsers;
    private long totalRooms;
    private long activeRooms;
    private long totalBookings;
    private long pendingBookings;
    private long confirmedBookings;
    private long completedBookings;
    private long totalCheckIns;
    private long totalIssues;
    private long openIssues;
    private List<Map<String, Object>> roomRanking;
}
