package com.chris.fin_shark.m06.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * 排程器配置
 * <p>
 * 功能編號: F-M06-008
 * 功能名稱: 資料更新排程
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Configuration
@EnableScheduling
public class SchedulerConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix("scheduled-task-");
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setAwaitTerminationSeconds(60);
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
    }
}
