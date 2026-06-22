package com.instrumentroom.service.waitlist;

import com.instrumentroom.entity.*;
import com.instrumentroom.exception.BusinessException;
import com.instrumentroom.repository.BookingRepository;
import com.instrumentroom.repository.WaitlistRepository;
import com.instrumentroom.service.BookingService;
import com.instrumentroom.service.PracticeRoomService;
import com.instrumentroom.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class WaitlistPromotionService {

    private static final Logger logger = LoggerFactory.getLogger(WaitlistPromotionService.class);

    private final WaitlistRepository waitlistRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistStateMachine stateMachine;
    private final WaitlistRuleService ruleService;
    private final PracticeRoomService roomService;
    private final NotificationService notificationService;

    private final AtomicBoolean promotionRunning = new AtomicBoolean(false);

    public WaitlistPromotionService(WaitlistRepository waitlistRepository,
                                    BookingRepository bookingRepository,
                                    WaitlistStateMachine stateMachine,
                                    WaitlistRuleService ruleService,
                                    PracticeRoomService roomService,
                                    NotificationService notificationService) {
        this.waitlistRepository = waitlistRepository;
        this.bookingRepository = bookingRepository;
        this.stateMachine = stateMachine;
        this.ruleService = ruleService;
        this.roomService = roomService;
        this.notificationService = notificationService;
    }

    @Transactional
    public int promoteWaitlistsForSlot(Long roomId, LocalDate bookingDate,
                                       LocalTime startTime, LocalTime endTime) {
        if (!promotionRunning.compareAndSet(false, true)) {
            logger.warn("转正调度正在运行中，跳过本次触发");
            return 0;
        }

        try {
            return doPromoteWaitlists(roomId, bookingDate, startTime, endTime);
        } finally {
            promotionRunning.set(false);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private int doPromoteWaitlists(Long roomId, LocalDate bookingDate,
                                   LocalTime startTime, LocalTime endTime) {
        int promotedCount = 0;
        int retryCount = 3;

        for (int attempt = 0; attempt < retryCount; attempt++) {
            try {
                List<Waitlist> candidates = waitlistRepository.findEligibleWaitlistsWithLock(
                        roomId, bookingDate, startTime, endTime, WaitlistStatus.WAITING);

                if (candidates.isEmpty()) {
                    logger.debug("没有符合条件的候补记录，roomId={}, date={}, time={}-{}",
                            roomId, bookingDate, startTime, endTime);
                    break;
                }

                logger.info("开始转正处理，候补数量: {}, roomId={}, attempt={}",
                        candidates.size(), roomId, attempt + 1);

                for (Waitlist candidate : candidates) {
                    if (!hasAvailableSlot(roomId, bookingDate, startTime, endTime)) {
                        logger.debug("时段已无空位，停止转正处理");
                        break;
                    }

                    boolean success = tryPromoteSingleWaitlist(candidate);
                    if (success) {
                        promotedCount++;
                    }
                }

                break;

            } catch (Exception e) {
                logger.error("转正处理异常，attempt={}, roomId={}", attempt + 1, roomId, e);
                if (attempt == retryCount - 1) {
                    throw new BusinessException("转正处理失败，请稍后重试");
                }
            }
        }

        if (promotedCount > 0) {
            logger.info("转正处理完成，成功转正 {} 个候补", promotedCount);
        }

        return promotedCount;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean tryPromoteSingleWaitlist(Waitlist waitlist) {
        try {
            if (waitlist.getStatus() != WaitlistStatus.WAITING) {
                logger.debug("候补状态不是WAITING，跳过: id={}, status={}",
                        waitlist.getId(), waitlist.getStatus());
                return false;
            }

            PracticeRoom room = waitlist.getRoom();
            if (room.getStatus() != RoomStatus.OPEN) {
                logger.warn("练习室状态异常，跳过转正: roomId={}, status={}", room.getId(), room.getStatus());
                markWaitlistFailed(waitlist, "练习室当前不可用");
                return false;
            }

            if (!hasAvailableSlot(room.getId(), waitlist.getBookingDate(),
                    waitlist.getStartTime(), waitlist.getEndTime())) {
                logger.debug("时段已无空位，跳过转正: waitlistId={}", waitlist.getId());
                return false;
            }

            stateMachine.transition(waitlist, WaitlistStatus.PROCESSING);

            Booking booking = createBookingFromWaitlist(waitlist);

            waitlist.setConfirmedBookingId(booking.getId());
            stateMachine.transition(waitlist.getId(), WaitlistStatus.CONFIRMED);

            notificationService.notifyWaitlistPromoted(waitlist, booking);

            logger.info("候补转正成功: waitlistId={}, bookingId={}",
                    waitlist.getId(), booking.getId());

            return true;

        } catch (Exception e) {
            logger.error("候补转正失败: waitlistId={}", waitlist.getId(), e);
            try {
                markWaitlistFailed(waitlist, "转正失败: " + e.getMessage());
            } catch (Exception ex) {
                logger.error("标记候补失败状态出错: waitlistId={}", waitlist.getId(), ex);
            }
            return false;
        }
    }

    private boolean hasAvailableSlot(Long roomId, LocalDate bookingDate,
                                     LocalTime startTime, LocalTime endTime) {
        List<Booking> conflicts = bookingRepository.findConflictingBookings(
                roomId, bookingDate, startTime, endTime);
        return conflicts.isEmpty();
    }

    private Booking createBookingFromWaitlist(Waitlist waitlist) {
        Booking booking = Booking.builder()
                .room(waitlist.getRoom())
                .user(waitlist.getUser())
                .bookingDate(waitlist.getBookingDate())
                .startTime(waitlist.getStartTime())
                .endTime(waitlist.getEndTime())
                .purpose(waitlist.getPurpose() != null ? waitlist.getPurpose() : "候补转正")
                .status(BookingStatus.CONFIRMED)
                .build();

        return bookingRepository.save(booking);
    }

    private void markWaitlistFailed(Waitlist waitlist, String reason) {
        waitlistRepository.updateStatusAndFailReason(
                waitlist.getId(), WaitlistStatus.FAILED, reason);
    }

    @Transactional
    public int promoteAllWaitlistsForRoom(Long roomId) {
        List<Waitlist> waitlists = waitlistRepository.findEligibleWaitlistsWithDetails(
                roomId, LocalDate.now(), LocalTime.MIN, LocalTime.MAX, WaitlistStatus.WAITING);

        int totalPromoted = 0;
        for (Waitlist waitlist : waitlists) {
            boolean success = tryPromoteSingleWaitlist(waitlist);
            if (success) {
                totalPromoted++;
            }
        }
        return totalPromoted;
    }

    @Transactional(readOnly = true)
    public List<Waitlist> getEligibleWaitlists(Long roomId, LocalDate bookingDate,
                                               LocalTime startTime, LocalTime endTime) {
        return waitlistRepository.findEligibleWaitlistsWithDetails(
                roomId, bookingDate, startTime, endTime, WaitlistStatus.WAITING);
    }
}
