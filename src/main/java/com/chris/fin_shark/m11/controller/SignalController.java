package com.chris.fin_shark.m11.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m11.dto.response.SignalScanResponse;
import com.chris.fin_shark.m11.service.SignalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 信號掃描 Controller
 * <p>
 * 提供全市場策略信號掃描 API
 * Base URL: /api/v1/strategy/signals
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/strategy/signals")
@Slf4j
@RequiredArgsConstructor
public class SignalController {

    private final SignalService signalService;

    /**
     * API-M11-013: 全市場策略信號掃描
     * <p>
     * 掃描全市場，取得所有活躍策略產生的信號
     *
     * @param tradeDate     交易日期（預設最近交易日）
     * @param signalType    信號類型（BUY, SELL, HOLD）
     * @param minConfidence 最低信心度（預設 60）
     * @param strategyType  策略類型
     * @param limit         回傳筆數（預設 100）
     * @return 信號掃描結果
     */
    @GetMapping("/scan")
    public ApiResponse<SignalScanResponse> scanSignals(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate,
            @RequestParam(required = false) String signalType,
            @RequestParam(required = false) BigDecimal minConfidence,
            @RequestParam(required = false) String strategyType,
            @RequestParam(required = false, defaultValue = "100") int limit) {

        log.info("GET /api/v1/strategy/signals/scan?date={}&signalType={}&minConfidence={}",
                tradeDate, signalType, minConfidence);

        SignalScanResponse response = signalService.scanSignals(
                tradeDate, signalType, minConfidence, strategyType, limit);

        return ApiResponse.success(response);
    }
}
