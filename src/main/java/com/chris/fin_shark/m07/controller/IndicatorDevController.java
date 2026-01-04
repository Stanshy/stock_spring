package com.chris.fin_shark.m07.controller;

import com.chris.fin_shark.m07.service.IndicatorCalculationAsyncExecutor;
import com.chris.fin_shark.m07.service.IndicatorCalculationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.chris.fin_shark.common.dto.ApiResponse;

import java.time.LocalDate;

//開發用
@RestController
@RequestMapping("/api/m07/dev/indicators")
@RequiredArgsConstructor
public class IndicatorDevController {

    private final IndicatorCalculationService calculationService;
    private final IndicatorCalculationAsyncExecutor asyncExecutor;

    /**
     * DEV ONLY: 回填某檔股票一段期間的指標
     */
    @PostMapping("/backfill")
    public ApiResponse<Void> backfill(@Valid  @RequestBody IndicatorBackfillRequest request) {

        String stockId = request.getStockId();
        LocalDate end = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
        LocalDate start = request.getStartDate() != null ? request.getStartDate() : end.minusYears(1);
        String priority = request.getIndicatorPriority();
        boolean force = request.getForceRecalculate() != null && request.getForceRecalculate();


        asyncExecutor.backfillAsync(
                stockId,
                start,
                end,
                priority,
                force
        );

        return ApiResponse.success(null);
    }
}
