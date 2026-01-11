package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.common.enums.ErrorCode;
import com.chris.fin_shark.common.exception.BusinessException;
import com.chris.fin_shark.common.exception.DataNotFoundException;
import com.chris.fin_shark.common.exception.DataValidationException;
import com.chris.fin_shark.common.enums.JobStatus;
import com.chris.fin_shark.m06.converter.JobExecutionConverter;
import com.chris.fin_shark.common.dto.job.JobExecutionDTO;
import com.chris.fin_shark.common.dto.job.JobStatusDTO;
import com.chris.fin_shark.m06.job.*;
import com.chris.fin_shark.m06.repository.JobExecutionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Job 管理服務
 * <p>
 * 功能編號: F-M06-008
 * 功能名稱: 資料更新排程
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JobManagementService {

    private final JobExecutionRepository jobExecutionRepository;
    private final JobExecutionConverter jobExecutionConverter;
    private final StockPriceSyncJob stockPriceSyncJob;
    private final InstitutionalTradingSyncJob institutionalTradingSyncJob;
    private final MarginTradingSyncJob marginTradingSyncJob;
    private final FinancialStatementSyncJob financialStatementSyncJob;
    private final DataQualityCheckJob dataQualityCheckJob;

    /**
     * 分頁查詢 Job 執行記錄
     *
     * @param jobName   Job 名稱（可選）
     * @param jobStatus Job 狀態（可選）
     * @param page      頁碼（從 1 開始）
     * @param size      每頁筆數
     * @return Job 執行記錄分頁
     */
    @Transactional(readOnly = true)
    public PageResponse<JobExecutionDTO> queryExecutions(
            String jobName, String jobStatus, Integer page, Integer size) {

        log.debug("查詢 Job 執行記錄: jobName={}, jobStatus={}, page={}, size={}",
                jobName, jobStatus, page, size);
        if (page == null || page < 1) {
            throw DataValidationException.validationFailed("page", "page must be >= 1");
        }
        if (size == null || size < 1) {
            throw DataValidationException.validationFailed("size", "size must be >= 1");
        }



        // 建立分頁請求（頁碼從 0 開始）
        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "startTime")
        );

        // 查詢資料
        Page<JobExecution> executionPage;
        if (jobName != null) {
            executionPage = jobExecutionRepository.findByJobNameOrderByStartTimeDesc(jobName, pageable);
        } else {
            executionPage = jobExecutionRepository.findAll(pageable);
        }

        // 轉換為 DTO
        List<JobExecutionDTO> dtoList = jobExecutionConverter.toDTOList(executionPage.getContent());

        return PageResponse.of(dtoList, page, size, executionPage.getTotalElements());
    }

    /**
     * 查詢單一 Job 執行記錄
     *
     * @param executionId 執行 ID
     * @return Job 執行記錄
     */
    @Transactional(readOnly = true)
    public JobExecutionDTO getExecutionById(Long executionId) {
        log.debug("查詢 Job 執行記錄: executionId={}", executionId);

        JobExecution execution = jobExecutionRepository.findById(executionId)
                .orElseThrow(() -> new DataNotFoundException(
                        ErrorCode.RESOURCE_NOT_FOUND,
                        "Job ???????",
                        "Job execution with ID '" + executionId + "' does not exist",
                        "execution_id"
                ));

        return jobExecutionConverter.toDTO(execution);
    }

    /**
     * 查詢 Job 狀態總覽
     *
     * @return Job 狀態統計
     */
    @Transactional(readOnly = true)
    public JobStatusDTO getJobStatus() {
        log.debug("查詢 Job 狀態總覽");

        // 查詢執行中的 Job
        List<JobExecution> runningJobs = jobExecutionRepository.findRunningJobs();

        // 查詢今日開始時間
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);

        // 查詢今日失敗的 Job
        List<JobExecution> failedJobs = jobExecutionRepository.findFailedJobsSince(todayStart);

        // 統計今日成功的 Job
        long todaySuccessJobs = jobExecutionRepository.countByJobNameAndJobStatus(
                null, JobStatus.SUCCESS.getCode());

        // 建立執行中的 Job 資訊列表
        List<JobStatusDTO.RunningJobInfo> runningJobList = runningJobs.stream()
                .map(job -> JobStatusDTO.RunningJobInfo.builder()
                        .executionId(job.getExecutionId())
                        .jobName(job.getJobName())
                        .startTime(job.getStartTime().toString())
                        .processedItems(job.getProcessedItems())
                        .build())
                .collect(Collectors.toList());

        return JobStatusDTO.builder()
                .runningJobs(runningJobs.size())
                .todaySuccessJobs((int) todaySuccessJobs)
                .todayFailedJobs(failedJobs.size())
                .runningJobList(runningJobList)
                .build();
    }

    /**
     * 手動觸發股價同步 Job
     *
     * @param tradeDate 交易日期
     * @return Job 執行記錄
     */
    @Transactional
    public JobExecutionDTO triggerStockPriceSync(LocalDate tradeDate) {
        log.info("手動觸發股價同步: tradeDate={}", tradeDate);

        // 呼叫 Job 手動觸發方法
        stockPriceSyncJob.syncStockPricesManually(tradeDate);

        // 查詢最新執行記錄
        JobExecution execution = jobExecutionRepository.findLatestByJobName("StockPriceSync")
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_ERROR,
                        "??? Job ????",
                        "No job execution record found after triggering StockPriceSync"
                ));

        return jobExecutionConverter.toDTO(execution);
    }

    /**
     * 觸發法人買賣超同步
     *
     * @param tradeDate 交易日期
     * @return Job 執行記錄
     */
    public JobExecution triggerInstitutionalSync(LocalDate tradeDate) {
        log.info("手動觸發法人買賣超同步: tradeDate={}", tradeDate);
        institutionalTradingSyncJob.syncInstitutionalTradingManually(tradeDate);
        // 查詢並返回最新的執行記錄
        return jobExecutionRepository.findLatestByJobName("InstitutionalTradingSync")
                .orElse(null);
    }

    /**
     * 觸發融資融券同步
     *
     * @param tradeDate 交易日期
     * @return Job 執行記錄
     */
    public JobExecution triggerMarginSync(LocalDate tradeDate) {
        log.info("手動觸發融資融券同步: tradeDate={}", tradeDate);
        marginTradingSyncJob.syncMarginTradingManually(tradeDate);
        return jobExecutionRepository.findLatestByJobName("MarginTradingSync")
                .orElse(null);
    }

    /**
     * 觸發財報同步
     *
     * @param year    年度
     * @param quarter 季度
     * @return Job 執行記錄
     */
    public JobExecution triggerFinancialSync(int year, short quarter) {
        log.info("手動觸發財報同步: year={}, quarter={}", year, quarter);
        financialStatementSyncJob.syncFinancialStatementsManually(year, quarter);
        return jobExecutionRepository.findLatestByJobName("FinancialStatementSync")
                .orElse(null);
    }

    /**
     * 觸發資料品質檢核
     *
     * @return Job 執行記錄
     */
    public JobExecution triggerQualityCheck() {
        log.info("手動觸發資料品質檢核");
        dataQualityCheckJob.runQualityCheckManually();
        return jobExecutionRepository.findLatestByJobName("DataQualityCheck")
                .orElse(null);
    }


    /**
     * 手動觸發「整個月份」股價同步 Job
     *
     * @param monthDate 任一該月日期（會自動轉為該月第一天）
     * @return Job 執行記錄
     */
    @Transactional
    public JobExecutionDTO triggerStockPriceSyncForMonth(LocalDate monthDate) {
        LocalDate targetMonth = monthDate.withDayOfMonth(1);
        log.info("手動觸發整月股價同步: month={}", targetMonth);

        // 1️⃣ 呼叫 Job
        stockPriceSyncJob.syncStockPricesForMonthManually(targetMonth);

        // 2️⃣ 取得最新 Job 記錄
        JobExecution execution = jobExecutionRepository.findLatestByJobName("StockPriceSync")
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INTERNAL_ERROR,
                        "StockPriceSync 整月 Job 執行記錄不存在",
                        "No job execution record found after triggering StockPriceSync (monthly)"
                ));

        return jobExecutionConverter.toDTO(execution);
    }


}
