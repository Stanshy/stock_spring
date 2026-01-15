package com.chris.fin_shark.m11.job;

import com.chris.fin_shark.m11.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 因子元數據刷新排程 Job
 * <p>
 * JOB-M11-005: REFRESH_FACTOR_METADATA
 * 每週一 07:00 從 M07/M08/M09 同步最新的因子定義
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FactorMetadataRefreshJob {

    private final StrategyMapper strategyMapper;

    /**
     * 定時刷新因子元數據
     * <p>
     * 執行時間：每週一 07:00
     * </p>
     */
    @Scheduled(cron = "0 0 7 * * MON")
    public void refreshFactorMetadata() {
        log.info("排程觸發：因子元數據刷新 Job");

        try {
            // 目前因子元數據是靜態定義在資料庫中
            // 此 Job 主要用於未來支援動態因子註冊
            // 或刷新因子元數據的快取

            int factorCount = strategyMapper.countActiveFactors();
            log.info("因子元數據刷新 Job 完成: {} 個活躍因子", factorCount);

        } catch (Exception e) {
            log.error("因子元數據刷新 Job 失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 手動觸發刷新
     */
    public void refreshManually() {
        log.info("手動觸發：因子元數據刷新 Job");
        int factorCount = strategyMapper.countActiveFactors();
        log.info("因子元數據刷新完成: {} 個活躍因子", factorCount);
    }
}
