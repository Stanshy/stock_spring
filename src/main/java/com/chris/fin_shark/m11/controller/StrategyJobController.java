package com.chris.fin_shark.m11.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m11.job.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 策略 Job 手動執行 Controller
 * <p>
 * 提供手動觸發 Job 執行的 API
 * Base URL: /api/v1/strategy/jobs
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/strategy/jobs")
@Slf4j
@RequiredArgsConstructor
public class StrategyJobController {

    private final StrategyExecutionJob strategyExecutionJob;
    private final SignalNotificationJob signalNotificationJob;
    private final StrategyStatisticsJob strategyStatisticsJob;
    private final SignalCleanupJob signalCleanupJob;
    private final FactorMetadataRefreshJob factorMetadataRefreshJob;

    /**
     * 手動執行所有活躍策略
     *
     * @param tradeDate 交易日期（預設今日）
     * @return 執行結果
     */
    @PostMapping("/exec-all")
    public ApiResponse<Map<String, Object>> executeAllStrategies(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate) {

        if (tradeDate == null) {
            tradeDate = LocalDate.now();
        }

        log.info("POST /api/v1/strategy/jobs/exec-all date={}", tradeDate);

        long startTime = System.currentTimeMillis();
        strategyExecutionJob.executeManually(tradeDate);
        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new HashMap<>();
        result.put("job", "EXEC_ALL_STRATEGIES");
        result.put("tradeDate", tradeDate);
        result.put("durationMs", duration);
        result.put("status", "SUCCESS");

        return ApiResponse.success("Job executed successfully", result);
    }

    /**
     * 手動通知 M13 信號已就緒
     *
     * @param tradeDate 交易日期（預設今日）
     * @return 執行結果
     */
    @PostMapping("/notify-m13")
    public ApiResponse<Map<String, Object>> notifyM13(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate) {

        if (tradeDate == null) {
            tradeDate = LocalDate.now();
        }

        log.info("POST /api/v1/strategy/jobs/notify-m13 date={}", tradeDate);

        signalNotificationJob.notifyManually(tradeDate);

        Map<String, Object> result = new HashMap<>();
        result.put("job", "NOTIFY_M13_SIGNALS");
        result.put("tradeDate", tradeDate);
        result.put("status", "SUCCESS");

        return ApiResponse.success("M13 notification sent", result);
    }

    /**
     * 手動更新策略統計
     *
     * @return 執行結果
     */
    @PostMapping("/update-stats")
    public ApiResponse<Map<String, Object>> updateStatistics() {

        log.info("POST /api/v1/strategy/jobs/update-stats");

        long startTime = System.currentTimeMillis();
        strategyStatisticsJob.updateManually(LocalDate.now());
        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new HashMap<>();
        result.put("job", "UPDATE_STRATEGY_STATS");
        result.put("durationMs", duration);
        result.put("status", "SUCCESS");

        return ApiResponse.success("Statistics updated", result);
    }

    /**
     * 手動清理過期信號
     *
     * @param signalsRetentionDays    信號保留天數（預設 90）
     * @param executionsRetentionDays 執行診斷保留天數（預設 30）
     * @return 執行結果
     */
    @PostMapping("/cleanup")
    public ApiResponse<Map<String, Object>> cleanupOldSignals(
            @RequestParam(required = false, defaultValue = "90") int signalsRetentionDays,
            @RequestParam(required = false, defaultValue = "30") int executionsRetentionDays) {

        log.info("POST /api/v1/strategy/jobs/cleanup signals={}d, executions={}d",
                signalsRetentionDays, executionsRetentionDays);

        long startTime = System.currentTimeMillis();
        signalCleanupJob.cleanupManually(signalsRetentionDays, executionsRetentionDays);
        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new HashMap<>();
        result.put("job", "CLEANUP_OLD_SIGNALS");
        result.put("signalsRetentionDays", signalsRetentionDays);
        result.put("executionsRetentionDays", executionsRetentionDays);
        result.put("durationMs", duration);
        result.put("status", "SUCCESS");

        return ApiResponse.success("Cleanup completed", result);
    }

    /**
     * 手動刷新因子元數據
     *
     * @return 執行結果
     */
    @PostMapping("/refresh-factors")
    public ApiResponse<Map<String, Object>> refreshFactorMetadata() {

        log.info("POST /api/v1/strategy/jobs/refresh-factors");

        factorMetadataRefreshJob.refreshManually();

        Map<String, Object> result = new HashMap<>();
        result.put("job", "REFRESH_FACTOR_METADATA");
        result.put("status", "SUCCESS");

        return ApiResponse.success("Factor metadata refreshed", result);
    }
}
