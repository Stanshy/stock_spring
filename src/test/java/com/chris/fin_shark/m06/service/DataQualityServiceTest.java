package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m06.converter.DataQualityCheckConverter;
import com.chris.fin_shark.m06.converter.DataQualityIssueConverter;
import com.chris.fin_shark.m06.domain.DataQualityCheck;
import com.chris.fin_shark.m06.domain.DataQualityIssue;
import com.chris.fin_shark.m06.dto.DataQualityCheckDTO;
import com.chris.fin_shark.m06.dto.DataQualityIssueDTO;
import com.chris.fin_shark.m06.dto.DataQualitySummaryDTO;
import com.chris.fin_shark.m06.repository.DataQualityCheckRepository;
import com.chris.fin_shark.m06.repository.DataQualityIssueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * DataQualityService 單元測試
 * <p>
 * 測試資料品質服務的核心業務邏輯
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("資料品質服務測試")
class DataQualityServiceTest {

    @Mock
    private DataQualityCheckRepository checkRepository;

    @Mock
    private DataQualityIssueRepository issueRepository;

    @Mock
    private DataQualityCheckConverter checkConverter;

    @Mock
    private DataQualityIssueConverter issueConverter;

    @Mock
    private DataQualityExecutionService dataQualityExecutionService;

    @InjectMocks
    private DataQualityService dataQualityService;

    private DataQualityCheck testCheck;
    private DataQualityCheckDTO testCheckDTO;
    private DataQualityIssue testIssue;
    private DataQualityIssueDTO testIssueDTO;

    @BeforeEach
    void setUp() {
        testCheck = new DataQualityCheck();
        testCheck.setCheckId(1L);
        testCheck.setCheckName("四價關係檢查");
        testCheck.setTargetTable("stock_prices");
        testCheck.setIsActive(true);

        testCheckDTO = DataQualityCheckDTO.builder()
                .checkId(1L)
                .checkName("四價關係檢查")
                .targetTable("stock_prices")
                .isActive(true)
                .build();

        testIssue = new DataQualityIssue();
        testIssue.setIssueId(1L);
        testIssue.setSeverity("HIGH");
        testIssue.setStatus("OPEN");
        testIssue.setCreatedAt(LocalDateTime.now());

        testIssueDTO = DataQualityIssueDTO.builder()
                .issueId(1L)
                .severity("HIGH")
                .status("OPEN")
                .build();
    }

    @Test
    @DisplayName("getActiveChecks - 應返回所有啟用的檢核規則")
    void getActiveChecks_shouldReturnActiveChecks() {
        // Given
        when(checkRepository.findByIsActiveTrue()).thenReturn(List.of(testCheck));
        when(checkConverter.toDTOList(any())).thenReturn(List.of(testCheckDTO));

        // When
        List<DataQualityCheckDTO> result = dataQualityService.getActiveChecks();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCheckName()).isEqualTo("四價關係檢查");
        verify(checkRepository).findByIsActiveTrue();
    }

    @Test
    @DisplayName("getActiveChecks - 無啟用規則時應返回空列表")
    void getActiveChecks_shouldReturnEmptyWhenNoActiveChecks() {
        // Given
        when(checkRepository.findByIsActiveTrue()).thenReturn(Collections.emptyList());
        when(checkConverter.toDTOList(any())).thenReturn(Collections.emptyList());

        // When
        List<DataQualityCheckDTO> result = dataQualityService.getActiveChecks();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getChecksByTargetTable - 應返回指定目標表的檢核規則")
    void getChecksByTargetTable_shouldReturnChecksByTargetTable() {
        // Given
        when(checkRepository.findActiveChecksByTargetTable("stock_prices"))
                .thenReturn(List.of(testCheck));
        when(checkConverter.toDTOList(any())).thenReturn(List.of(testCheckDTO));

        // When
        List<DataQualityCheckDTO> result = dataQualityService.getChecksByTargetTable("stock_prices");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTargetTable()).isEqualTo("stock_prices");
    }

    @Test
    @DisplayName("queryIssues - 應返回分頁的品質問題")
    void queryIssues_shouldReturnPagedIssues() {
        // Given
        Page<DataQualityIssue> issuePage = new PageImpl<>(List.of(testIssue));
        when(issueRepository.findAll(any(Pageable.class))).thenReturn(issuePage);
        when(issueConverter.toDTOList(any())).thenReturn(List.of(testIssueDTO));

        // When
        PageResponse<DataQualityIssueDTO> result = dataQualityService.queryIssues(
                null, null, 1, 20);

        // Then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getPagination().getPage()).isEqualTo(1);
        assertThat(result.getPagination().getPageSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("getOpenIssues - 應返回未解決的問題")
    void getOpenIssues_shouldReturnOpenIssues() {
        // Given
        Page<DataQualityIssue> issuePage = new PageImpl<>(List.of(testIssue));
        when(issueRepository.findOpenIssues(any(Pageable.class))).thenReturn(issuePage);
        when(issueConverter.toDTOList(any())).thenReturn(List.of(testIssueDTO));

        // When
        PageResponse<DataQualityIssueDTO> result = dataQualityService.getOpenIssues(1, 20);

        // Then
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getStatus()).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("getIssueById - 應返回指定 ID 的問題")
    void getIssueById_shouldReturnIssueById() {
        // Given
        when(issueRepository.findById(1L)).thenReturn(Optional.of(testIssue));
        when(issueConverter.toDTO(testIssue)).thenReturn(testIssueDTO);

        // When
        DataQualityIssueDTO result = dataQualityService.getIssueById(1L);

        // Then
        assertThat(result.getIssueId()).isEqualTo(1L);
        assertThat(result.getSeverity()).isEqualTo("HIGH");
    }

    @Test
    @DisplayName("getIssueById - 問題不存在時應拋出異常")
    void getIssueById_shouldThrowWhenIssueNotFound() {
        // Given
        when(issueRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dataQualityService.getIssueById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Issue not found");
    }

    @Test
    @DisplayName("getQualitySummary - 應正確計算品質摘要統計")
    void getQualitySummary_shouldCalculateQualitySummary() {
        // Given
        when(checkRepository.count()).thenReturn(10L);
        when(issueRepository.countOpenIssues()).thenReturn(5L);
        when(issueRepository.countBySeverityAndStatus("HIGH", "OPEN")).thenReturn(2L);
        when(issueRepository.countBySeverityAndStatus("MEDIUM", "OPEN")).thenReturn(2L);
        when(issueRepository.countBySeverityAndStatus("LOW", "OPEN")).thenReturn(1L);

        // When
        DataQualitySummaryDTO result = dataQualityService.getQualitySummary();

        // Then
        assertThat(result.getTotalActiveChecks()).isEqualTo(10L);
        assertThat(result.getTotalOpenIssues()).isEqualTo(5L);
        assertThat(result.getHighSeverityIssues()).isEqualTo(2L);
        assertThat(result.getMediumSeverityIssues()).isEqualTo(2L);
        assertThat(result.getLowSeverityIssues()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getQualitySummary - 無問題時品質分數應為 100")
    void getQualitySummary_shouldReturnPerfectScoreWhenNoIssues() {
        // Given
        when(checkRepository.count()).thenReturn(10L);
        when(issueRepository.countOpenIssues()).thenReturn(0L);
        when(issueRepository.countBySeverityAndStatus(anyString(), anyString())).thenReturn(0L);

        // When
        DataQualitySummaryDTO result = dataQualityService.getQualitySummary();

        // Then
        assertThat(result.getQualityScore()).isEqualTo(100);
    }

    @Test
    @DisplayName("getQualitySummary - 高嚴重性問題應大幅降低品質分數")
    void getQualitySummary_shouldReduceScoreForHighSeverityIssues() {
        // Given
        when(checkRepository.count()).thenReturn(10L);
        when(issueRepository.countOpenIssues()).thenReturn(5L);
        when(issueRepository.countBySeverityAndStatus("HIGH", "OPEN")).thenReturn(5L);
        when(issueRepository.countBySeverityAndStatus("MEDIUM", "OPEN")).thenReturn(0L);
        when(issueRepository.countBySeverityAndStatus("LOW", "OPEN")).thenReturn(0L);

        // When
        DataQualitySummaryDTO result = dataQualityService.getQualitySummary();

        // Then
        // 100 - (5 * 10) - (5 * 2) = 100 - 50 - 10 = 40
        assertThat(result.getQualityScore()).isEqualTo(40);
    }

    @Test
    @DisplayName("getQualitySummary - 品質分數不應低於 0")
    void getQualitySummary_shouldNotHaveNegativeScore() {
        // Given
        when(checkRepository.count()).thenReturn(10L);
        when(issueRepository.countOpenIssues()).thenReturn(100L);
        when(issueRepository.countBySeverityAndStatus("HIGH", "OPEN")).thenReturn(50L);
        when(issueRepository.countBySeverityAndStatus("MEDIUM", "OPEN")).thenReturn(30L);
        when(issueRepository.countBySeverityAndStatus("LOW", "OPEN")).thenReturn(20L);

        // When
        DataQualitySummaryDTO result = dataQualityService.getQualitySummary();

        // Then
        assertThat(result.getQualityScore()).isGreaterThanOrEqualTo(0);
    }
}
