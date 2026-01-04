package com.chris.fin_shark.m07.service;

import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.dto.TradingCalendarDTO;
import com.chris.fin_shark.m06.repository.StockRepository;
import com.chris.fin_shark.m06.service.TradingCalendarService;
import com.chris.fin_shark.m07.converter.IndicatorCalculationJobConverter;
import com.chris.fin_shark.m07.domain.IndicatorCalculationJob;
import com.chris.fin_shark.m07.domain.TechnicalIndicator;
import com.chris.fin_shark.m07.dto.IndicatorCalculationJobDTO;
import com.chris.fin_shark.m07.dto.request.IndicatorCalculationRequest;
import com.chris.fin_shark.m07.engine.*;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import com.chris.fin_shark.m07.provider.PriceSeriesProvider;
import com.chris.fin_shark.m07.repository.IndicatorCalculationJobRepository;
import com.chris.fin_shark.m07.repository.TechnicalIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 指標計算服務（整合 Engine 版本）
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IndicatorCalculationService {


    private final IndicatorEngine engine;
    private final IndicatorRegistry registry;
    private final PriceSeriesProvider priceProvider;
    private final TechnicalIndicatorRepository indicatorRepository;
    private final IndicatorCalculationJobRepository jobRepository;
    private final IndicatorCalculationJobConverter jobConverter;
    private final TradingCalendarService tradingCalendarService;
    private final StockRepository stockRepository;

    /**
     * API-M07-007: 手動觸發指標計算
     */

    public IndicatorCalculationJobDTO triggerCalculation(IndicatorCalculationRequest request) {
        log.info("手動觸發指標計算: date={}, priority={}",
                request.getCalculationDate(),
                request.getIndicatorPriority());

        // 1. 建立 Job 記錄
        IndicatorCalculationJob job = IndicatorCalculationJob.builder()
                .jobType("CALCULATE_INDICATORS")
                .calculationDate(request.getCalculationDate())
                .stockList(request.getStockIds() != null
                        ? request.getStockIds().toArray(new String[0])
                        : null)
                .indicatorPriority(request.getIndicatorPriority())
                .status("PENDING")
                .statistics(new HashMap<>())
                .createdBy("SYSTEM")
                .build();

        IndicatorCalculationJob savedJob = jobRepository.save(job);



        return jobConverter.toDTO(savedJob);
    }

    /**
     * 計算技術指標（主邏輯 - 整合 Engine）
     */
    @Transactional
    public Long calculateIndicators(
            LocalDate calculationDate,
            List<String> stockIds,
            String indicatorPriority,
            Boolean forceRecalculate) {

        log.info("========================================");
        log.info("🚀 開始計算技術指標");
        log.info("========================================");
        log.info("計算日期: {}", calculationDate);
        log.info("優先級: {}", indicatorPriority);

        // 1. 建立 Job 記錄
        IndicatorCalculationJob job = createJob(calculationDate, stockIds, indicatorPriority);

        try {
            // 2. 更新狀態為 RUNNING
            job.setStatus("RUNNING");
            job.setStartTime(LocalDateTime.now());
            jobRepository.save(job);

            // ✅ 3. 使用 Engine 執行計算
            Map<String, IndicatorResult> results = executeWithEngine(
                    calculationDate,
                    stockIds,
                    indicatorPriority,
                    forceRecalculate
            );

            // 4. 轉換並儲存結果
            List<TechnicalIndicator> indicators = convertToEntities(results, calculationDate);
            indicatorRepository.saveAll(indicators);
            log.info("💾 已儲存 {} 筆指標資料", indicators.size());

            // 5. 更新 Job 狀態為成功
            job.setStatus("SUCCESS");
            job.setEndTime(LocalDateTime.now());
            job.setDurationSeconds(calculateDuration(job.getStartTime(), job.getEndTime()));
            job.setStatistics(buildStatistics(results));

            jobRepository.save(job);

            log.info("✅ 指標計算完成");
            return job.getJobId();

        } catch (Exception e) {
            log.error("❌ 指標計算失敗", e);
            updateJobAsFailed(job, e);
            throw new RuntimeException("指標計算失敗", e);
        }
    }

    /**
     * ✅ 使用 Engine 執行計算
     */
    private Map<String, IndicatorResult> executeWithEngine(
            LocalDate calculationDate,
            List<String> stockIds,
            String indicatorPriority,
            Boolean forceRecalculate) {

        // 1. 建立計算計劃
        IndicatorPlan plan = createPlan(indicatorPriority);
        log.info("📋 計算計劃: {}", plan.getIndicators().keySet());

        // 2. 取得目標股票
        List<String> targetStocks = getTargetStocks(stockIds);
        log.info("📈 目標股票數: {}", targetStocks.size());

        // 3. 批次取得價格資料
        Map<String, PriceSeries> seriesMap = priceProvider.getBatch(
                targetStocks,
                calculationDate,
                250  // 取最近 250 天
        );
        log.info("📊 已取得價格資料: {} 支股票", seriesMap.size());

        // 4. 批次計算指標
        Map<String, IndicatorResult> results = engine.batchCompute(seriesMap, plan);
        log.info("✅ 計算完成: {} 支股票", results.size());

        return results;
    }

    /**
     * 建立計算計劃
     */
    private IndicatorPlan createPlan(String priority) {
        if (priority == null || priority.isEmpty()) {
            // 計算所有指標
            return IndicatorPlan.builder()
                    .indicators(getAllIndicatorsWithDefaultParams())
                    .build();
        }

        // 根據優先級計算
        List<String> indicatorNames = registry.getIndicatorsByPriority(priority);

        Map<String, Map<String, Object>> indicators = new HashMap<>();
        for (String name : indicatorNames) {
            Map<String, Object> params = registry.getMetadata(name)
                    .map(IndicatorMetadata::getDefaultParams)
                    .orElse(Map.of());
            indicators.put(name, params);
        }

        return IndicatorPlan.builder()
                .indicators(indicators)
                .priority(priority)
                .build();
    }

    /**
     * 取得所有指標的預設參數
     */
    private Map<String, Map<String, Object>> getAllIndicatorsWithDefaultParams() {
        Map<String, Map<String, Object>> result = new HashMap<>();

        registry.getAllCalculators().forEach((name, calculator) -> {
            Map<String, Object> params = calculator.getMetadata().getDefaultParams();
            result.put(name, params);
        });

        return result;
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
     * 轉換為 Entity
     */
    private List<TechnicalIndicator> convertToEntities(
            Map<String, IndicatorResult> results,
            LocalDate calculationDate) {

        List<TechnicalIndicator> indicators = new ArrayList<>();

        results.forEach((stockId, result) -> {
            if (!result.hasErrors()) {

                // 先拿出四個 JSON 區塊
                Map<String, Object> trend = result.getTrendIndicators();        // MA / EMA / MACD
                Map<String, Object> momentum = result.getMomentumIndicators();  // RSI / Stoch
                Map<String, Object> volatility = result.getVolatilityIndicators(); // BBands / ATR
                Map<String, Object> volume = result.getVolumeIndicators();      // OBV 等

                TechnicalIndicator indicator = TechnicalIndicator.builder()
                        .stockId(stockId)
                        .calculationDate(calculationDate)
                        .calculationVersion("v2.0")
                        .calculationEngine("DefaultIndicatorEngine")

                        // 原本就有的 JSON 欄位
                        .trendIndicators(trend)
                        .momentumIndicators(momentum)
                        .volatilityIndicators(volatility)
                        .volumeIndicators(volume)

                        // ⭐ 新增：把常用指標展平到欄位

                        // MA / EMA
                        .ma5( toDecimal(get(trend, "ma5")) )
                        .ma20( toDecimal(get(trend, "ma20")) )
                        .ma60( toDecimal(get(trend, "ma60")) )
                        .ema12( toDecimal(get(trend, "ema12")) )
                        .ema26( toDecimal(get(trend, "ema26")) )

                        // MACD（你 JSON 裡 macd 是一個物件）
                        .macdValue( extractMacdLine(trend) )
                        .macdSignal( extractMacdHistogram(trend) )          // 這個是文字欄位
                        .macdHistogram( extractMacdHistogram(trend) )

                        // RSI
                        .rsi14( toDecimal(get(momentum, "rsi_14")) )

                        // Stochastic（如果你 Engine 有算）
                        .stochK( toDecimal(get(momentum, "stoch_k")) )
                        .stochD( toDecimal(get(momentum, "stoch_d")) )

                        // BBands
                        .bbandsUpper( extractBbands(volatility, "upper") )
                        .bbandsMiddle( extractBbands(volatility, "middle") )
                        .bbandsLower( extractBbands(volatility, "lower") )

                        // ATR / OBV / ADX 之類可以之後補
                        // .atr14( toDecimal(get(volatility, "atr_14")) )
                        // .obv( toDecimal(get(volume, "obv")) )
                        // .adx14( toDecimal(get(trend, "adx_14")) )

                        .build();

                indicators.add(indicator);
            }
        });

        return indicators;
    }

    /**
     * 建立 Job 記錄
     */
    private IndicatorCalculationJob createJob(
            LocalDate calculationDate,
            List<String> stockIds,
            String indicatorPriority) {

        return jobRepository.save(
                IndicatorCalculationJob.builder()
                        .jobType("CALCULATE_INDICATORS")
                        .calculationDate(calculationDate)
                        .stockList(stockIds != null ? stockIds.toArray(new String[0]) : null)
                        .indicatorPriority(indicatorPriority)
                        .status("PENDING")
                        .build()
        );
    }

    /**
     * 建立統計資訊
     */
    private Map<String, Object> buildStatistics(Map<String, IndicatorResult> results) {
        int totalStocks = results.size();
        int successCount = (int) results.values().stream()
                .filter(r -> !r.hasErrors())
                .count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("total_stocks", totalStocks);
        stats.put("success_count", successCount);
        stats.put("failed_count", totalStocks - successCount);

        return stats;
    }

    /**
     * 更新 Job 為失敗
     */
    private void updateJobAsFailed(IndicatorCalculationJob job, Exception e) {
        job.setStatus("FAILED");
        job.setEndTime(LocalDateTime.now());
        job.setDurationSeconds(calculateDuration(job.getStartTime(), job.getEndTime()));
        job.setErrorMessage(e.getMessage());
        job.setErrorStackTrace(getStackTrace(e));
        jobRepository.save(job);
    }

    /**
     * 計算執行時長
     */
    private Integer calculateDuration(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            return null;
        }
        return (int) ChronoUnit.SECONDS.between(startTime, endTime);
    }

    /**
     * 取得異常堆疊
     */
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }



    @SuppressWarnings("unchecked")
    private Object get(Map<String, Object> map, String key) {
        if (map == null) return null;
        return map.get(key);
    }

    private BigDecimal toDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }

    @SuppressWarnings("unchecked")
    private BigDecimal extractMacdLine(Map<String, Object> trend) {
        if (trend == null) return null;
        Object macdObj = trend.get("macd");
        if (macdObj instanceof Map<?, ?> macd) {
            Object v = macd.get("macd_line");   // 你 JSON 裡的 key 叫 macd_line
            return toDecimal(v);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private String extractMacdSignal(Map<String, Object> trend) {
        if (trend == null) return null;
        Object macdObj = trend.get("macd");
        if (macdObj instanceof Map<?, ?> macd) {
            Object v = macd.get("macd_signal"); // "BULLISH" / "BEARISH"
            return v != null ? v.toString() : null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BigDecimal extractMacdHistogram(Map<String, Object> trend) {
        if (trend == null) return null;
        Object macdObj = trend.get("macd");
        if (macdObj instanceof Map<?, ?> macd) {
            Object v = macd.get("histogram");
            return toDecimal(v);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private BigDecimal extractBbands(Map<String, Object> volatility, String field) {
        if (volatility == null) return null;
        Object bbandsObj = volatility.get("bbands");
        if (bbandsObj instanceof Map<?, ?> bbands) {
            Object v = bbands.get(field); // "upper" / "middle" / "lower"
            return toDecimal(v);
        }
        return null;
    }




    /**
     * 開發用：回填某檔股票在一段日期區間內的指標
     * 例如：近一年全部交易日
     */
    public void backfillIndicatorsForRange(
            String stockId,
            LocalDate startDate,
            LocalDate endDate,
            String indicatorPriority,
            boolean forceRecalculate
    ) {
        log.info("📆 開始回填指標: stockId={}, startDate={}, endDate={}, priority={}, force={}",
                stockId, startDate, endDate, indicatorPriority, forceRecalculate);

        if (stockId == null || stockId.isBlank()) {
            throw new IllegalArgumentException("stockId is required for backfill");
        }
        List<String> stocks = List.of(stockId);

        List<LocalDate> tradingDays =
                tradingCalendarService.getTradingDaysInRange(startDate, endDate)
                        .stream()
                        .map(TradingCalendarDTO::getCalendarDate)
                        .toList();

        for (LocalDate tradeDate : tradingDays) {

            // （可選）避免覆蓋
//            if (existsIndicator(stockId, tradeDate) && !force) {
//                continue;
//            }
                calculateIndicators(tradeDate, stocks, indicatorPriority, forceRecalculate);



        }

        log.info("✅ 回填完成: stockId={}, startDate={}, endDate={}", stockId, startDate, endDate);
    }
}
