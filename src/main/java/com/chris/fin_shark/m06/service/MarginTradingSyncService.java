package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.client.twse.TwseApiClient;
import com.chris.fin_shark.client.twse.TwseMarginClient;
import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.common.dto.external.TwseMarginData;
import com.chris.fin_shark.common.enums.JobStatus;
import com.chris.fin_shark.common.enums.JobType;
import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.domain.MarginTrading;
import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.mapper.MarginTradingMapper;
import com.chris.fin_shark.m06.repository.JobExecutionRepository;
import com.chris.fin_shark.m06.repository.MarginTradingRepository;
import com.chris.fin_shark.m06.repository.StockRepository;
import com.chris.fin_shark.m06.repository.TradingCalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 融資融券同步服務
 * <p>
 * 功能編號: F-M06-004
 * 功能名稱: 籌碼資料同步
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarginTradingSyncService {

    private final TwseMarginClient twseMarginClient;
    private final StockRepository stockRepository;
    private final MarginTradingRepository marginTradingRepository;
    private final MarginTradingMapper marginTradingMapper;
    private final TradingCalendarRepository tradingCalendarRepository;
    private final JobExecutionRepository jobExecutionRepository;

    /**
     * 同步指定日期的所有股票融資融券
     *
     * @param tradeDate   交易日期
     * @param triggerType 觸發類型
     * @return Job 執行記錄
     */
    @Transactional
    public JobExecution syncMarginTradingForDate(LocalDate tradeDate, TriggerType triggerType) {
        log.info("開始同步融資融券資料: tradeDate={}, triggerType={}", tradeDate, triggerType.getDescription());

        // 1. 檢查是否為交易日
        if (!tradingCalendarRepository.isTradingDay(tradeDate)) {
            log.warn("非交易日，跳過同步: {}", tradeDate);
            return createSkippedExecution(tradeDate, triggerType);
        }

        // 2. 建立 Job 執行記錄
        JobExecution execution = createJobExecution(tradeDate, triggerType);

        try {
            // 3. 查詢所有活躍股票
            List<Stock> activeStocks = stockRepository.findByIsActiveTrue();
            Set<String> stockIds = activeStocks.stream()
                    .map(Stock::getStockId)
                    .collect(Collectors.toSet());
            execution.setTotalItems(stockIds.size());
            log.info("查詢到 {} 檔活躍股票", stockIds.size());

            // 4. 批次呼叫 TWSE API
            List<TwseMarginData> apiDataList = twseMarginClient.getMarginTrading(tradeDate, stockIds);
            log.info("從 TWSE API 取得 {} 筆融資融券資料", apiDataList.size());

            if (apiDataList.isEmpty()) {
                log.warn("TWSE API 未回傳任何資料，可能非交易日或 API 異常");
                execution.setJobStatus(JobStatus.FAILED.getCode());
                execution.setErrorMessage("API 未回傳資料");
                execution.setEndTime(LocalDateTime.now());
                return jobExecutionRepository.save(execution);
            }

            // 5. 轉換 DTO -> Entity
            List<MarginTrading> entities = apiDataList.stream()
                    .map(this::convertToEntity)
                    .collect(Collectors.toList());

            // 6. 批次儲存（UPSERT）
            try {
                marginTradingMapper.batchInsert(entities);
                execution.setSuccessItems(entities.size());
                execution.setFailedItems(0);
                log.info("融資融券批次儲存成功: {} 筆", entities.size());
            } catch (Exception e) {
                log.error("融資融券批次儲存失敗", e);
                execution.setSuccessItems(0);
                execution.setFailedItems(entities.size());
                throw e;
            }

            execution.setProcessedItems(entities.size());

            // 7. 更新執行結果
            long durationMs = java.time.Duration.between(
                    execution.getStartTime(), LocalDateTime.now()).toMillis();

            execution.setJobStatus(JobStatus.SUCCESS.getCode());
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationMs(durationMs);

            log.info("融資融券同步完成: 成功 {}, 耗時 {}ms", entities.size(), durationMs);

        } catch (Exception e) {
            log.error("融資融券同步發生嚴重錯誤", e);

            long durationMs = java.time.Duration.between(
                    execution.getStartTime(), LocalDateTime.now()).toMillis();

            execution.setJobStatus(JobStatus.FAILED.getCode());
            execution.setErrorMessage(e.getMessage());
            execution.setErrorStackTrace(getStackTrace(e));
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationMs(durationMs);
        }

        return jobExecutionRepository.save(execution);
    }

    /**
     * 轉換 API DTO 為 Entity
     */
    private MarginTrading convertToEntity(TwseMarginData data) {
        return MarginTrading.builder()
                .stockId(data.getStockId())
                .tradeDate(data.getTradeDate())
                .marginPurchase(data.getMarginPurchase())
                .marginSell(data.getMarginSell())
                .marginBalance(data.getMarginBalance())
                .marginQuota(data.getMarginQuota())
                .shortPurchase(data.getShortPurchase())
                .shortSell(data.getShortSell())
                .shortBalance(data.getShortBalance())
                .shortQuota(data.getShortQuota())
                .build();
    }

    /**
     * 建立 Job 執行記錄
     */
    private JobExecution createJobExecution(LocalDate tradeDate, TriggerType triggerType) {
        JobExecution execution = new JobExecution();
        execution.setJobName("MarginTradingSync");
        execution.setJobType(JobType.DATA_SYNC.getCode());
        execution.setJobStatus(JobStatus.RUNNING.getCode());
        execution.setStartTime(LocalDateTime.now());
        execution.setTriggerType(triggerType.getCode());
        execution.setRetryCount(0);
        execution.setMaxRetry(3);

        return jobExecutionRepository.save(execution);
    }

    /**
     * 建立跳過執行的記錄（非交易日）
     */
    private JobExecution createSkippedExecution(LocalDate tradeDate, TriggerType triggerType) {
        JobExecution execution = new JobExecution();
        execution.setJobName("MarginTradingSync");
        execution.setJobType(JobType.DATA_SYNC.getCode());
        execution.setJobStatus(JobStatus.CANCELLED.getCode());
        execution.setStartTime(LocalDateTime.now());
        execution.setEndTime(LocalDateTime.now());
        execution.setDurationMs(0L);
        execution.setErrorMessage("非交易日: " + tradeDate);
        execution.setTriggerType(triggerType.getCode());

        return jobExecutionRepository.save(execution);
    }

    /**
     * 取得異常堆疊追蹤
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}
