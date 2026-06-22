package com.instrumentroom.scheduler;

import com.instrumentroom.config.SchedulerConfig;
import com.instrumentroom.entity.Booking;
import com.instrumentroom.entity.BookingStatus;
import com.instrumentroom.notification.NotificationService;
import com.instrumentroom.repository.BookingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class NotificationScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduledTask.class);

    private static final long REMINDER_MINUTES_BEFORE = 60;
    private static final long OVERDUE_MINUTES = 30;

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    private final AtomicBoolean reminderRunning = new AtomicBoolean(false);
    private final AtomicBoolean overdueRunning = new AtomicBoolean(false);

    public NotificationScheduledTask(BookingRepository bookingRepository,
                                     NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = SchedulerConfig.BOOKING_REMINDER_CRON)
    public void executeBookingReminderTask() {
        String taskName = "预约开始前提醒";

        if (!reminderRunning.compareAndSet(false, true)) {
            logger.warn("{} 任务上一次执行尚未完成，跳过本次执行", taskName);
            return;
        }

        long startTime = System.currentTimeMillis();
        logger.info("========== {} 任务开始执行 ==========", taskName);

        try {
            int reminderCount = sendBookingReminders();
            long duration = System.currentTimeMillis() - startTime;

            logger.info("========== {} 任务执行完成，提醒预约数: {}，耗时: {}ms ==========",
                    taskName, reminderCount, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("========== {} 任务执行异常，耗时: {}ms ==========",
                    taskName, duration, e);

        } finally {
            reminderRunning.set(false);
        }
    }

    @Scheduled(cron = SchedulerConfig.CHECK_IN_OVERDUE_CRON)
    public void executeCheckInOverdueTask() {
        String taskName = "入住逾期提醒";

        if (!overdueRunning.compareAndSet(false, true)) {
            logger.warn("{} 任务上一次执行尚未完成，跳过本次执行", taskName);
            return;
        }

        long startTime = System.currentTimeMillis();
        logger.info("========== {} 任务开始执行 ==========", taskName);

        try {
            int overdueCount = sendOverdueNotifications();
            long duration = System.currentTimeMillis() - startTime;

            logger.info("========== {} 任务执行完成，逾期提醒数: {}，耗时: {}ms ==========",
                    taskName, overdueCount, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("========== {} 任务执行异常，耗时: {}ms ==========",
                    taskName, duration, e);

        } finally {
            overdueRunning.set(false);
        }
    }

    private int sendBookingReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderWindowStart = now.plusMinutes(REMINDER_MINUTES_BEFORE - 30);
        LocalDateTime reminderWindowEnd = now.plusMinutes(REMINDER_MINUTES_BEFORE + 30);

        LocalDate today = LocalDate.now();
        LocalTime startTime = reminderWindowStart.toLocalTime();
        LocalTime endTime = reminderWindowEnd.toLocalTime();

        logger.debug("查询即将开始的预约，时间范围: {} - {}", startTime, endTime);

        List<BookingStatus> targetStatuses = Arrays.asList(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );

        List<Booking> upcomingBookings = bookingRepository.findBookingsStartingBetween(
                targetStatuses, today, startTime, endTime);

        if (upcomingBookings.isEmpty()) {
            logger.info("没有找到即将开始的预约");
            return 0;
        }

        logger.info("找到 {} 个即将开始的预约，开始发送提醒", upcomingBookings.size());

        int count = 0;
        for (Booking booking : upcomingBookings) {
            try {
                notificationService.notifyBookingReminder(booking);
                count++;
            } catch (Exception e) {
                logger.error("发送预约提醒失败，预约ID: {}", booking.getId(), e);
            }
        }

        return count;
    }

    private int sendOverdueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime overdueDeadline = now.minusMinutes(OVERDUE_MINUTES);

        LocalDate today = LocalDate.now();
        LocalTime overdueTime = overdueDeadline.toLocalTime();

        logger.debug("查询逾期未签到的预约，截止时间: {}", overdueTime);

        List<BookingStatus> targetStatuses = Arrays.asList(
                BookingStatus.PENDING,
                BookingStatus.CONFIRMED
        );

        List<Booking> overdueBookings = bookingRepository.findOverdueCheckIns(
                targetStatuses, today, overdueTime);

        if (overdueBookings.isEmpty()) {
            logger.info("没有找到逾期未签到的预约");
            return 0;
        }

        logger.info("找到 {} 个逾期未签到的预约，开始发送提醒", overdueBookings.size());

        int count = 0;
        for (Booking booking : overdueBookings) {
            try {
                notificationService.notifyCheckInOverdue(booking);
                count++;
            } catch (Exception e) {
                logger.error("发送逾期提醒失败，预约ID: {}", booking.getId(), e);
            }
        }

        return count;
    }
}
