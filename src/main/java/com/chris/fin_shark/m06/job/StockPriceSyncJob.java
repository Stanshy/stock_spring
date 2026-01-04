package com.chris.fin_shark.m06.job;

import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.service.StockPriceSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 股價同步排程 Job
 * <p>
 * 功能編號: F-M06-008
 * 每日定時執行股價資料同步
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StockPriceSyncJob {

    private final StockPriceSyncService stockPriceSyncService;

    /**
     * 定時執行股價同步
     * <p>
     * 執行時間：每週一到週五 18:00
     * Cron 表達式：秒 分 時 日 月 週
     * </p>
     */
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void syncDailyStockPrices() {
        log.info("排程觸發：股價同步 Job");

        LocalDate today = LocalDate.now();
        stockPriceSyncService.syncStockPricesForDate(today, TriggerType.SCHEDULED);
    }

    /**
     * 手動觸發股價同步
     * <p>
     * 提供給 Controller 呼叫，用於手動補齊資料
     * </p>
     *
     * @param tradeDate 交易日期
     */
    public void syncStockPricesManually(LocalDate tradeDate) {
        log.info("手動觸發：股價同步 Job, tradeDate={}", tradeDate);

        stockPriceSyncService.syncStockPricesForDate(tradeDate, TriggerType.MANUAL);
    }


    /**
     * 手動觸發「整個月份」股價同步
     * <p>
     * 提供給 Controller 呼叫，用於補齊整月資料
     * </p>
     *
     * @param monthDate 任意該月日期（會自動轉為月初）
     */
    public void syncStockPricesForMonthManually(LocalDate monthDate) {
        LocalDate targetMonth = monthDate.withDayOfMonth(1);
        log.info("手動觸發：股價同步 Job（整月）, month={}", targetMonth);

        stockPriceSyncService.syncStockPricesForMonth(
                targetMonth,
                TriggerType.MANUAL
        );
    }

}
