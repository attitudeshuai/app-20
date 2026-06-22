package com.instrumentroom.service.waitlist;

import com.instrumentroom.dto.waitlist.CreateWaitlistRequest;
import com.instrumentroom.entity.*;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.repository.BookingRepository;
import com.instrumentroom.repository.WaitlistRepository;
import com.instrumentroom.service.AuthService;
import com.instrumentroom.service.PracticeRoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
public class WaitlistRuleService {

    private static final Logger logger = LoggerFactory.getLogger(WaitlistRuleService.class);

    private final WaitlistRepository waitlistRepository;
    private final BookingRepository bookingRepository;
    private final PracticeRoomService roomService;
    private final AuthService authService;

    @Value("${waitlist.default-priority:10}")
    private int defaultPriority;

    @Value("${waitlist.max-priority:100}")
    private int maxPriority;

    @Value("${waitlist.min-priority:0}")
    private int minPriority;

    @Value("${waitlist.max-per-user-per-room:3}")
    private int maxPerUserPerRoom;

    @Value("${waitlist.expire-hours:24}")
    private int expireHours;

    public WaitlistRuleService(WaitlistRepository waitlistRepository,
                               BookingRepository bookingRepository,
                               PracticeRoomService roomService,
                               AuthService authService) {
        this.waitlistRepository = waitlistRepository;
        this.bookingRepository = bookingRepository;
        this.roomService = roomService;
        this.authService = authService;
    }

    @Transactional(readOnly = true)
    public void validateWaitlistSubmission(CreateWaitlistRequest request) {
        PracticeRoom room = roomService.getRoomEntityById(request.getRoomId());

        if (room.getStatus() != RoomStatus.OPEN) {
            throw new BusinessException("练习室当前不可预约");
        }

        LocalDate bookingDate = request.getBookingDate();
        LocalTime startTime = request.getStartTime();
        LocalTime endTime = request.getEndTime();

        if (bookingDate.isBefore(LocalDate.now())) {
            throw new BusinessException("预约日期不能早于今天");
        }
        if (!startTime.isBefore(endTime)) {
            throw new BusinessException("开始时间必须早于结束时间");
        }
        if (startTime.isBefore(room.getOpenTime()) || endTime.isAfter(room.getCloseTime())) {
            throw new BusinessException("预约时间超出练习室开放时间范围");
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                room.getId(), bookingDate, startTime, endTime);
        if (conflicts.isEmpty()) {
            throw new BusinessException("该时段仍有空闲，请直接预约");
        }

        User currentUser = authService.getCurrentUserEntity();
        List<WaitlistStatus> activeStatuses = Arrays.asList(
                WaitlistStatus.WAITING, WaitlistStatus.PROCESSING);
        long userWaitlistCount = waitlistRepository.countUserWaitlistInTimeRange(
                currentUser.getId(), room.getId(), bookingDate, startTime, endTime, activeStatuses);
        if (userWaitlistCount >= maxPerUserPerRoom) {
            throw new BusinessException(String.format(
                    "您在该时段已有%d个候补请求，达到上限", maxPerUserPerRoom));
        }
    }

    public int calculatePriority(Integer requestedPriority) {
        if (requestedPriority == null) {
            return defaultPriority;
        }
        if (requestedPriority < minPriority) {
            return minPriority;
        }
        if (requestedPriority > maxPriority) {
            return maxPriority;
        }
        return requestedPriority;
    }

    public LocalDateTime calculateExpireTime(LocalDate bookingDate, LocalTime startTime) {
        LocalDateTime bookingStartDateTime = LocalDateTime.of(bookingDate, startTime);
        LocalDateTime expireFromNow = LocalDateTime.now().plusHours(expireHours);
        return bookingStartDateTime.isBefore(expireFromNow) ? bookingStartDateTime : expireFromNow;
    }

    @Transactional(readOnly = true)
    public int calculateQueuePosition(Long roomId, LocalDate bookingDate,
                                      LocalTime startTime, LocalTime endTime,
                                      int priority, LocalDateTime createdAt) {
        List<Waitlist> waitlists = waitlistRepository.findEligibleWaitlistsWithDetails(
                roomId, bookingDate, startTime, endTime, WaitlistStatus.WAITING);

        int position = 1;
        for (Waitlist w : waitlists) {
            if (w.getPriority() > priority ||
                    (w.getPriority().equals(priority) && w.getCreatedAt().isBefore(createdAt))) {
                position++;
            }
        }
        return position;
    }

    @Transactional(readOnly = true)
    public void validatePriorityChange(Waitlist waitlist, int newPriority) {
        if (newPriority < minPriority || newPriority > maxPriority) {
            throw new BusinessException(String.format(
                    "优先级范围应在 %d 到 %d 之间", minPriority, maxPriority));
        }

        if (waitlist.getStatus() != WaitlistStatus.WAITING) {
            throw new BusinessException("仅等待中的候补可调整优先级");
        }
    }

    public boolean isSlotAvailable(Long roomId, LocalDate bookingDate,
                                   LocalTime startTime, LocalTime endTime) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                roomId, bookingDate, startTime, endTime);
        return conflicts.isEmpty();
    }

    public int getDefaultPriority() {
        return defaultPriority;
    }

    public int getMaxPriority() {
        return maxPriority;
    }

    public int getMinPriority() {
        return minPriority;
    }
}
