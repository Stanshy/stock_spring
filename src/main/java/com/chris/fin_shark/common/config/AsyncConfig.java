package com.chris.fin_shark.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 非同步任務配置
 *
 * 配置非同步執行緒池
 *
 * @author chris
 * @since 2025-12-24
 */
@Configuration
@EnableAsync
@Slf4j
public class AsyncConfig {

    /**
     * 非同步任務執行緒池
     *
     * @return Executor
     */
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心執行緒數
        executor.setCorePoolSize(10);

        // 最大執行緒數
        executor.setMaxPoolSize(20);

        // 佇列容量
        executor.setQueueCapacity(200);

        // 執行緒名稱前綴
        executor.setThreadNamePrefix("async-");

        // 拒絕策略: CallerRunsPolicy（由調用執行緒執行）
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任務完成後再關閉執行緒池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待時間（秒）
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("Async executor initialized: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(),
                executor.getMaxPoolSize(),
                executor.getQueueCapacity());

        return executor;
    }

    // TODO: 各模組開發時，可以配置多個執行緒池
    // 範例:
    // @Bean(name = "jobExecutor")
    // public Executor jobExecutor() {
    //     ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    //     executor.setCorePoolSize(5);
    //     executor.setMaxPoolSize(10);
    //     executor.setThreadNamePrefix("job-");
    //     executor.initialize();
    //     return executor;
    // }
}
