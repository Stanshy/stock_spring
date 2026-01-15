package com.chris.fin_shark.m09.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m09.dto.request.ChipCalculationRequest;
import com.chris.fin_shark.m09.service.ChipCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 籌碼計算 Job Controller
 * <p>
 * 功能編號: F-M09-001, F-M09-002, F-M09-005
 * 提供手動觸發籌碼計算的 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/jobs")
@Slf4j
@RequiredArgsConstructor
public class ChipJobController {

    private final ChipCalculationService chipCalculationService;

    /**
     * API-M09-006: 手動觸發籌碼計算
     *
     * @param request 計算請求
     * @return 計算統計資訊
     */
    @PostMapping("/calculate-chip")
    public ApiResponse<Map<String, Object>> calculateChipAnalysis(
            @Valid @RequestBody ChipCalculationRequest request) {

        log.info("POST /api/jobs/calculate-chip: date={}, priority={}",
                request.getCalculationDate(),
                request.getPriority());

        Map<String, Object> response = chipCalculationService.triggerCalculation(request);

        return ApiResponse.success(response);
    }

    /**
     * API-M09-007: 手動觸發單一股票籌碼計算（開發用）
     *
     * @param stockId 股票代碼
     * @return 計算結果摘要
     */
    @PostMapping("/calculate-chip/{stockId}")
    public ApiResponse<Map<String, Object>> calculateSingleStock(
            @PathVariable String stockId) {

        log.info("POST /api/jobs/calculate-chip/{}", stockId);

        // 使用預設配置計算單一股票
        ChipCalculationRequest request = ChipCalculationRequest.builder()
                .stockIds(java.util.List.of(stockId))
                .priority("P1")
                .includeInstitutional(true)
                .includeMargin(true)
                .includeSignals(true)
                .build();

        Map<String, Object> response = chipCalculationService.triggerCalculation(request);

        return ApiResponse.success(response);
    }
}
