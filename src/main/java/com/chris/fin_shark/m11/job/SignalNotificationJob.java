package com.chris.fin_shark.m11.job;

import com.chris.fin_shark.m11.dto.response.SignalScanResponse;
import com.chris.fin_shark.m11.service.SignalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 信號通知排程 Job
 * <p>
 * JOB-M11-002: NOTIFY_M13_SIGNALS
 * 每交易日 16:45 通知 M13 新信號已就緒
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SignalNotificationJob {

    private final SignalService signalService;

    /**
     * 定時通知 M13 新信號已就緒
     * <p>
     * 執行時間：每週一到週五 16:45
     * 前置條件：JOB-M11-001 完成
     * </p>
     */
    @Scheduled(cron = "0 45 16 * * MON-FRI")
    public void notifySignalsReady() {
        log.info("排程觸發：信號通知 Job");

        LocalDate today = LocalDate.now();

        try {
            // 掃描當日信號
            SignalScanResponse scanResult = signalService.scanSignals(
                    today,
                    null,
                    BigDecimal.ZERO,
                    null,
                    1000);

            int totalSignals = scanResult.getTotalSignals();

            if (totalSignals > 0) {
                // 發送事件通知 M13
                notifyM13(today, scanResult);
                log.info("信號通知 Job 完成: {} 個信號已通知 M13", totalSignals);
            } else {
                log.info("信號通知 Job 完成: 無新信號需通知");
            }

        } catch (Exception e) {
            log.error("信號通知 Job 失敗: {}", e.getMessage(), e);
        }
    }

    /**
     * 手動觸發信號通知
     *
     * @param tradeDate 交易日期
     */
    public void notifyManually(LocalDate tradeDate) {
        log.info("手動觸發：信號通知 Job, date={}", tradeDate);

        SignalScanResponse scanResult = signalService.scanSignals(
                tradeDate,
                null,
                BigDecimal.ZERO,
                null,
                1000);

        if (scanResult.getTotalSignals() > 0) {
            notifyM13(tradeDate, scanResult);
        }
    }

    /**
     * 通知 M13 信號已就緒
     * <p>
     * TODO: 實作事件匯流排通知機制
     * 目前僅記錄日誌，待 M13 實作後整合
     * </p>
     */
    private void notifyM13(LocalDate tradeDate, SignalScanResponse scanResult) {
        log.info("通知 M13: date={}, signals={}, buy={}, sell={}, hold={}",
                tradeDate,
                scanResult.getTotalSignals(),
                scanResult.getSignalSummary().getBuy(),
                scanResult.getSignalSummary().getSell(),
                scanResult.getSignalSummary().getHold());

        // TODO: 透過事件匯流排或訊息佇列通知 M13
        // eventBus.publish(StrategySignalsReadyEvent.builder()
        //         .tradeDate(tradeDate)
        //         .totalSignals(scanResult.getTotalSignals())
        //         .summary(scanResult.getSignalSummary())
        //         .build());
    }
}
