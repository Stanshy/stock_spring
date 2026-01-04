package com.chris.fin_shark.m07.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m07.dto.IndicatorCalculationJobDTO;
import com.chris.fin_shark.m07.dto.request.IndicatorCalculationRequest;

import com.chris.fin_shark.m07.service.IndicatorCalculationAsyncExecutor;
import com.chris.fin_shark.m07.service.IndicatorCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 指標 Job 管理 Controller
 * <p>
 * 功能編號: F-M07-013
 * 功能名稱: 指標計算排程
 * 提供手動觸發指標計算的 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/jobs")
@Slf4j
@RequiredArgsConstructor
public class IndicatorJobController {

    private final IndicatorCalculationService calculationService;
    private final IndicatorCalculationAsyncExecutor asyncExecutor;

    /**
     * API-M07-007: 手動觸發指標計算
     *
     * @param request 計算請求
     * @return Job 執行資訊
     */
    @PostMapping("/calculate-indicators")
    public ApiResponse<IndicatorCalculationJobDTO> calculateIndicators(
            @Valid @RequestBody IndicatorCalculationRequest request) {

        log.info("POST /api/jobs/calculate-indicators: date={}, priority={}",
                request.getCalculationDate(),
                request.getIndicatorPriority());

        IndicatorCalculationJobDTO response = calculationService
                .triggerCalculation(request);

        asyncExecutor.executeByJobId(response.getJobId());

        return ApiResponse.success(response);
    }
}
