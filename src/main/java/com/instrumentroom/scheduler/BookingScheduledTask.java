package com.instrumentroom.scheduler;

import com.instrumentroom.config.SchedulerConfig;
import com.instrumentroom.service.BookingStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class BookingScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(BookingScheduledTask.class);

    private final BookingStatusService bookingStatusService;

    private final AtomicBoolean autoCancelRunning = new AtomicBoolean(false);
    private final AtomicBoolean autoCompleteRunning = new AtomicBoolean(false);

    public BookingScheduledTask(BookingStatusService bookingStatusService) {
        this.bookingStatusService = bookingStatusService;
    }

    @Scheduled(cron = SchedulerConfig.AUTO_CANCEL_CRON)
    public void executeAutoCancelTask() {
        String taskName = "自动取消超时预约";
        
        if (!autoCancelRunning.compareAndSet(false, true)) {
            logger.warn("{} 任务上一次执行尚未完成，跳过本次执行", taskName);
            return;
        }

        long startTime = System.currentTimeMillis();
        logger.info("========== {} 任务开始执行 ==========", taskName);

        try {
            int cancelledCount = bookingStatusService.autoCancelExpiredBookings();
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("========== {} 任务执行完成，取消预约数: {}，耗时: {}ms ==========", 
                    taskName, cancelledCount, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("========== {} 任务执行异常，耗时: {}ms ==========", 
                    taskName, duration, e);

        } finally {
            autoCancelRunning.set(false);
        }
    }

    @Scheduled(cron = SchedulerConfig.AUTO_COMPLETE_CRON)
    public void executeAutoCompleteTask() {
        String taskName = "自动完成已结束预约";
        
        if (!autoCompleteRunning.compareAndSet(false, true)) {
            logger.warn("{} 任务上一次执行尚未完成，跳过本次执行", taskName);
            return;
        }

        long startTime = System.currentTimeMillis();
        logger.info("========== {} 任务开始执行 ==========", taskName);

        try {
            int completedCount = bookingStatusService.autoCompleteFinishedBookings();
            long duration = System.currentTimeMillis() - startTime;
            
            logger.info("========== {} 任务执行完成，完成预约数: {}，耗时: {}ms ==========", 
                    taskName, completedCount, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("========== {} 任务执行异常，耗时: {}ms ==========", 
                    taskName, duration, e);

        } finally {
            autoCompleteRunning.set(false);
        }
    }
}
