package com.instrumentroom.scheduler;

import com.instrumentroom.config.SchedulerConfig;
import com.instrumentroom.service.waitlist.WaitlistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class WaitlistScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(WaitlistScheduledTask.class);

    private final WaitlistService waitlistService;

    private final AtomicBoolean expireCleanupRunning = new AtomicBoolean(false);

    public WaitlistScheduledTask(WaitlistService waitlistService) {
        this.waitlistService = waitlistService;
    }

    @Scheduled(cron = SchedulerConfig.WAITLIST_EXPIRE_CRON)
    public void executeExpireCleanupTask() {
        String taskName = "候补过期清理";

        if (!expireCleanupRunning.compareAndSet(false, true)) {
            logger.warn("{} 任务上一次执行尚未完成，跳过本次执行", taskName);
            return;
        }

        long startTime = System.currentTimeMillis();
        logger.info("========== {} 任务开始执行 ==========", taskName);

        try {
            waitlistService.cleanExpiredWaitlists();
            long duration = System.currentTimeMillis() - startTime;

            logger.info("========== {} 任务执行完成，耗时: {}ms ==========",
                    taskName, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("========== {} 任务执行异常，耗时: {}ms ==========",
                    taskName, duration, e);

        } finally {
            expireCleanupRunning.set(false);
        }
    }
}
