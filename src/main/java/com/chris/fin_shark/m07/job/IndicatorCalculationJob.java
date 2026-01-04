package com.chris.fin_shark.m07.job;

import com.chris.fin_shark.m07.service.IndicatorCalculationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 指標計算排程 Job
 * <p>
 * 功能編號: F-M07-013
 * 每日定時執行技術指標計算
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class IndicatorCalculationJob {

    private final IndicatorCalculationService calculationService;

    /**
     * 定時執行指標計算
     * <p>
     * 執行時間：每週一到週五 15:30（盤後）
     * Cron 表達式：秒 分 時 日 月 週
     * </p>
     */
    @Scheduled(cron = "0 30 15 * * MON-FRI")
    public void calculateDailyIndicators() {
        log.info("排程觸發：技術指標計算 Job");

        LocalDate today = LocalDate.now();

        try {
            // 計算基礎組指標（P0）
            calculationService.calculateIndicators(
                    today,
                    null, // 全部股票
                    "P0",
                    false
            );

            // 計算進階組指標（P1）
            calculationService.calculateIndicators(
                    today,
                    null,
                    "P1",
                    false
            );

            log.info("技術指標計算 Job 執行完成");

        } catch (Exception e) {
            log.error("技術指標計算 Job 執行失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 手動觸發指標計算
     * <p>
     * 提供給 Controller 呼叫，用於手動補齊資料
     * </p>
     *
     * @param calculationDate 計算日期
     * @param priority        指標優先級
     */
    public void calculateIndicatorsManually(LocalDate calculationDate, String priority) {
        log.info("手動觸發：技術指標計算 Job, date={}, priority={}", calculationDate, priority);

        calculationService.calculateIndicators(
                calculationDate,
                null,
                priority,
                false
        );
    }
}
