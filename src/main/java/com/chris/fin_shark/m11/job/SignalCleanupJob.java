package com.chris.fin_shark.m11.job;

import com.chris.fin_shark.m11.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 信號清理排程 Job
 * <p>
 * JOB-M11-004: CLEANUP_OLD_SIGNALS
 * 每週日 03:00 清理過期的策略信號與執行記錄
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SignalCleanupJob {

    private final StrategyMapper strategyMapper;

    // 信號保留天數
    private static final int SIGNALS_RETENTION_DAYS = 90;

    // 執行記錄完整保留天數
    private static final int EXECUTIONS_FULL_RETENTION_DAYS = 30;

    /**
     * 定時清理過期資料
     * <p>
     * 執行時間：每週日 03:00
     * </p>
     */
    @Scheduled(cron = "0 0 3 * * SUN")
    @Transactional
    public void cleanupOldSignals() {
        log.info("排程觸發：信號清理 Job");

        try {
            LocalDate signalsCutoffDate = LocalDate.now().minusDays(SIGNALS_RETENTION_DAYS);
            LocalDate executionsCutoffDate = LocalDate.now().minusDays(EXECUTIONS_FULL_RETENTION_DAYS);

            // 清理過期信號
            int deletedSignals = strategyMapper.deleteOldSignals(signalsCutoffDate);
            log.info("已清理 {} 筆過期信號（超過 {} 天）", deletedSignals, SIGNALS_RETENTION_DAYS);

            // 清理過期執行記錄的診斷資訊
            int cleanedExecutions = strategyMapper.clearOldExecutionDiagnostics(executionsCutoffDate);
            log.info("已清理 {} 筆執行記錄的診斷資訊（超過 {} 天）",
                    cleanedExecutions, EXECUTIONS_FULL_RETENTION_DAYS);

            log.info("信號清理 Job 完成");

        } catch (Exception e) {
            log.error("信號清理 Job 失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 手動觸發清理
     *
     * @param signalsRetentionDays    信號保留天數
     * @param executionsRetentionDays 執行記錄診斷保留天數
     */
    @Transactional
    public void cleanupManually(int signalsRetentionDays, int executionsRetentionDays) {
        log.info("手動觸發：信號清理 Job, signalsRetention={}, executionsRetention={}",
                signalsRetentionDays, executionsRetentionDays);

        LocalDate signalsCutoffDate = LocalDate.now().minusDays(signalsRetentionDays);
        LocalDate executionsCutoffDate = LocalDate.now().minusDays(executionsRetentionDays);

        int deletedSignals = strategyMapper.deleteOldSignals(signalsCutoffDate);
        int cleanedExecutions = strategyMapper.clearOldExecutionDiagnostics(executionsCutoffDate);

        log.info("手動清理完成: 清理 {} 信號, {} 執行診斷", deletedSignals, cleanedExecutions);
    }
}
