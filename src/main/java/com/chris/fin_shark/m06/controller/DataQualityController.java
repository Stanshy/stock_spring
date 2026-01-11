package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m06.dto.DataQualityCheckDTO;
import com.chris.fin_shark.m06.dto.DataQualityIssueDTO;
import com.chris.fin_shark.m06.dto.DataQualitySummaryDTO;
import com.chris.fin_shark.m06.dto.QualityCheckResultDTO;
import com.chris.fin_shark.m06.dto.request.QualityCheckExecuteRequest;
import com.chris.fin_shark.m06.service.DataQualityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 資料品質 Controller
 * <p>
 * 功能編號: F-M06-006
 * 功能名稱: 資料品質檢核
 * 提供品質檢核規則、問題記錄查詢等功能
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/data-quality")
@Slf4j
@RequiredArgsConstructor
public class DataQualityController {

    private final DataQualityService dataQualityService;

    /**
     * 查詢所有啟用的檢核規則
     *
     * @return 檢核規則列表
     */
    @GetMapping("/checks")
    public ApiResponse<List<DataQualityCheckDTO>> getActiveChecks() {
        log.info("GET /api/data-quality/checks");

        List<DataQualityCheckDTO> checks = dataQualityService.getActiveChecks();
        return ApiResponse.success(checks);
    }

    /**
     * 查詢指定目標表的檢核規則
     *
     * @param targetTable 目標表名稱
     * @return 檢核規則列表
     */
    @GetMapping("/checks/{targetTable}")
    public ApiResponse<List<DataQualityCheckDTO>> getChecksByTargetTable(
            @PathVariable String targetTable) {

        log.info("GET /api/data-quality/checks/{}", targetTable);

        List<DataQualityCheckDTO> checks = dataQualityService.getChecksByTargetTable(targetTable);
        return ApiResponse.success(checks);
    }

    /**
     * 分頁查詢品質問題
     *
     * @param status 狀態（可選）
     * @param severity 嚴重性（可選）
     * @param page 頁碼
     * @param size 每頁筆數
     * @return 品質問題分頁
     */
    @GetMapping("/issues")
    public ApiResponse<PageResponse<DataQualityIssueDTO>> queryIssues(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String severity,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("GET /api/data-quality/issues?status={}&severity={}&page={}&size={}",
                status, severity, page, size);

        PageResponse<DataQualityIssueDTO> pageResponse = dataQualityService.queryIssues(status, severity, page, size);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 查詢未解決的品質問題
     *
     * @param page 頁碼
     * @param size 每頁筆數
     * @return 未解決問題分頁
     */
    @GetMapping("/issues/open")
    public ApiResponse<PageResponse<DataQualityIssueDTO>> getOpenIssues(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("GET /api/data-quality/issues/open?page={}&size={}", page, size);

        PageResponse<DataQualityIssueDTO> pageResponse = dataQualityService.getOpenIssues(page, size);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 查詢品質問題詳情
     *
     * @param issueId 問題 ID
     * @return 問題詳情
     */
    @GetMapping("/issues/{issueId}")
    public ApiResponse<DataQualityIssueDTO> getIssueById(@PathVariable Long issueId) {
        log.info("GET /api/data-quality/issues/{}", issueId);

        DataQualityIssueDTO issue = dataQualityService.getIssueById(issueId);
        return ApiResponse.success(issue);
    }

    /**
     * 查詢資料品質統計摘要
     *
     * @return 統計摘要
     */
    @GetMapping("/summary")
    public ApiResponse<DataQualitySummaryDTO> getQualitySummary() {
        log.info("GET /api/data-quality/summary");

        DataQualitySummaryDTO summary = dataQualityService.getQualitySummary();
        return ApiResponse.success(summary);
    }

    /**
     * 手動觸發品質檢核
     *
     * @param request 檢核請求
     * @return 檢核結果
     */
    @PostMapping("/run-check")
    public ApiResponse<QualityCheckResultDTO> runQualityCheck(
            @Valid @RequestBody QualityCheckExecuteRequest request) {

        log.info("POST /api/data-quality/run-check: {}", request);

        QualityCheckResultDTO result = dataQualityService.runQualityCheck(request);
        return ApiResponse.success(result);
    }
}
