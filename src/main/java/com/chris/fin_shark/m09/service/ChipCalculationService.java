package com.chris.fin_shark.m09.service;

import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.repository.StockRepository;
import com.chris.fin_shark.m09.converter.ChipAnalysisResultConverter;
import com.chris.fin_shark.m09.converter.ChipSignalConverter;
import com.chris.fin_shark.m09.domain.ChipAnalysisResult;
import com.chris.fin_shark.m09.domain.ChipSignalEntity;
import com.chris.fin_shark.m09.dto.request.ChipCalculationRequest;
import com.chris.fin_shark.m09.engine.*;
import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.engine.model.ChipSignal;
import com.chris.fin_shark.m09.provider.ChipSeriesProvider;
import com.chris.fin_shark.m09.repository.ChipAnalysisResultRepository;
import com.chris.fin_shark.m09.repository.ChipSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 籌碼計算服務
 * <p>
 * 整合 ChipEngine 執行籌碼指標計算、異常訊號偵測，並儲存結果。
 * 對應功能編號: F-M09-001, F-M09-002, F-M09-005
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChipCalculationService {

    private final ChipEngine chipEngine;
    private final ChipRegistry chipRegistry;
    private final ChipSeriesProvider chipSeriesProvider;
    private final ChipAnalysisResultRepository analysisResultRepository;
    private final ChipSignalRepository signalRepository;
    private final ChipAnalysisResultConverter resultConverter;
    private final ChipSignalConverter signalConverter;
    private final StockRepository stockRepository;

    /**
     * 觸發籌碼計算
     *
     * @param request 計算請求
     * @return 計算統計資訊
     */
    @Transactional
    public Map<String, Object> triggerCalculation(ChipCalculationRequest request) {
        LocalDate calculationDate = request.getCalculationDate() != null
                ? request.getCalculationDate()
                : LocalDate.now();

        log.info("========================================");
        log.info("開始籌碼計算");
        log.info("========================================");
        log.info("計算日期: {}", calculationDate);
        log.info("優先級: {}", request.getPriority());

        long startTime = System.currentTimeMillis();

        try {
            // 1. 建立計算計劃
            ChipPlan plan = createPlan(request);

            // 2. 取得目標股票
            List<String> targetStocks = getTargetStocks(request.getStockIds());
            log.info("目標股票數: {}", targetStocks.size());

            // 3. 批次取得籌碼資料
            Map<String, ChipSeries> seriesMap = chipSeriesProvider.getBatch(
                    targetStocks,
                    calculationDate,
                    60  // 取最近 60 天
            );
            log.info("已取得籌碼資料: {} 支股票", seriesMap.size());

            // 4. 批次計算
            Map<String, ChipResult> results = chipEngine.batchCompute(seriesMap, plan);
            log.info("計算完成: {} 支股票", results.size());

            // 5. 轉換並儲存結果
            int savedCount = saveResults(results, calculationDate);
            int signalCount = saveSignals(results, calculationDate);

            long duration = System.currentTimeMillis() - startTime;

            log.info("已儲存 {} 筆分析結果, {} 筆訊號", savedCount, signalCount);
            log.info("籌碼計算完成，耗時 {} ms", duration);

            // 6. 組裝統計資訊
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("calculation_date", calculationDate.toString());
            statistics.put("total_stocks", targetStocks.size());
            statistics.put("processed_stocks", seriesMap.size());
            statistics.put("success_count", savedCount);
            statistics.put("signal_count", signalCount);
            statistics.put("duration_ms", duration);

            return statistics;

        } catch (Exception e) {
            log.error("籌碼計算失敗", e);
            throw new RuntimeException("籌碼計算失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 計算單一股票籌碼（內部使用）
     */
    @Transactional
    public ChipResult calculateForStock(String stockId, LocalDate calculationDate) {
        log.debug("計算單一股票籌碼: stockId={}, date={}", stockId, calculationDate);

        // 1. 取得籌碼資料
        ChipSeries series = chipSeriesProvider.get(stockId, calculationDate, 60);
        if (series == null || series.isEmpty()) {
            log.warn("無籌碼資料: stockId={}", stockId);
            return null;
        }

        // 2. 建立預設計劃
        ChipPlan plan = ChipPlan.defaultPlan();

        // 3. 計算
        ChipResult result = chipEngine.compute(series, plan);

        // 4. 儲存
        if (result != null && !result.hasErrors()) {
            ChipAnalysisResult entity = resultConverter.toEntity(result);
            entity.setCalculationTimeMs((int) (System.currentTimeMillis() % 10000));
            analysisResultRepository.save(entity);

            // 儲存訊號
            if (result.hasSignals()) {
                List<ChipSignalEntity> signalEntities = signalConverter.toEntityList(
                        result.getSignals(), stockId, calculationDate);
                signalRepository.saveAll(signalEntities);
            }
        }

        return result;
    }

    // ========== 私有方法 ==========

    /**
     * 建立計算計劃
     */
    private ChipPlan createPlan(ChipCalculationRequest request) {
        ChipPlan.ChipPlanBuilder builder = ChipPlan.builder();

        // 根據請求設定計算類別
        if (Boolean.TRUE.equals(request.getIncludeInstitutional())) {
            builder.includeInstitutional(true);
        }
        if (Boolean.TRUE.equals(request.getIncludeMargin())) {
            builder.includeMargin(true);
        }
        if (Boolean.TRUE.equals(request.getIncludeSignals())) {
            builder.includeSignals(true);
        }

        // 根據優先級設定
        String priority = request.getPriority();
        if ("P0".equals(priority)) {
            // P0: 只計算核心指標
            builder.includeInstitutional(true)
                   .includeMargin(true)
                   .includeConcentration(false)
                   .includeCost(false)
                   .includeSignals(true);
        } else if ("P1".equals(priority)) {
            // P1: 核心 + 籌碼集中度
            builder.includeInstitutional(true)
                   .includeMargin(true)
                   .includeConcentration(true)
                   .includeCost(false)
                   .includeSignals(true);
        } else if ("P2".equals(priority)) {
            // P2: 全部指標
            builder.includeInstitutional(true)
                   .includeMargin(true)
                   .includeConcentration(true)
                   .includeCost(true)
                   .includeSignals(true);
        }

        return builder.build();
    }

    /**
     * 取得目標股票
     */
    private List<String> getTargetStocks(List<String> stockIds) {
        if (stockIds != null && !stockIds.isEmpty()) {
            return stockIds;
        }
        return stockRepository.findActiveStockIds();
    }

    /**
     * 儲存分析結果
     */
    private int saveResults(Map<String, ChipResult> results, LocalDate calculationDate) {
        List<ChipAnalysisResult> entities = new ArrayList<>();

        results.forEach((stockId, result) -> {
            if (result != null && !result.hasErrors()) {
                ChipAnalysisResult entity = resultConverter.toEntity(result);
                entities.add(entity);
            }
        });

        if (!entities.isEmpty()) {
            analysisResultRepository.saveAll(entities);
        }

        return entities.size();
    }

    /**
     * 儲存異常訊號
     */
    private int saveSignals(Map<String, ChipResult> results, LocalDate calculationDate) {
        List<ChipSignalEntity> allSignals = new ArrayList<>();

        results.forEach((stockId, result) -> {
            if (result != null && result.hasSignals()) {
                List<ChipSignalEntity> signalEntities = signalConverter.toEntityList(
                        result.getSignals(), stockId, calculationDate);
                allSignals.addAll(signalEntities);
            }
        });

        if (!allSignals.isEmpty()) {
            signalRepository.saveAll(allSignals);
        }

        return allSignals.size();
    }
}
