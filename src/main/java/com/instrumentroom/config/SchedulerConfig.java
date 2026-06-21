package com.instrumentroom.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerConfig.class);

    private static final int POOL_SIZE = 5;
    private static final String THREAD_NAME_PREFIX = "booking-scheduler-";
    private static final int AWAIT_TERMINATION_SECONDS = 30;

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(POOL_SIZE);
        scheduler.setThreadNamePrefix(THREAD_NAME_PREFIX);
        scheduler.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setErrorHandler(throwable -> 
            logger.error("定时任务执行发生未捕获异常", throwable)
        );
        scheduler.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                logger.warn("定时任务被拒绝执行，当前活跃线程: {}, 队列大小: {}", 
                    executor.getActiveCount(), executor.getQueue().size());
            }
        });
        scheduler.initialize();
        logger.info("定时任务调度器初始化完成，线程池大小: {}", POOL_SIZE);
        return scheduler;
    }

    @Bean
    public CronTrigger autoCancelCronTrigger() {
        return new CronTrigger("0 */5 * * * ?");
    }

    @Bean
    public CronTrigger autoCompleteCronTrigger() {
        return new CronTrigger("0 */10 * * * ?");
    }
}
