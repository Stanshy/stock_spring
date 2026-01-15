package com.chris.fin_shark.m11.job;

import com.chris.fin_shark.m11.service.StrategyExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 策略批次執行排程 Job
 * <p>
 * JOB-M11-001: EXEC_ALL_STRATEGIES
 * 每交易日 16:35 執行所有活躍策略
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StrategyExecutionJob {

    private final StrategyExecutionService strategyExecutionService;

    /**
     * 定時執行所有活躍策略
     * <p>
     * 執行時間：每週一到週五 16:35（盤後）
     * 前置條件：M07/M08/M09 計算完成
     * </p>
     */
    @Scheduled(cron = "0 35 16 * * MON-FRI")
    public void executeAllStrategies() {
        log.info("排程觸發：策略批次執行 Job");

        LocalDate today = LocalDate.now();

        try {
            strategyExecutionService.executeBatch(today);
            log.info("策略批次執行 Job 完成");

        } catch (Exception e) {
            log.error("策略批次執行 Job 失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 手動觸發批次執行
     *
     * @param executionDate 執行日期
     */
    public void executeManually(LocalDate executionDate) {
        log.info("手動觸發：策略批次執行 Job, date={}", executionDate);
        strategyExecutionService.executeBatch(executionDate);
    }
}
