package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.common.dto.job.JobExecutionDTO;
import com.chris.fin_shark.common.dto.job.JobStatusDTO;
import com.chris.fin_shark.m06.converter.JobExecutionConverter;
import com.chris.fin_shark.m06.service.JobManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Job 管理 Controller
 * <p>
 * 功能編號: F-M06-008
 * 功能名稱: 資料更新排程
 * 提供 Job 執行記錄查詢、手動觸發等功能
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/jobs")
@Slf4j
@RequiredArgsConstructor
public class JobManagementController {

    private final JobManagementService jobManagementService;
    private final JobExecutionConverter jobExecutionConverter;

    /**
     * 分頁查詢 Job 執行記錄
     *
     * @param jobName Job 名稱（可選）
     * @param jobStatus Job 狀態（可選）
     * @param page 頁碼
     * @param size 每頁筆數
     * @return Job 執行記錄分頁
     */
    @GetMapping("/executions")
    public ApiResponse<PageResponse<JobExecutionDTO>> queryExecutions(
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) String jobStatus,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("GET /api/jobs/executions?jobName={}&jobStatus={}&page={}&size={}",
                jobName, jobStatus, page, size);

        PageResponse<JobExecutionDTO> pageResponse = jobManagementService.queryExecutions(jobName, jobStatus, page, size);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 查詢單一 Job 執行記錄詳情
     *
     * @param executionId 執行 ID
     * @return Job 執行記錄詳情
     */
    @GetMapping("/executions/{executionId}")
    public ApiResponse<JobExecutionDTO> getExecutionById(@PathVariable Long executionId) {
        log.info("GET /api/jobs/executions/{}", executionId);

        JobExecutionDTO execution = jobManagementService.getExecutionById(executionId);
        return ApiResponse.success(execution);
    }

    /**
     * 查詢 Job 狀態總覽
     *
     * @return Job 狀態統計
     */
    @GetMapping("/status")
    public ApiResponse<JobStatusDTO> getJobStatus() {
        log.info("GET /api/jobs/status");

        JobStatusDTO status = jobManagementService.getJobStatus();
        return ApiResponse.success(status);
    }

    /**
     * 手動觸發股價同步 Job
     *
     * @param tradeDate 交易日期（可選，預設今天）
     * @return 執行結果
     */
    @PostMapping("/trigger/stock-price-sync")
    public ApiResponse<JobExecutionDTO> triggerStockPriceSync(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate) {

        LocalDate targetDate = tradeDate != null ? tradeDate : LocalDate.now();
        log.info("POST /api/jobs/trigger/stock-price-sync?tradeDate={}", targetDate);

        JobExecutionDTO execution = jobManagementService.triggerStockPriceSync(targetDate);
        return ApiResponse.success(execution);
    }


    /**
     * 手動觸發法人買賣超同步
     *
     * @param tradeDate 交易日期
     * @return Job 執行記錄
     */
    @PostMapping("/trigger/institutional-sync")
    public ApiResponse<JobExecutionDTO> triggerInstitutionalSync(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate) {

        log.info("POST /api/jobs/trigger/institutional-sync?tradeDate={}", tradeDate);

        JobExecution execution = jobManagementService.triggerInstitutionalSync(tradeDate);
        return ApiResponse.success(jobExecutionConverter.toDTO(execution));
    }

    /**
     * 手動觸發融資融券同步
     *
     * @param tradeDate 交易日期
     * @return Job 執行記錄
     */
    @PostMapping("/trigger/margin-sync")
    public ApiResponse<JobExecutionDTO> triggerMarginSync(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate) {

        log.info("POST /api/jobs/trigger/margin-sync?tradeDate={}", tradeDate);

        JobExecution execution = jobManagementService.triggerMarginSync(tradeDate);
        return ApiResponse.success(jobExecutionConverter.toDTO(execution));
    }

    /**
     * 手動觸發財報同步
     *
     * @param year    年度
     * @param quarter 季度
     * @return Job 執行記錄
     */
    @PostMapping("/trigger/financial-sync")
    public ApiResponse<JobExecutionDTO> triggerFinancialSync(
            @RequestParam Integer year,
            @RequestParam Short quarter) {

        log.info("POST /api/jobs/trigger/financial-sync?year={}&quarter={}", year, quarter);

        JobExecution execution = jobManagementService.triggerFinancialSync(year, quarter);
        return ApiResponse.success(jobExecutionConverter.toDTO(execution));
    }

    /**
     * 手動觸發資料品質檢核
     *
     * @return Job 執行記錄
     */
    @PostMapping("/trigger/quality-check")
    public ApiResponse<JobExecutionDTO> triggerQualityCheck() {

        log.info("POST /api/jobs/trigger/quality-check");

        JobExecution execution = jobManagementService.triggerQualityCheck();
        return ApiResponse.success(jobExecutionConverter.toDTO(execution));
    }


    /**
     * 手動觸發「整個月份」股價同步 Job
     *
     * @param monthDate 任何該月日期（可選，預設今天）
     */
    @PostMapping("/trigger/stock-price-sync-month")
    public ApiResponse<JobExecutionDTO> triggerStockPriceSyncForMonth(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate monthDate) {

        LocalDate target = (monthDate != null ? monthDate : LocalDate.now());
        log.info("POST /api/jobs/trigger/stock-price-sync-month?month={}", target);

        JobExecutionDTO execution =
                jobManagementService.triggerStockPriceSyncForMonth(target);

        return ApiResponse.success(execution);
    }


}
