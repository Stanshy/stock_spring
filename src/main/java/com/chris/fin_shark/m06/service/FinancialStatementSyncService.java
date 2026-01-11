package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.client.finmind.FinMindClient;
import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.common.dto.external.FinMindFinancialData;
import com.chris.fin_shark.common.enums.JobStatus;
import com.chris.fin_shark.common.enums.JobType;
import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.domain.FinancialStatement;
import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.mapper.FinancialStatementMapper;
import com.chris.fin_shark.m06.repository.FinancialStatementRepository;
import com.chris.fin_shark.m06.repository.JobExecutionRepository;
import com.chris.fin_shark.m06.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * 財務報表同步服務
 * <p>
 * 功能編號: F-M06-003
 * 功能名稱: 財報資料同步
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialStatementSyncService {

    private final FinMindClient finMindClient;
    private final StockRepository stockRepository;
    private final FinancialStatementRepository financialStatementRepository;
    private final FinancialStatementMapper financialStatementMapper;
    private final JobExecutionRepository jobExecutionRepository;

    /**
     * 同步指定年度季度的所有股票財報
     *
     * @param year        年度
     * @param quarter     季度
     * @param triggerType 觸發類型
     * @return Job 執行記錄
     */
    @Transactional
    public JobExecution syncFinancialStatementsForPeriod(int year, short quarter, TriggerType triggerType) {
        log.info("開始同步財報資料: year={}, quarter={}, triggerType={}",
                year, quarter, triggerType.getDescription());

        // 建立 Job 執行記錄
        JobExecution execution = createJobExecution(year, quarter, triggerType);

        try {
            // 查詢所有活躍股票
            List<Stock> activeStocks = stockRepository.findByIsActiveTrue();
            List<String> stockIds = activeStocks.stream()
                    .map(Stock::getStockId)
                    .collect(Collectors.toList());
            execution.setTotalItems(stockIds.size());
            log.info("查詢到 {} 檔活躍股票", stockIds.size());

// 計算查詢日期範圍
            LocalDate startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
            LocalDate endDate = startDate.plusMonths(3).minusDays(1);

            List<FinancialStatement> allEntities = new ArrayList<>();
            int failCount = 0;

// 逐一查詢每檔股票（FinMind 需要逐檔查詢）
            for (String stockId : stockIds) {
                try {
                    List<FinMindFinancialData> dataList = finMindClient.getFinancialStatements(
                            stockId, startDate, endDate);

                    // 找出指定季度的資料
                    FinMindFinancialData data = dataList.stream()
                            .filter(d -> d.getYear() == year && d.getQuarter() == quarter)
                            .findFirst()
                            .orElse(null);

                    if (data != null) {
                        allEntities.add(convertToEntity(data));
                        log.debug("取得財報: {} {}Q{}", stockId, year, quarter);
                    } else {
                        log.warn("未找到財報資料: {} {}Q{}", stockId, year, quarter);
                        failCount++;
                    }

                } catch (Exception e) {
                    failCount++;
                    log.error("財報查詢失敗: stockId={}", stockId, e);
                }

                execution.setProcessedItems(allEntities.size() + failCount);

                // 避免 API 限流
                Thread.sleep(300);
            }

// 批次儲存（UPSERT）
            if (!allEntities.isEmpty()) {
                try {
                    financialStatementMapper.batchInsert(allEntities);
                    execution.setSuccessItems(allEntities.size());
                    log.info("財報批次儲存成功: {} 筆", allEntities.size());
                } catch (Exception e) {
                    log.error("財報批次儲存失敗", e);
                    execution.setSuccessItems(0);
                    failCount += allEntities.size();
                    throw e;
                }
            }

            execution.setFailedItems(failCount);

// 更新執行結果
            long durationMs = java.time.Duration.between(
                    execution.getStartTime(), LocalDateTime.now()).toMillis();

            execution.setJobStatus(JobStatus.SUCCESS.getCode());
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationMs(durationMs);

            log.info("財報同步完成: 成功 {}, 失敗 {}, 耗時 {}ms",
                    allEntities.size(), failCount, durationMs);

        } catch (Exception e) {
            log.error("財報同步發生嚴重錯誤", e);

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
    private FinancialStatement convertToEntity(FinMindFinancialData data) {
        return FinancialStatement.builder()
                .stockId(data.getStockId())
                .year(data.getYear())
                .quarter(data.getQuarter())
                .reportType(data.getReportType() != null ? data.getReportType() : "Q")
                // 損益表
                .revenue(data.getRevenue())
                .grossProfit(data.getGrossProfit())
                .operatingExpense(data.getOperatingExpense())
                .operatingIncome(data.getOperatingIncome())
                .netIncome(data.getNetIncome())
                // 資產負債表
                .totalAssets(data.getTotalAssets())
                .totalLiabilities(data.getTotalLiabilities())
                .equity(data.getEquity())
                .currentAssets(data.getCurrentAssets())
                .currentLiabilities(data.getCurrentLiabilities())
                // 現金流量表
                .operatingCashFlow(data.getOperatingCashFlow())
                .investingCashFlow(data.getInvestingCashFlow())
                .financingCashFlow(data.getFinancingCashFlow())
                // 每股指標
                .eps(data.getEps())
                .bps(data.getBps())
                // 來源
                .source("FinMind")
                .publishDate(data.getDate())
                .build();
    }

//    /**
//     * 同步單一股票指定期間的財報（供手動觸發使用）
//     *
//     * @param stockId 股票代碼
//     * @param year    年度
//     * @param quarter 季度
//     * @return 是否成功
//     */
//    public boolean syncSingleStockFinancial(String stockId, int year, short quarter) {
//        try {
//            syncSingleStock(stockId, year, quarter);
//            return true;
//        } catch (Exception e) {
//            log.error("同步單一股票財報失敗: stockId={}, year={}, quarter={}",
//                    stockId, year, quarter, e);
//            return false;
//        }
//    }

    /**
     * 建立 Job 執行記錄
     */
    private JobExecution createJobExecution(int year, int quarter, TriggerType triggerType) {
        JobExecution execution = new JobExecution();
        execution.setJobName("FinancialStatementSync");
        execution.setJobType(JobType.DATA_SYNC.getCode());
        execution.setJobStatus(JobStatus.RUNNING.getCode());
        execution.setStartTime(LocalDateTime.now());
        execution.setTriggerType(triggerType.getCode());
        execution.setRetryCount(0);
        execution.setMaxRetry(3);

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
