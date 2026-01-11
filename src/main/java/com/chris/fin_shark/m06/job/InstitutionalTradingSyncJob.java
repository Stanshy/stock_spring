package com.chris.fin_shark.m06.job;

import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.service.InstitutionalTradingSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * 三大法人買賣超同步排程 Job
 * <p>
 * 功能編號: F-M06-008
 * 每日定時執行法人買賣超資料同步
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class InstitutionalTradingSyncJob {

    private final InstitutionalTradingSyncService institutionalTradingSyncService;

    /**
     * 定時執行法人買賣超同步
     * <p>
     * 執行時間：每週一到週五 16:30
     * Cron 表達式：秒 分 時 日 月 週
     * </p>
     */
    @Scheduled(cron = "0 30 16 * * MON-FRI")
    public void syncDailyInstitutionalTrading() {
        log.info("排程觸發：法人買賣超同步 Job");

        LocalDate today = LocalDate.now();
        institutionalTradingSyncService.syncInstitutionalTradingForDate(today, TriggerType.SCHEDULED);
    }

    /**
     * 手動觸發法人買賣超同步
     * <p>
     * 提供給 Controller 呼叫，用於手動補齊資料
     * </p>
     *
     * @param tradeDate 交易日期
     */
    public void syncInstitutionalTradingManually(LocalDate tradeDate) {
        log.info("手動觸發：法人買賣超同步 Job, tradeDate={}", tradeDate);

        institutionalTradingSyncService.syncInstitutionalTradingForDate(tradeDate, TriggerType.MANUAL);
    }
}
