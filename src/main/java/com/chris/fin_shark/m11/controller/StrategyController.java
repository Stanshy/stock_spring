package com.chris.fin_shark.m11.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.dto.StrategyDTO;
import com.chris.fin_shark.m11.dto.StrategyExecutionDTO;
import com.chris.fin_shark.m11.dto.StrategySignalDTO;
import com.chris.fin_shark.m11.dto.request.*;
import com.chris.fin_shark.m11.dto.response.PresetStrategyResponse;
import com.chris.fin_shark.m11.dto.response.StrategyExecuteResponse;
import com.chris.fin_shark.m11.service.SignalService;
import com.chris.fin_shark.m11.service.StrategyExecutionService;
import com.chris.fin_shark.m11.service.StrategyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 策略管理 Controller
 * <p>
 * 提供策略的 CRUD、執行、信號查詢等 API
 * Base URL: /api/v1/strategy
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/strategy")
@Slf4j
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyService strategyService;
    private final StrategyExecutionService strategyExecutionService;
    private final SignalService signalService;

    /**
     * API-M11-001: 查詢策略清單
     *
     * @param status  策略狀態（DRAFT, ACTIVE, INACTIVE, ARCHIVED）
     * @param type    策略類型（MOMENTUM, VALUE, HYBRID, CUSTOM）
     * @param keyword 關鍵字搜尋
     * @param page    頁碼（從 0 開始）
     * @param size    每頁筆數
     * @param sort    排序欄位與方向
     * @return 策略清單
     */
    @GetMapping
    public ApiResponse<PageResponse<StrategyDTO>> getStrategies(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "created_at,desc") String sort) {

        log.info("GET /api/v1/strategy?status={}&type={}&page={}&size={}", status, type, page, size);

        // 解析排序參數（格式: "field,direction"）
        String sortField = "created_at";
        String sortDirection = "desc";
        if (sort != null && sort.contains(",")) {
            String[] parts = sort.split(",");
            sortField = parts[0];
            sortDirection = parts.length > 1 ? parts[1] : "desc";
        }

        StrategyQueryRequest request = StrategyQueryRequest.builder()
                .status(status)
                .type(type)
                .keyword(keyword)
                .page(page)
                .size(size)
                .sortField(sortField)
                .sortDirection(sortDirection)
                .build();

        PageResponse<StrategyDTO> response = strategyService.getStrategies(request);
        return ApiResponse.success(response);
    }

    /**
     * API-M11-002: 查詢策略詳情
     *
     * @param strategyId 策略 ID
     * @param version    指定版本號（省略則取最新版）
     * @return 策略詳情
     */
    @GetMapping("/{strategyId}")
    public ApiResponse<StrategyDTO> getStrategy(
            @PathVariable String strategyId,
            @RequestParam(required = false) Integer version) {

        log.info("GET /api/v1/strategy/{} version={}", strategyId, version);

        StrategyDTO response;
        if (version != null) {
            response = strategyService.getStrategy(strategyId, version);
        } else {
            response = strategyService.getStrategy(strategyId);
        }

        return ApiResponse.success(response);
    }

    /**
     * API-M11-003: 建立新策略
     *
     * @param request 策略建立請求
     * @return 建立的策略
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<StrategyDTO> createStrategy(@Valid @RequestBody StrategyCreateRequest request) {

        log.info("POST /api/v1/strategy name={}", request.getStrategyName());

        StrategyDTO response = strategyService.createStrategy(request);
        return ApiResponse.success("Strategy created successfully", response);
    }

    /**
     * API-M11-004: 更新策略
     *
     * @param strategyId 策略 ID
     * @param request    策略更新請求
     * @return 更新後的策略
     */
    @PutMapping("/{strategyId}")
    public ApiResponse<StrategyDTO> updateStrategy(
            @PathVariable String strategyId,
            @Valid @RequestBody StrategyUpdateRequest request) {

        log.info("PUT /api/v1/strategy/{}", strategyId);

        StrategyDTO response = strategyService.updateStrategy(strategyId, request);
        return ApiResponse.success("Strategy updated successfully", response);
    }

    /**
     * API-M11-005: 刪除策略（封存）
     *
     * @param strategyId 策略 ID
     * @return 成功訊息
     */
    @DeleteMapping("/{strategyId}")
    public ApiResponse<Void> archiveStrategy(@PathVariable String strategyId) {

        log.info("DELETE /api/v1/strategy/{}", strategyId);

        strategyService.archiveStrategy(strategyId);
        return ApiResponse.success("Strategy archived successfully", null);
    }

    /**
     * API-M11-006: 更新策略狀態
     *
     * @param strategyId 策略 ID
     * @param request    狀態更新請求
     * @return 更新後的策略
     */
    @PatchMapping("/{strategyId}/status")
    public ApiResponse<StrategyDTO> updateStrategyStatus(
            @PathVariable String strategyId,
            @Valid @RequestBody StrategyStatusRequest request) {

        log.info("PATCH /api/v1/strategy/{}/status -> {}", strategyId, request.getStatus());

        StrategyDTO response = strategyService.updateStatus(strategyId, request.getStatus());
        return ApiResponse.success("Strategy status updated", response);
    }

    /**
     * API-M11-007: 執行策略
     *
     * @param strategyId 策略 ID
     * @param request    執行請求
     * @return 執行結果
     */
    @PostMapping("/{strategyId}/execute")
    public ApiResponse<StrategyExecuteResponse> executeStrategy(
            @PathVariable String strategyId,
            @Valid @RequestBody StrategyExecuteRequest request) {

        log.info("POST /api/v1/strategy/{}/execute date={}", strategyId, request.getExecutionDate());

        StrategyExecuteResponse response = strategyExecutionService.executeStrategy(strategyId, request);
        return ApiResponse.success("Strategy executed successfully", response);
    }

    /**
     * API-M11-008: 查詢策略信號
     *
     * @param strategyId    策略 ID
     * @param startDate     開始日期
     * @param endDate       結束日期
     * @param signalType    信號類型
     * @param stockId       指定股票
     * @param minConfidence 最低信心度
     * @param page          頁碼
     * @param size          每頁筆數
     * @return 策略信號清單
     */
    @GetMapping("/{strategyId}/signals")
    public ApiResponse<PageResponse<StrategySignalDTO>> getSignals(
            @PathVariable String strategyId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false) String signalType,
            @RequestParam(required = false) String stockId,
            @RequestParam(required = false) BigDecimal minConfidence,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "50") int size) {

        log.info("GET /api/v1/strategy/{}/signals", strategyId);

        SignalQueryRequest request = SignalQueryRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .signalType(signalType)
                .stockId(stockId)
                .minConfidence(minConfidence)
                .page(page)
                .size(size)
                .build();

        PageResponse<StrategySignalDTO> response = signalService.getSignals(strategyId, request);
        return ApiResponse.success(response);
    }

    /**
     * API-M11-009: 查詢執行歷史
     *
     * @param strategyId 策略 ID
     * @param startDate  開始日期
     * @param endDate    結束日期
     * @param page       頁碼
     * @param size       每頁筆數
     * @return 執行歷史清單
     */
    @GetMapping("/{strategyId}/executions")
    public ApiResponse<PageResponse<StrategyExecutionDTO>> getExecutionHistory(
            @PathVariable String strategyId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {

        log.info("GET /api/v1/strategy/{}/executions", strategyId);

        // 設定預設日期範圍
        if (startDate == null) {
            startDate = LocalDate.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        PageResponse<StrategyExecutionDTO> response = strategyExecutionService
                .getExecutionHistory(strategyId, startDate, endDate, page, size);

        return ApiResponse.success(response);
    }

    /**
     * API-M11-010: 查詢預設策略庫
     *
     * @return 預設策略清單
     */
    @GetMapping("/presets")
    public ApiResponse<PresetStrategyResponse> getPresetStrategies() {

        log.info("GET /api/v1/strategy/presets");

        List<Strategy> presets = strategyService.getPresetStrategies();

        List<PresetStrategyResponse.PresetStrategyDTO> presetDTOs = presets.stream()
                .map(s -> PresetStrategyResponse.PresetStrategyDTO.builder()
                        .strategyId(s.getStrategyId())
                        .strategyName(s.getStrategyName())
                        .strategyType(s.getStrategyType())
                        .description(s.getDescription())
                        .build())
                .collect(Collectors.toList());

        PresetStrategyResponse response = PresetStrategyResponse.builder()
                .presetStrategies(presetDTOs)
                .build();

        return ApiResponse.success(response);
    }
}
