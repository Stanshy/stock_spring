package com.chris.fin_shark.m11.service;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m11.converter.StrategyConverter;
import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategyExecution;
import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.dto.FactorDataDTO;
import com.chris.fin_shark.m11.dto.StrategyExecutionDTO;
import com.chris.fin_shark.m11.dto.StrategySignalDTO;
import com.chris.fin_shark.m11.dto.request.StrategyExecuteRequest;
import com.chris.fin_shark.m11.dto.response.StrategyExecuteResponse;
import com.chris.fin_shark.m11.engine.DefaultStrategyEngine;
import com.chris.fin_shark.m11.engine.Diagnostics;
import com.chris.fin_shark.m11.engine.StrategyExecutionPlan;
import com.chris.fin_shark.m11.enums.ExecutionStatus;
import com.chris.fin_shark.m11.enums.ExecutionType;
import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.exception.StrategyExecutionException;
import com.chris.fin_shark.m11.exception.StrategyNotFoundException;
import com.chris.fin_shark.m11.mapper.FactorDataMapper;
import com.chris.fin_shark.m11.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 策略執行服務
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyExecutionService {

    private final StrategyMapper strategyMapper;
    private final FactorDataMapper factorDataMapper;
    private final DefaultStrategyEngine strategyEngine;
    private final StrategyConverter strategyConverter;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private final AtomicInteger executionCounter = new AtomicInteger(0);

    /**
     * 執行策略
     */
    @Transactional
    public StrategyExecuteResponse executeStrategy(String strategyId, StrategyExecuteRequest request) {
        log.info("開始執行策略: {}", strategyId);
        long startTime = System.currentTimeMillis();

        // 1. 取得策略
        Strategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new StrategyNotFoundException(strategyId);
        }

        // 2. 檢查策略狀態
        if (strategy.getStatus() != StrategyStatus.ACTIVE) {
            throw new StrategyExecutionException(strategyId, "策略未啟用");
        }

        // 3. 準備執行計劃
        LocalDate executionDate = request.getExecutionDate() != null
                ? request.getExecutionDate()
                : LocalDate.now();

        StrategyExecutionPlan plan = buildExecutionPlan(strategyId, request, executionDate);

        // 4. 建立執行記錄
        String executionId = generateExecutionId(executionDate);
        StrategyExecution execution = createExecution(strategy, executionId, executionDate, plan);
        strategyMapper.insertExecution(execution);

        Diagnostics diagnostics = new Diagnostics();
        List<StrategySignal> signals = new ArrayList<>();

        try {
            // 5. 載入因子數據
            LocalDate latestReportDate = factorDataMapper.getLatestReportDate();
            if (latestReportDate == null) {
                latestReportDate = executionDate.minusMonths(3);
            }

            List<FactorDataDTO> factorDataList = loadFactorData(plan, executionDate, latestReportDate);
            execution.setStocksEvaluated(factorDataList.size());

            log.info("載入因子數據完成: {} 檔股票", factorDataList.size());

            // 6. 逐一評估股票
            for (FactorDataDTO factorData : factorDataList) {
                try {
                    Map<String, Object> factorMap = factorData.toFactorMap();
                    diagnostics.recordFactorLoaded();

                    StrategySignal signal = strategyEngine.evaluateAndGenerateSignal(
                            strategy,
                            factorData.getStockId(),
                            factorMap,
                            executionId,
                            executionDate);

                    if (signal != null) {
                        signals.add(signal);
                    }
                } catch (Exception e) {
                    log.warn("股票評估失敗: {}", factorData.getStockId(), e);
                    diagnostics.recordSkipped("evaluation_error");
                }
            }

            // 7. 批次儲存信號
            if (!signals.isEmpty() && Boolean.TRUE.equals(
                    request.getOptions() != null ? request.getOptions().getSaveResults() : true)) {
                strategyMapper.batchInsertSignals(signals);
            }

            // 8. 更新執行記錄
            updateExecutionSuccess(execution, signals, diagnostics, startTime);

            // 9. 更新策略統計
            strategyMapper.updateStatistics(strategyId, signals.size());

            log.info("策略執行完成: {} 產生 {} 個信號，耗時 {} ms",
                    strategyId, signals.size(), System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("策略執行失敗: {}", strategyId, e);
            updateExecutionFailed(execution, e.getMessage(), startTime);
            throw new StrategyExecutionException(strategyId, e.getMessage());
        }

        // 10. 建立回應
        return buildResponse(strategy, execution, signals, diagnostics);
    }

    /**
     * 查詢執行歷史
     */
    public PageResponse<StrategyExecutionDTO> getExecutionHistory(
            String strategyId,
            LocalDate startDate,
            LocalDate endDate,
            int page,
            int size) {

        // 驗證策略存在
        if (strategyMapper.selectById(strategyId) == null) {
            throw new StrategyNotFoundException(strategyId);
        }

        int offset = page * size;
        List<StrategyExecution> executions = strategyMapper.selectExecutionHistory(
                strategyId, startDate, endDate, offset, size);
        int total = strategyMapper.countExecutionHistory(strategyId, startDate, endDate);

        List<StrategyExecutionDTO> dtos = strategyConverter.toExecutionDTOList(executions);

        return PageResponse.of(dtos, page + 1, size, total);
    }

    /**
     * 批次執行所有啟用的策略（供 Job 呼叫）
     */
    @Transactional
    public void executeBatch(LocalDate executionDate) {
        log.info("開始批次執行策略: {}", executionDate);

        List<Strategy> activeStrategies = strategyMapper.selectActiveStrategies();
        log.info("找到 {} 個啟用的策略", activeStrategies.size());

        for (Strategy strategy : activeStrategies) {
            try {
                StrategyExecuteRequest request = StrategyExecuteRequest.builder()
                        .executionDate(executionDate)
                        .stockUniverse(StrategyExecuteRequest.StockUniverseDTO.builder()
                                .type("MARKET")
                                .marketType("TWSE")
                                .minVolume(1000)
                                .excludeEtf(true)
                                .build())
                        .options(StrategyExecuteRequest.ExecutionOptionsDTO.builder()
                                .includeFactorValues(true)
                                .includeDiagnostics(true)
                                .saveResults(true)
                                .build())
                        .build();

                executeStrategy(strategy.getStrategyId(), request);

            } catch (Exception e) {
                log.error("策略執行失敗: {}", strategy.getStrategyId(), e);
                // 繼續執行下一個策略
            }
        }

        log.info("批次執行完成");
    }

    // ==================== 私有方法 ====================

    private String generateExecutionId(LocalDate date) {
        int counter = executionCounter.incrementAndGet();
        if (counter > 999) {
            executionCounter.set(0);
        }
        return String.format("EXEC_%s_%03d", date.format(DATE_FORMATTER), counter);
    }

    private StrategyExecutionPlan buildExecutionPlan(
            String strategyId,
            StrategyExecuteRequest request,
            LocalDate executionDate) {

        StrategyExecutionPlan.StockUniverse stockUniverse;

        if (request.getStockUniverse() != null) {
            var su = request.getStockUniverse();
            stockUniverse = StrategyExecutionPlan.StockUniverse.builder()
                    .type(StrategyExecutionPlan.StockUniverse.UniverseType.valueOf(
                            su.getType() != null ? su.getType() : "MARKET"))
                    .marketType(su.getMarketType())
                    .minVolume(su.getMinVolume())
                    .excludeEtf(Boolean.TRUE.equals(su.getExcludeEtf()))
                    .stockIds(su.getStockIds())
                    .industries(su.getIndustries())
                    .build();
        } else {
            stockUniverse = StrategyExecutionPlan.StockUniverse.twseAll();
        }

        return StrategyExecutionPlan.builder()
                .strategyId(strategyId)
                .executionDate(executionDate)
                .stockUniverse(stockUniverse)
                .build();
    }

    private StrategyExecution createExecution(
            Strategy strategy,
            String executionId,
            LocalDate executionDate,
            StrategyExecutionPlan plan) {

        return StrategyExecution.builder()
                .executionId(executionId)
                .strategyId(strategy.getStrategyId())
                .strategyVersion(strategy.getCurrentVersion())
                .executionDate(executionDate)
                .executionType(ExecutionType.MANUAL)
                .status(ExecutionStatus.RUNNING)
                .startedAt(LocalDateTime.now())
                .build();
    }

    private List<FactorDataDTO> loadFactorData(
            StrategyExecutionPlan plan,
            LocalDate executionDate,
            LocalDate latestReportDate) {

        var universe = plan.getStockUniverse();

        return factorDataMapper.loadFactorData(
                executionDate,
                latestReportDate,
                universe.getMarketType(),
                universe.getMinVolume(),
                universe.isExcludeEtf(),
                universe.getStockIds(),
                universe.getIndustries());
    }

    private void updateExecutionSuccess(
            StrategyExecution execution,
            List<StrategySignal> signals,
            Diagnostics diagnostics,
            long startTime) {

        execution.setSignalsGenerated(signals.size());
        execution.setBuySignals((int) signals.stream()
                .filter(s -> "BUY".equals(s.getSignalType().name())).count());
        execution.setSellSignals((int) signals.stream()
                .filter(s -> "SELL".equals(s.getSignalType().name())).count());
        execution.setHoldSignals((int) signals.stream()
                .filter(s -> "HOLD".equals(s.getSignalType().name())).count());

        if (!signals.isEmpty()) {
            BigDecimal avgConf = signals.stream()
                    .map(StrategySignal::getConfidenceScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(signals.size()), 2, RoundingMode.HALF_UP);
            execution.setAvgConfidence(avgConf);
        }

        execution.setExecutionTimeMs((int) (System.currentTimeMillis() - startTime));
        execution.setStatus(ExecutionStatus.SUCCESS);
        execution.setDiagnostics(diagnostics.toMap());
        execution.setCompletedAt(LocalDateTime.now());

        strategyMapper.updateExecution(execution);
    }

    private void updateExecutionFailed(
            StrategyExecution execution,
            String errorMessage,
            long startTime) {

        execution.setExecutionTimeMs((int) (System.currentTimeMillis() - startTime));
        execution.setStatus(ExecutionStatus.FAILED);
        execution.setErrorMessage(errorMessage);
        execution.setCompletedAt(LocalDateTime.now());

        strategyMapper.updateExecution(execution);
    }

    private StrategyExecuteResponse buildResponse(
            Strategy strategy,
            StrategyExecution execution,
            List<StrategySignal> signals,
            Diagnostics diagnostics) {

        List<StrategySignalDTO> signalDTOs = strategyConverter.toSignalDTOList(signals);

        return StrategyExecuteResponse.builder()
                .executionId(execution.getExecutionId())
                .strategyId(strategy.getStrategyId())
                .strategyName(strategy.getStrategyName())
                .executionDate(execution.getExecutionDate())
                .executionSummary(StrategyExecuteResponse.ExecutionSummaryDTO.builder()
                        .stocksEvaluated(execution.getStocksEvaluated())
                        .signalsGenerated(signals.size())
                        .buySignals(execution.getBuySignals())
                        .sellSignals(execution.getSellSignals())
                        .avgConfidence(execution.getAvgConfidence())
                        .executionTimeMs((long) execution.getExecutionTimeMs())
                        .build())
                .signals(signalDTOs)
                .diagnostics(StrategyExecuteResponse.DiagnosticsDTO.builder()
                        .factorsLoaded(diagnostics.getFactorsLoaded())
                        .factorsMissing(diagnostics.getFactorsMissing())
                        .calculationErrors(diagnostics.getCalculationErrors())
                        .warnings(diagnostics.getWarnings())
                        .build())
                .build();
    }
}
