package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.common.domain.JobExecution;
import com.chris.fin_shark.common.enums.JobStatus;
import com.chris.fin_shark.common.enums.JobType;
import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.dto.QualityCheckResultDTO;
import com.chris.fin_shark.m06.dto.request.QualityCheckExecuteRequest;
import com.chris.fin_shark.m06.enums.QualityCheckType;
import com.chris.fin_shark.m06.mapper.DataQualityMapper;
import com.chris.fin_shark.m06.repository.DataQualityCheckRepository;
import com.chris.fin_shark.m06.repository.DataQualityIssueRepository;
import com.chris.fin_shark.m06.repository.JobExecutionRepository;
import com.chris.fin_shark.m06.vo.MissingDataVO;
import com.chris.fin_shark.m06.vo.QualityCheckExecutionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 資料品質檢核執行服務
 * <p>
 * 功能編號: F-M06-006
 * 功能名稱: 資料品質檢核
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataQualityExecutionService {

    private final DataQualityMapper dataQualityMapper;
    private final DataQualityCheckRepository dataQualityCheckRepository;
    private final DataQualityIssueRepository dataQualityIssueRepository;
    private final JobExecutionRepository jobExecutionRepository;

    /**
     * 執行品質檢核
     *
     * @param request     檢核請求
     * @param triggerType 觸發類型
     * @return 檢核結果
     */
    @Transactional
    public QualityCheckResultDTO executeQualityCheck(QualityCheckExecuteRequest request, TriggerType triggerType) {
        log.info("開始執行品質檢核: request={}, triggerType={}", request, triggerType.getDescription());

        LocalDateTime startTime = LocalDateTime.now();
        List<Map<String, Object>> details = new ArrayList<>();
        int totalIssues = 0;

        try {
            // 決定檢核日期範圍
            LocalDate startDate = request.getStartDate() != null ?
                    request.getStartDate() : LocalDate.now().minusDays(7);
            LocalDate endDate = request.getEndDate() != null ?
                    request.getEndDate() : LocalDate.now();

            // 決定要執行的檢核類型
            List<String> checkTypes = request.getCheckTypes();
            if (checkTypes == null || checkTypes.isEmpty()) {
                checkTypes = List.of(
                        QualityCheckType.COMPLETENESS.getCode(),
                        QualityCheckType.CONSISTENCY.getCode(),
                        QualityCheckType.TIMELINESS.getCode()
                );
            }

            // 執行各類型檢核
            for (String checkType : checkTypes) {
                Map<String, Object> checkResult = executeCheckByType(checkType, startDate, endDate);
                details.add(checkResult);
                totalIssues += (Integer) checkResult.getOrDefault("issue_count", 0);
            }

            // 計算品質分數
            int qualityScore = calculateQualityScore(totalIssues);

            // 建立結果
            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

            return QualityCheckResultDTO.builder()
                    .checkName("FullQualityCheck")
                    .checkType("ALL")
                    .status("SUCCESS")
                    .result(totalIssues == 0 ? "PASS" : "FAIL")
                    .issueCount(totalIssues)
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationMs(durationMs)
                    .details(details)
                    .qualityScore(qualityScore)
                    .summary(buildSummary(details, totalIssues, qualityScore))
                    .build();

        } catch (Exception e) {
            log.error("品質檢核執行失敗", e);

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

            return QualityCheckResultDTO.builder()
                    .checkName("FullQualityCheck")
                    .checkType("ALL")
                    .status("FAILED")
                    .result("ERROR")
                    .startTime(startTime)
                    .endTime(endTime)
                    .durationMs(durationMs)
                    .errorMessage(e.getMessage())
                    .build();
        }
    }

    /**
     * 依類型執行檢核
     */
    private Map<String, Object> executeCheckByType(String checkType, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> result = new HashMap<>();
        result.put("check_type", checkType);
        result.put("start_date", startDate.toString());
        result.put("end_date", endDate.toString());

        try {
            int issueCount = 0;
            List<?> issues;

            switch (QualityCheckType.fromCode(checkType)) {
                case COMPLETENESS:
                    // 檢核股價完整性
                    List<MissingDataVO> stockPriceMissing = dataQualityMapper.checkStockPriceCompleteness(startDate, endDate);
                    List<MissingDataVO> institutionalMissing = dataQualityMapper.checkInstitutionalCompleteness(startDate, endDate);
                    List<MissingDataVO> marginMissing = dataQualityMapper.checkMarginCompleteness(startDate, endDate);

                    issueCount = stockPriceMissing.size() + institutionalMissing.size() + marginMissing.size();
                    result.put("stock_price_missing", stockPriceMissing.size());
                    result.put("institutional_missing", institutionalMissing.size());
                    result.put("margin_missing", marginMissing.size());
                    break;

                case CONSISTENCY:
                    // 檢核股價四價關係
                    List<QualityCheckExecutionVO> priceViolations = dataQualityMapper.checkPriceRelationship(startDate, endDate);
                    issueCount = priceViolations.size();
                    result.put("price_violations", issueCount);
                    break;

                case TIMELINESS:
                    // 檢核資料時效性
                    QualityCheckExecutionVO stockPriceTimeliness = dataQualityMapper.checkDataTimeliness("stock_prices");
                    result.put("stock_prices_latest", stockPriceTimeliness != null ? stockPriceTimeliness.getActualValue() : "N/A");
                    break;

                default:
                    log.warn("未支援的檢核類型: {}", checkType);
            }

            result.put("issue_count", issueCount);
            result.put("status", "SUCCESS");

        } catch (Exception e) {
            log.error("執行檢核類型 {} 失敗", checkType, e);
            result.put("status", "FAILED");
            result.put("error", e.getMessage());
            result.put("issue_count", 0);
        }

        return result;
    }

    /**
     * 計算品質分數
     */
    private int calculateQualityScore(int totalIssues) {
        // 簡單計分規則：100 - (問題數 * 2)，最低 0 分
        int score = 100 - (totalIssues * 2);
        return Math.max(0, Math.min(100, score));
    }

    /**
     * 建立摘要
     */
    private Map<String, Object> buildSummary(List<Map<String, Object>> details, int totalIssues, int qualityScore) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("total_checks", details.size());
        summary.put("total_issues", totalIssues);
        summary.put("quality_score", qualityScore);
        summary.put("quality_level", getQualityLevel(qualityScore));
        return summary;
    }

    /**
     * 取得品質等級
     */
    private String getQualityLevel(int score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 80) return "GOOD";
        if (score >= 60) return "FAIR";
        if (score >= 40) return "POOR";
        return "CRITICAL";
    }

    /**
     * 排程執行品質檢核
     *
     * @param triggerType 觸發類型
     * @return Job 執行記錄
     */
    @Transactional
    public JobExecution runScheduledQualityCheck(TriggerType triggerType) {
        log.info("排程執行品質檢核: triggerType={}", triggerType.getDescription());

        JobExecution execution = createJobExecution(triggerType);

        try {
            QualityCheckExecuteRequest request = QualityCheckExecuteRequest.builder()
                    .startDate(LocalDate.now().minusDays(7))
                    .endDate(LocalDate.now())
                    .activeStocksOnly(true)
                    .recordIssues(true)
                    .build();

            QualityCheckResultDTO result = executeQualityCheck(request, triggerType);

            execution.setSuccessItems(result.getIssueCount() == 0 ? 1 : 0);
            execution.setFailedItems(result.getIssueCount() > 0 ? 1 : 0);
            execution.setJobStatus(JobStatus.SUCCESS.getCode());
            execution.setEndTime(LocalDateTime.now());
            execution.setDurationMs(result.getDurationMs());

        } catch (Exception e) {
            log.error("排程品質檢核失敗", e);
            execution.setJobStatus(JobStatus.FAILED.getCode());
            execution.setErrorMessage(e.getMessage());
            execution.setEndTime(LocalDateTime.now());
        }

        return jobExecutionRepository.save(execution);
    }

    /**
     * 建立 Job 執行記錄
     */
    private JobExecution createJobExecution(TriggerType triggerType) {
        JobExecution execution = new JobExecution();
        execution.setJobName("DataQualityCheck");
        execution.setJobType(JobType.DATA_QUALITY.getCode());
        execution.setJobStatus(JobStatus.RUNNING.getCode());
        execution.setStartTime(LocalDateTime.now());
        execution.setTriggerType(triggerType.getCode());
        execution.setRetryCount(0);
        execution.setMaxRetry(1);

        return jobExecutionRepository.save(execution);
    }
}
