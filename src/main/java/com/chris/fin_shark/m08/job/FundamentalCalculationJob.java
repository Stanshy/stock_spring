package com.chris.fin_shark.m08.job;

import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.common.enums.JobStatus;
import com.chris.fin_shark.common.enums.JobType;
import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m08.dto.request.CalculateFundamentalsRequest;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m06.repository.JobExecutionRepository;
import com.chris.fin_shark.m08.service.FundamentalIndicatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 財務指標計算排程 Job
 * <p>
 * 功能編號: JOB-M08-001
 * 每週一 09:00 自動計算所有股票的財務指標
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FundamentalCalculationJob {

    private final FundamentalIndicatorService indicatorService;
    private final JobExecutionRepository jobExecutionRepository;

    // ========== 依賴 M06 模組 ==========
    // TODO: 注入 M06 依賴
    // private final StockRepository stockRepository;

    /**
     * 定時執行財務指標計算
     * <p>
     * 執行時間：每週一 09:00
     * Cron 表達式：秒 分 時 日 月 週
     * </p>
     */
    @Scheduled(cron = "0 0 9 * * MON")
    public void calculateScheduled() {
        log.info("排程觸發：財務指標計算 Job");

        CalculateFundamentalsRequest request = CalculateFundamentalsRequest.builder()
                .force(false)
                .build();

        execute(request, TriggerType.SCHEDULED);
    }

    /**
     * 手動觸發財務指標計算
     *
     * @param request 計算請求
     */
    public void calculateManually(CalculateFundamentalsRequest request) {
        log.info("手動觸發：財務指標計算 Job, request={}", request);

        execute(request, TriggerType.MANUAL);
    }

    /**
     * 執行財務指標計算
     */
    private void execute(CalculateFundamentalsRequest request, TriggerType triggerType) {
        // 1. 建立 Job 執行記錄
        JobExecution execution = createJobExecution(triggerType);

        try {
            // 2. 查詢待處理股票清單
            List<String> stockIds = getStockIds(request);
            execution.setTotalItems(stockIds.size());
            log.info("查詢到 {} 檔股票需要計算財務指標", stockIds.size());

            int successCount = 0;
            int failCount = 0;

            // 3. 逐一計算財務指標
            for (String stockId : stockIds) {
                try {
                    // TODO: 取得最新財報的年度季度
                    Integer year = 2024;    // 範例值
                    Integer quarter = 3;     // 範例值

                    CalculationResult result = indicatorService.calculateAndSave(
                            stockId, year, quarter);

                    if (result.hasErrors()) {
                        log.error("計算失敗: stockId={}, errors={}",
                                stockId, result.getDiagnostics().getErrors());
                        failCount++;
                    } else {
                        log.debug("計算成功: stockId={}, 指標數量={}",
                                stockId, result.getTotalIndicatorCount());
                        successCount++;
                    }

                } catch (Exception e) {
                    log.error("計算失敗: stockId={}", stockId, e);
                    failCount++;
                }

                // 更新進度
                execution.setProcessedItems(successCount + failCount);
                jobExecutionRepository.save(execution);

                // 避免 CPU 過載
                Thread.sleep(50);
            }

            // 4. 計算執行時長
            long durationMs = java.time.Duration.between(
                    execution.getStartTime(), LocalDateTime.now()).toMillis();

            // 5. 更新執行結果
            execution.setSuccessItems(successCount);
            execution.setFailedItems(failCount);
            execution.setJobStatus(JobStatus.SUCCESS.getCode());
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationMs(durationMs);

            log.info("財務指標計算完成: 成功 {}, 失敗 {}, 耗時 {}ms",
                    successCount, failCount, durationMs);

        } catch (Exception e) {
            log.error("財務指標計算發生嚴重錯誤", e);

            long durationMs = java.time.Duration.between(
                    execution.getStartTime(), LocalDateTime.now()).toMillis();

            execution.setJobStatus(JobStatus.FAILED.getCode());
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationMs(durationMs);
        }

        jobExecutionRepository.save(execution);
    }

    /**
     * 取得待處理股票清單
     */
    private List<String> getStockIds(CalculateFundamentalsRequest request) {
        if (request.getStockIds() != null && !request.getStockIds().isEmpty()) {
            // 手動指定股票
            return request.getStockIds();
        }

        // TODO: 從 M06 查詢所有活躍股票
        // return stockRepository.findByIsActiveTrue()
        //         .stream()
        //         .map(Stock::getStockId)
        //         .collect(Collectors.toList());

        // 臨時範例
        return List.of("2330", "2317", "2454");
    }

    /**
     * 建立 Job 執行記錄
     */
    private JobExecution createJobExecution(TriggerType triggerType) {
        JobExecution execution = new JobExecution();
        execution.setJobName("FundamentalCalculation");
        execution.setJobType(JobType.CALCULATION.getCode());
        execution.setJobStatus(JobStatus.RUNNING.getCode());
        execution.setStartTime(LocalDateTime.now());
        execution.setTriggerType(triggerType.getCode());
        execution.setRetryCount(0);
        execution.setMaxRetry(3);

        return jobExecutionRepository.save(execution);
    }
}
