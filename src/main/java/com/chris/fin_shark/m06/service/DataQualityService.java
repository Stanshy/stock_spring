package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.converter.DataQualityCheckConverter;
import com.chris.fin_shark.m06.converter.DataQualityIssueConverter;
import com.chris.fin_shark.m06.domain.DataQualityCheck;
import com.chris.fin_shark.m06.domain.DataQualityIssue;
import com.chris.fin_shark.m06.dto.DataQualityCheckDTO;
import com.chris.fin_shark.m06.dto.DataQualityIssueDTO;
import com.chris.fin_shark.m06.dto.DataQualitySummaryDTO;
import com.chris.fin_shark.m06.dto.QualityCheckResultDTO;
import com.chris.fin_shark.m06.dto.request.QualityCheckExecuteRequest;
import com.chris.fin_shark.m06.repository.DataQualityCheckRepository;
import com.chris.fin_shark.m06.repository.DataQualityIssueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 資料品質服務
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
public class DataQualityService {

    private final DataQualityCheckRepository checkRepository;
    private final DataQualityIssueRepository issueRepository;
    private final DataQualityCheckConverter checkConverter;
    private final DataQualityIssueConverter issueConverter;
    private final DataQualityExecutionService dataQualityExecutionService;

    /**
     * 查詢所有啟用的檢核規則
     *
     * @return 檢核規則列表
     */
    @Transactional(readOnly = true)
    public List<DataQualityCheckDTO> getActiveChecks() {
        log.debug("查詢啟用的檢核規則");

        List<DataQualityCheck> checks = checkRepository.findByIsActiveTrue();
        return checkConverter.toDTOList(checks);
    }

    /**
     * 根據目標表查詢檢核規則
     *
     * @param targetTable 目標表
     * @return 檢核規則列表
     */
    @Transactional(readOnly = true)
    public List<DataQualityCheckDTO> getChecksByTargetTable(String targetTable) {
        log.debug("查詢目標表檢核規則: targetTable={}", targetTable);

        List<DataQualityCheck> checks = checkRepository.findActiveChecksByTargetTable(targetTable);
        return checkConverter.toDTOList(checks);
    }

    /**
     * 分頁查詢品質問題
     *
     * @param status   狀態
     * @param severity 嚴重性
     * @param page     頁碼
     * @param size     每頁筆數
     * @return 品質問題分頁
     */
    @Transactional(readOnly = true)
    public PageResponse<DataQualityIssueDTO> queryIssues(String status, String severity,
                                                         Integer page, Integer size) {
        log.debug("查詢品質問題: status={}, severity={}, page={}, size={}",
                status, severity, page, size);

        // TODO: 實作按條件查詢邏輯
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<DataQualityIssue> issuePage = issueRepository.findAll(pageable);

        List<DataQualityIssueDTO> dtoList = issueConverter.toDTOList(issuePage.getContent());
        return PageResponse.of(dtoList, page, size, issuePage.getTotalElements());
    }

    /**
     * 查詢未解決的品質問題
     *
     * @param page 頁碼
     * @param size 每頁筆數
     * @return 未解決問題分頁
     */
    @Transactional(readOnly = true)
    public PageResponse<DataQualityIssueDTO> getOpenIssues(Integer page, Integer size) {
        log.debug("查詢未解決問題: page={}, size={}", page, size);

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<DataQualityIssue> issuePage = issueRepository.findOpenIssues(pageable);

        List<DataQualityIssueDTO> dtoList = issueConverter.toDTOList(issuePage.getContent());
        return PageResponse.of(dtoList, page, size, issuePage.getTotalElements());
    }

    /**
     * 查詢問題詳情
     *
     * @param issueId 問題 ID
     * @return 問題詳情
     */
    @Transactional(readOnly = true)
    public DataQualityIssueDTO getIssueById(Long issueId) {
        log.debug("查詢問題詳情: issueId={}", issueId);

        DataQualityIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found: " + issueId));

        return issueConverter.toDTO(issue);
    }

    /**
     * 查詢資料品質統計摘要
     *
     * @return 統計摘要
     */
    @Transactional(readOnly = true)
    public DataQualitySummaryDTO getQualitySummary() {
        log.debug("查詢資料品質統計");

        long totalActiveChecks = checkRepository.count();
        long totalOpenIssues = issueRepository.countOpenIssues();
        long highSeverityIssues = issueRepository.countBySeverityAndStatus("HIGH", "OPEN");
        long mediumSeverityIssues = issueRepository.countBySeverityAndStatus("MEDIUM", "OPEN");
        long lowSeverityIssues = issueRepository.countBySeverityAndStatus("LOW", "OPEN");

        // 計算品質分數（簡單算法：100 - 問題數的影響）
        int qualityScore = calculateQualityScore(totalOpenIssues, highSeverityIssues);

        return DataQualitySummaryDTO.builder()
                .totalActiveChecks(totalActiveChecks)
                .totalOpenIssues(totalOpenIssues)
                .highSeverityIssues(highSeverityIssues)
                .mediumSeverityIssues(mediumSeverityIssues)
                .lowSeverityIssues(lowSeverityIssues)
                .todayNewIssues(0L)  // TODO: 實作今日統計
                .todayResolvedIssues(0L)  // TODO: 實作今日統計
                .qualityScore(qualityScore)
                .build();
    }

    /**
     * 手動觸發品質檢核
     *
     * @param request 檢核請求
     */
    @Transactional
    public QualityCheckResultDTO runQualityCheck(QualityCheckExecuteRequest request) {
        return dataQualityExecutionService.executeQualityCheck(request, TriggerType.MANUAL);
    }

    /**
     * 計算品質分數
     */
    private int calculateQualityScore(long totalIssues, long highSeverityIssues) {
        // 簡單算法：100 - (高嚴重性問題數 * 10) - (總問題數 * 2)
        int score = (int) (100 - (highSeverityIssues * 10) - (totalIssues * 2));
        return Math.max(0, Math.min(100, score));
    }
}
