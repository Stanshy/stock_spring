package com.chris.fin_shark.m11.job;

import com.chris.fin_shark.m11.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 策略統計更新排程 Job
 * <p>
 * JOB-M11-003: UPDATE_STRATEGY_STATS
 * 每交易日 17:00 更新策略統計數據
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StrategyStatisticsJob {

    private final StrategyMapper strategyMapper;

    /**
     * 定時更新策略統計數據
     * <p>
     * 執行時間：每週一到週五 17:00
     * 前置條件：JOB-M11-001 完成
     * </p>
     */
    @Scheduled(cron = "0 0 17 * * MON-FRI")
    @Transactional
    public void updateStrategyStatistics() {
        log.info("排程觸發：策略統計更新 Job");

        try {
            int updatedCount = strategyMapper.updateAllStatistics();
            log.info("策略統計更新 Job 完成: 更新 {} 個策略", updatedCount);

        } catch (Exception e) {
            log.error("策略統計更新 Job 失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 手動觸發統計更新
     *
     * @param tradeDate 交易日期（用於日誌記錄）
     */
    @Transactional
    public void updateManually(LocalDate tradeDate) {
        log.info("手動觸發：策略統計更新 Job, date={}", tradeDate);
        strategyMapper.updateAllStatistics();
    }
}
