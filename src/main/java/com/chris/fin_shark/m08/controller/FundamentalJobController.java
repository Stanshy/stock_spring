package com.chris.fin_shark.m08.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m08.dto.request.CalculateFundamentalsRequest;
import com.chris.fin_shark.m08.job.FundamentalCalculationJob;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 基本面分析 Job Controller（管理員）
 * <p>
 * 功能編號: F-M08-013
 * 提供手動觸發 Job 的 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/jobs")
@Slf4j
@RequiredArgsConstructor
public class FundamentalJobController {

    private final FundamentalCalculationJob calculationJob;

    /**
     * API-M08-006: 手動觸發財務指標計算（P0 核心功能）
     * <p>
     * POST /api/jobs/calculate-fundamentals
     * </p>
     *
     * @param request 計算請求
     * @return 執行結果
     */
    @PostMapping("/calculate-fundamentals")
    public ApiResponse<String> calculateFundamentals(
            @Valid @RequestBody CalculateFundamentalsRequest request) {

        log.info("POST /api/jobs/calculate-fundamentals: request={}", request);

        // 手動觸發 Job
        calculationJob.calculateManually(request);

        return ApiResponse.success("財務指標計算 Job 已觸發");
    }
}
