package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.client.twse.TwseApiClient;
import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.common.enums.JobStatus;
import com.chris.fin_shark.common.enums.JobType;
import com.chris.fin_shark.common.enums.TriggerType;

import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.common.dto.external.TwseStockPriceData;
import com.chris.fin_shark.m06.repository.JobExecutionRepository;
import com.chris.fin_shark.m06.repository.StockPriceRepository;
import com.chris.fin_shark.m06.repository.StockRepository;
import com.chris.fin_shark.m06.repository.TradingCalendarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 股價同步服務
 * <p>
 * 功能編號: F-M06-002
 * 功能名稱: 股價資料同步
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockPriceSyncService {

    private final TwseApiClient twseApiClient;
    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final TradingCalendarRepository tradingCalendarRepository;
    private final JobExecutionRepository jobExecutionRepository;

    /**
     * 同步指定日期的所有股票股價
     *
     * @param tradeDate   交易日期
     * @param triggerType 觸發類型
     * @return Job 執行記錄
     */
    @Transactional
    public JobExecution syncStockPricesForDate(LocalDate tradeDate, TriggerType triggerType) {
        log.info("開始同步股價資料: tradeDate={}, triggerType={}", tradeDate, triggerType.getDescription());

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
            execution.setTotalItems(activeStocks.size());
            log.info("查詢到 {} 檔活躍股票", activeStocks.size());

            int successCount = 0;
            int failCount = 0;

            // 4. 逐一同步每檔股票
            for (Stock stock : activeStocks) {
                try {
                    syncSingleStock(stock.getStockId(), tradeDate);
                    successCount++;
                    log.debug("股票同步成功: {}", stock.getStockId());

                } catch (Exception e) {
                    failCount++;
                    log.error("股票同步失敗: stockId={}", stock.getStockId(), e);
                }

                // 更新進度
                execution.setProcessedItems(successCount + failCount);
                jobExecutionRepository.save(execution);

                // 避免 API 限流，每次請求間隔 100ms
                Thread.sleep(100);
            }

            // 5. 計算執行時長
            long durationMs = java.time.Duration.between(
                    execution.getStartTime(), LocalDateTime.now()).toMillis();

            // 6. 更新執行結果
            execution.setSuccessItems(successCount);
            execution.setFailedItems(failCount);
            execution.setJobStatus(JobStatus.SUCCESS.getCode());
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationMs(durationMs);

            log.info("股價同步完成: 成功 {}, 失敗 {}, 耗時 {}ms",
                    successCount, failCount, durationMs);

        } catch (Exception e) {
            log.error("股價同步發生嚴重錯誤", e);

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
     * 同步單一股票的股價資料
     */
    private void syncSingleStock(String stockId, LocalDate tradeDate) {
        log.debug("開始同步股票: stockId={}, date={}", stockId, tradeDate);

        // 1. 呼叫外部 API
        List<TwseStockPriceData> monthlyData = twseApiClient.getStockMonthlyPrices(
                stockId, tradeDate);

        if (monthlyData.isEmpty()) {
            log.warn("TWSE API 無回應資料: stockId={}, date={}", stockId, tradeDate);
            return;
        }

        // 2. 過濾出指定日期的資料
        TwseStockPriceData dayData = monthlyData.stream()
                .filter(data -> data.getTradeDate().equals(tradeDate))
                .findFirst()
                .orElse(null);

        if (dayData == null) {
            log.warn("指定日期無股價資料: stockId={}, date={}", stockId, tradeDate);
            return;
        }

        // 3. 轉換並儲存
        StockPrice stockPrice = convertToEntity(stockId, dayData);
        stockPriceRepository.save(stockPrice);

        log.info("股價儲存成功: stockId={}, date={}, close={}",
                stockId, tradeDate, dayData.getClosePrice());
    }

    /**
     * 將外部 API 資料轉換為 Entity
     */
    private StockPrice convertToEntity(String stockId, TwseStockPriceData data) {
        StockPrice entity = new StockPrice();

        // 基本資訊
        entity.setStockId(stockId);
        entity.setTradeDate(data.getTradeDate());

        // 股價資訊
        entity.setOpenPrice(data.getOpenPrice());
        entity.setHighPrice(data.getHighPrice());
        entity.setLowPrice(data.getLowPrice());
        entity.setClosePrice(data.getClosePrice());

        // 成交資訊
        entity.setVolume(data.getVolume());
        entity.setTurnover(data.getTurnover());
        entity.setTransactions(data.getTransactions());

        // 漲跌資訊
        entity.setChangePrice(data.getChangePrice());

        // 計算漲跌幅
        BigDecimal changePercent = calculateChangePercent(
                data.getClosePrice(), data.getChangePrice());
        entity.setChangePercent(changePercent);

        return entity;
    }

    /**
     * 計算漲跌幅（%）
     */
    private BigDecimal calculateChangePercent(BigDecimal closePrice, BigDecimal changePrice) {
        if (changePrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal previousClose = closePrice.subtract(changePrice);

        if (previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return changePrice
                .divide(previousClose, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 建立 Job 執行記錄
     */
    private JobExecution createJobExecution(LocalDate tradeDate, TriggerType triggerType) {
        JobExecution execution = new JobExecution();
        execution.setJobName("StockPriceSync");
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
        execution.setJobName("StockPriceSync");
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



    /**
     * 同步指定月份的所有股票股價（整月）
     */
    @Transactional
    public JobExecution syncStockPricesForMonth(LocalDate monthDate, TriggerType triggerType) {
        log.info("開始同步股價資料(整月): month={}, triggerType={}", monthDate, triggerType.getDescription());

        // 建立 Job 執行記錄
        JobExecution execution = createJobExecution(monthDate, triggerType);

        try {
            // 查詢所有活躍股票
            List<Stock> activeStocks = stockRepository.findByIsActiveTrue();
            execution.setTotalItems(activeStocks.size());
            log.info("查詢到 {} 檔活躍股票 (整月同步)", activeStocks.size());

            int successCount = 0;
            int failCount = 0;

            // 逐一同步每檔股票整個月份
            for (Stock stock : activeStocks) {
                try {
                    syncSingleStockForMonth(stock.getStockId(), monthDate);
                    successCount++;
                    log.debug("股票整月同步成功: {}", stock.getStockId());

                } catch (Exception e) {
                    failCount++;
                    log.error("股票整月同步失敗: stockId={}", stock.getStockId(), e);
                }

                // 更新進度
                execution.setProcessedItems(successCount + failCount);
                // 視需求決定是否每次都存 JOBE
                // jobExecutionRepository.save(execution);

                // 避免 API 限流
                Thread.sleep(100);
            }

            long durationMs = java.time.Duration.between(
                    execution.getStartTime(), LocalDateTime.now()).toMillis();

            execution.setSuccessItems(successCount);
            execution.setFailedItems(failCount);
            execution.setJobStatus(JobStatus.SUCCESS.getCode());
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationMs(durationMs);

            log.info("股價整月同步完成: 成功 {}, 失敗 {}, 耗時 {}ms",
                    successCount, failCount, durationMs);

        } catch (Exception e) {
            log.error("股價整月同步發生嚴重錯誤", e);

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
     * 同步單一股票「整個月份」的股價資料
     */
    private void syncSingleStockForMonth(String stockId, LocalDate monthDate) {
        log.debug("開始同步股票整月股價: stockId={}, month={}", stockId, monthDate);

        // 1. 呼叫外部 API（回傳該月份所有交易日）
        List<TwseStockPriceData> monthlyData = twseApiClient.getStockMonthlyPrices(
                stockId, monthDate);

        if (monthlyData.isEmpty()) {
            log.warn("TWSE API 無回應資料(整月): stockId={}, month={}", stockId, monthDate);
            return;
        }

        // 2. 逐日轉換並儲存
        for (TwseStockPriceData dayData : monthlyData) {
            try {
                StockPrice stockPrice = convertToEntity(stockId, dayData);
                stockPriceRepository.save(stockPrice);

                log.info("股價儲存成功: stockId={}, date={}, close={}",
                        stockId, dayData.getTradeDate(), dayData.getClosePrice());
            } catch (Exception e) {
                log.error("股價儲存失敗: stockId={}, date={}",
                        stockId, dayData.getTradeDate(), e);
            }
        }
    }

}

