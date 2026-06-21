package com.instrumentroom.service;

import com.instrumentroom.dto.stats.OverviewStats;
import com.instrumentroom.dto.stats.TrendStats;
import com.instrumentroom.entity.BookingStatus;
import com.instrumentroom.entity.IssueStatus;
import com.instrumentroom.entity.RoomStatus;
import com.instrumentroom.repository.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统计服务
 */
@Service
public class StatsService {

    private final UserRepository userRepository;
    private final PracticeRoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final CheckInRepository checkInRepository;
    private final RoomIssueRepository issueRepository;

    public StatsService(
            UserRepository userRepository,
            PracticeRoomRepository roomRepository,
            BookingRepository bookingRepository,
            CheckInRepository checkInRepository,
            RoomIssueRepository issueRepository) {
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.checkInRepository = checkInRepository;
        this.issueRepository = issueRepository;
    }

    /**
     * 获取总览统计
     */
    public OverviewStats getOverviewStats() {
        // 用户统计
        long totalUsers = userRepository.count();

        // 练习室统计
        long totalRooms = roomRepository.count();
        long activeRooms = roomRepository.count();
        // 计算开放的练习室数量
        activeRooms = roomRepository.findByStatusOrderByCreatedAtDesc(RoomStatus.OPEN).size();

        // 预约统计
        long totalBookings = bookingRepository.count();
        long pendingBookings = bookingRepository.countByStatus(BookingStatus.PENDING);
        long confirmedBookings = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long completedBookings = bookingRepository.countByStatus(BookingStatus.COMPLETED);

        // 签到统计
        long totalCheckIns = checkInRepository.count();

        // 反馈统计
        long totalIssues = issueRepository.count();
        long openIssues = issueRepository.countByStatus(IssueStatus.OPEN);

        // 练习室预约排行榜
        List<Object[]> roomRankingObjects = bookingRepository.getRoomRanking(PageRequest.of(0, 10));
        List<Map<String, Object>> roomRanking = roomRankingObjects.stream()
                .map(arr -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("roomName", arr[0]);
                    map.put("bookingCount", arr[1]);
                    return map;
                })
                .collect(Collectors.toList());

        return OverviewStats.builder()
                .totalUsers(totalUsers)
                .totalRooms(totalRooms)
                .activeRooms(activeRooms)
                .totalBookings(totalBookings)
                .pendingBookings(pendingBookings)
                .confirmedBookings(confirmedBookings)
                .completedBookings(completedBookings)
                .totalCheckIns(totalCheckIns)
                .totalIssues(totalIssues)
                .openIssues(openIssues)
                .roomRanking(roomRanking)
                .build();
    }

    /**
     * 获取趋势统计（按时间范围）
     */
    public TrendStats getTrendStats(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate.isAfter(endDate)) {
            LocalDate temp = startDate;
            startDate = endDate;
            endDate = temp;
        }

        List<Object[]> trendData = bookingRepository.getBookingTrend(startDate, endDate);

        // 构建完整日期范围内的数据（填充没有数据的日期）
        Map<LocalDate, Long> dataMap = trendData.stream()
                .collect(Collectors.toMap(
                        arr -> (LocalDate) arr[0],
                        arr -> ((Number) arr[1]).longValue()
                ));

        List<TrendStats.DailyBooking> dailyBookings = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            long count = dataMap.getOrDefault(current, 0L);
            dailyBookings.add(TrendStats.DailyBooking.builder()
                    .date(current)
                    .count(count)
                    .build());
            current = current.plusDays(1);
        }

        return TrendStats.builder()
                .startDate(startDate)
                .endDate(endDate)
                .dailyBookings(dailyBookings)
                .build();
    }
}
