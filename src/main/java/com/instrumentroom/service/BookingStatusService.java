package com.instrumentroom.service;

import com.instrumentroom.entity.Booking;
import com.instrumentroom.entity.BookingStatus;
import com.instrumentroom.entity.CheckIn;
import com.instrumentroom.notification.NotificationService;
import com.instrumentroom.repository.BookingRepository;
import com.instrumentroom.repository.CheckInRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class BookingStatusService {

    private static final Logger logger = LoggerFactory.getLogger(BookingStatusService.class);

    private static final long AUTO_CANCEL_GRACE_MINUTES = 30;

    private final BookingRepository bookingRepository;
    private final CheckInRepository checkInRepository;
    private final NotificationService notificationService;

    public BookingStatusService(
            BookingRepository bookingRepository,
            CheckInRepository checkInRepository,
            NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.checkInRepository = checkInRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public int autoCancelExpiredBookings() {
        logger.info("开始执行自动取消超时预约任务");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.minusMinutes(AUTO_CANCEL_GRACE_MINUTES);
        LocalDate deadlineDate = deadline.toLocalDate();
        LocalTime deadlineTime = deadline.toLocalTime();

        List<BookingStatus> targetStatuses = Arrays.asList(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );

        List<Booking> expiredBookings = bookingRepository.findBookingsExpiredBefore(
                targetStatuses, deadlineDate, deadlineTime);

        if (expiredBookings.isEmpty()) {
            logger.info("没有找到需要自动取消的预约");
            return 0;
        }

        logger.info("找到 {} 个可能需要自动取消的预约，开始逐个处理", expiredBookings.size());

        int cancelledCount = 0;
        List<Long> failedIds = new ArrayList<>();

        for (Booking booking : expiredBookings) {
            try {
                if (processAutoCancel(booking)) {
                    cancelledCount++;
                }
            } catch (Exception e) {
                logger.error("自动取消预约失败，预约ID: {}", booking.getId(), e);
                failedIds.add(booking.getId());
            }
        }

        logger.info("自动取消任务执行完成，成功取消: {} 个，失败: {} 个", 
                cancelledCount, failedIds.size());

        if (!failedIds.isEmpty()) {
            logger.warn("自动取消失败的预约ID列表: {}", failedIds);
        }

        return cancelledCount;
    }

    private boolean processAutoCancel(Booking booking) {
        BookingStatus currentStatus = booking.getStatus();
        
        if (currentStatus == BookingStatus.CANCELLED || currentStatus == BookingStatus.COMPLETED) {
            logger.debug("预约 {} 状态为 {}，无需自动取消", booking.getId(), currentStatus);
            return false;
        }

        Optional<CheckIn> checkInOpt = checkInRepository.findByBookingId(booking.getId());
        if (checkInOpt.isPresent() && checkInOpt.get().getCheckInAt() != null) {
            logger.debug("预约 {} 已办理入住，跳过自动取消", booking.getId());
            return false;
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        notificationService.notifyBookingCancelled(booking, "超时未办理入住，系统自动取消");

        logger.info("预约 {} 已自动取消，原因: 超时未办理入住", booking.getId());
        return true;
    }

    @Transactional
    public int autoCompleteFinishedBookings() {
        logger.info("开始执行自动完成预约任务");

        LocalDateTime now = LocalDateTime.now();
        LocalDate nowDate = now.toLocalDate();
        LocalTime nowTime = now.toLocalTime();

        List<BookingStatus> targetStatuses = Arrays.asList(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );

        List<Booking> finishedBookings = bookingRepository.findCompletedBookingsBefore(
                targetStatuses, nowDate, nowTime);

        if (finishedBookings.isEmpty()) {
            logger.info("没有找到需要自动完成的预约");
            return 0;
        }

        logger.info("找到 {} 个可能需要自动完成的预约，开始逐个处理", finishedBookings.size());

        int completedCount = 0;
        List<Long> failedIds = new ArrayList<>();

        for (Booking booking : finishedBookings) {
            try {
                if (processAutoComplete(booking)) {
                    completedCount++;
                }
            } catch (Exception e) {
                logger.error("自动完成预约失败，预约ID: {}", booking.getId(), e);
                failedIds.add(booking.getId());
            }
        }

        logger.info("自动完成任务执行完成，成功完成: {} 个，失败: {} 个", 
                completedCount, failedIds.size());

        if (!failedIds.isEmpty()) {
            logger.warn("自动完成失败的预约ID列表: {}", failedIds);
        }

        return completedCount;
    }

    private boolean processAutoComplete(Booking booking) {
        BookingStatus currentStatus = booking.getStatus();
        
        if (currentStatus == BookingStatus.CANCELLED || currentStatus == BookingStatus.COMPLETED) {
            logger.debug("预约 {} 状态为 {}，无需自动完成", booking.getId(), currentStatus);
            return false;
        }

        Optional<CheckIn> checkInOpt = checkInRepository.findByBookingId(booking.getId());
        
        if (checkInOpt.isEmpty()) {
            logger.debug("预约 {} 无签到记录，跳过自动完成", booking.getId());
            return false;
        }

        CheckIn checkIn = checkInOpt.get();
        
        if (checkIn.getCheckInAt() == null) {
            logger.debug("预约 {} 未办理入住，跳过自动完成", booking.getId());
            return false;
        }

        if (checkIn.getCheckOutAt() == null) {
            LocalDateTime endDateTime = LocalDateTime.of(
                    booking.getBookingDate(), booking.getEndTime());
            checkIn.setCheckOutAt(endDateTime);
            checkInRepository.save(checkIn);
            logger.debug("预约 {} 自动补充签出时间: {}", booking.getId(), endDateTime);
        }

        booking.setStatus(BookingStatus.COMPLETED);
        bookingRepository.save(booking);

        notificationService.notifyBookingCompleted(booking);

        logger.info("预约 {} 已自动完成，签出时间: {}", booking.getId(), checkIn.getCheckOutAt());
        return true;
    }

    @Transactional(readOnly = true)
    public long countActiveBookings() {
        long pending = bookingRepository.countByStatus(BookingStatus.PENDING);
        long confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        return pending + confirmed;
    }
}
