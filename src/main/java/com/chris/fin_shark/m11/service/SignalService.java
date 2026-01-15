package com.chris.fin_shark.m11.service;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m11.converter.StrategyConverter;
import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.dto.StrategySignalDTO;
import com.chris.fin_shark.m11.dto.request.SignalQueryRequest;
import com.chris.fin_shark.m11.dto.response.SignalScanResponse;
import com.chris.fin_shark.m11.exception.StrategyNotFoundException;
import com.chris.fin_shark.m11.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 信號服務
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignalService {

    private final StrategyMapper strategyMapper;
    private final StrategyConverter strategyConverter;

    /**
     * 查詢策略信號
     */
    public PageResponse<StrategySignalDTO> getSignals(String strategyId, SignalQueryRequest request) {
        log.debug("查詢策略信號: strategyId={}, request={}", strategyId, request);

        // 驗證策略存在
        Strategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new StrategyNotFoundException(strategyId);
        }

        // 設定預設日期範圍
        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null
                ? request.getEndDate()
                : LocalDate.now();

        int offset = request.getPage() * request.getSize();

        List<StrategySignal> signals = strategyMapper.selectSignals(
                strategyId,
                startDate,
                endDate,
                request.getSignalType(),
                request.getStockId(),
                request.getMinConfidence(),
                offset,
                request.getSize());

        int total = strategyMapper.countSignals(
                strategyId,
                startDate,
                endDate,
                request.getSignalType(),
                request.getStockId(),
                request.getMinConfidence());

        List<StrategySignalDTO> dtos = strategyConverter.toSignalDTOList(signals);

        // 補充策略名稱
        for (StrategySignalDTO dto : dtos) {
            dto.setStrategyName(strategy.getStrategyName());
        }

        return PageResponse.of(dtos, request.getPage() + 1, request.getSize(), total);
    }

    /**
     * 全市場信號掃描
     */
    public SignalScanResponse scanSignals(
            LocalDate tradeDate,
            String signalType,
            BigDecimal minConfidence,
            String strategyType,
            int limit) {

        log.info("全市場信號掃描: date={}, signalType={}, minConfidence={}",
                tradeDate, signalType, minConfidence);

        long startTime = System.currentTimeMillis();

        // 設定預設值
        if (tradeDate == null) {
            tradeDate = LocalDate.now();
        }
        if (minConfidence == null) {
            minConfidence = BigDecimal.valueOf(60);
        }

        // 取得所有啟用的策略
        List<Strategy> activeStrategies = strategyMapper.selectActiveStrategies();

        // 依策略類型篩選
        if (strategyType != null && !strategyType.isEmpty() && !"all".equalsIgnoreCase(strategyType)) {
            activeStrategies = activeStrategies.stream()
                    .filter(s -> strategyType.equals(s.getStrategyType().name()))
                    .collect(Collectors.toList());
        }

        // 收集所有信號
        List<StrategySignal> allSignals = new ArrayList<>();
        Map<String, String> strategyNames = new HashMap<>();

        for (Strategy strategy : activeStrategies) {
            strategyNames.put(strategy.getStrategyId(), strategy.getStrategyName());

            List<StrategySignal> signals = strategyMapper.selectUnconsumedSignals(
                    tradeDate,
                    strategy.getStrategyId(),
                    signalType,
                    minConfidence,
                    limit);

            allSignals.addAll(signals);
        }

        // 依信心度排序並限制數量
        allSignals.sort((a, b) -> b.getConfidenceScore().compareTo(a.getConfidenceScore()));
        if (allSignals.size() > limit) {
            allSignals = allSignals.subList(0, limit);
        }

        // 統計
        int buyCount = (int) allSignals.stream()
                .filter(s -> "BUY".equals(s.getSignalType().name())).count();
        int sellCount = (int) allSignals.stream()
                .filter(s -> "SELL".equals(s.getSignalType().name())).count();
        int holdCount = (int) allSignals.stream()
                .filter(s -> "HOLD".equals(s.getSignalType().name())).count();

        // 統計每檔股票的信號數
        Map<String, Integer> stockSignalCount = allSignals.stream()
                .collect(Collectors.groupingBy(
                        StrategySignal::getStockId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        // 轉換為 DTO
        List<StrategySignalDTO> signalDTOs = strategyConverter.toSignalDTOList(allSignals);
        for (StrategySignalDTO dto : signalDTOs) {
            dto.setStrategyName(strategyNames.get(dto.getStrategyId()));
        }

        long scanTime = System.currentTimeMillis() - startTime;
        log.info("信號掃描完成: 掃描 {} 個策略，找到 {} 個信號，耗時 {} ms",
                activeStrategies.size(), allSignals.size(), scanTime);

        return SignalScanResponse.builder()
                .tradeDate(tradeDate)
                .scanTimeMs(scanTime)
                .strategiesScanned(activeStrategies.size())
                .totalSignals(allSignals.size())
                .signalSummary(SignalScanResponse.SignalSummaryDTO.builder()
                        .buy(buyCount)
                        .sell(sellCount)
                        .hold(holdCount)
                        .build())
                .signals(signalDTOs)
                .stockSignalCount(stockSignalCount)
                .build();
    }

    /**
     * 查詢未消費的信號（供 M13 使用）
     */
    public List<StrategySignal> getUnconsumedSignals(
            LocalDate tradeDate,
            String strategyId,
            String signalType,
            BigDecimal minConfidence,
            int limit) {

        return strategyMapper.selectUnconsumedSignals(
                tradeDate, strategyId, signalType, minConfidence, limit);
    }

    /**
     * 標記信號已消費（供 M13 呼叫）
     */
    @Transactional
    public void markSignalsConsumed(List<String> signalIds, LocalDate tradeDate, String consumedBy) {
        if (signalIds == null || signalIds.isEmpty()) {
            return;
        }

        log.info("標記信號已消費: {} 個信號, consumedBy={}", signalIds.size(), consumedBy);
        strategyMapper.markSignalsConsumed(signalIds, tradeDate, consumedBy);
    }
}
