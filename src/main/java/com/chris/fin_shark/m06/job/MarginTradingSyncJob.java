package com.chris.fin_shark.m06.job;

import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.service.MarginTradingSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 融資融券同步排程 Job
 * <p>
 * 功能編號: F-M06-008
 * 每日定時執行融資融券資料同步
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class MarginTradingSyncJob {

    private final MarginTradingSyncService marginTradingSyncService;

    /**
     * 定時執行融資融券同步
     * <p>
     * 執行時間：每週一到週五 17:00
     * Cron 表達式：秒 分 時 日 月 週
     * </p>
     */
    @Scheduled(cron = "0 0 17 * * MON-FRI")
    public void syncDailyMarginTrading() {
        log.info("排程觸發：融資融券同步 Job");

        LocalDate today = LocalDate.now();
        marginTradingSyncService.syncMarginTradingForDate(today, TriggerType.SCHEDULED);
    }

    /**
     * 手動觸發融資融券同步
     * <p>
     * 提供給 Controller 呼叫，用於手動補齊資料
     * </p>
     *
     * @param tradeDate 交易日期
     */
    public void syncMarginTradingManually(LocalDate tradeDate) {
        log.info("手動觸發：融資融券同步 Job, tradeDate={}", tradeDate);

        marginTradingSyncService.syncMarginTradingForDate(tradeDate, TriggerType.MANUAL);
    }
}
