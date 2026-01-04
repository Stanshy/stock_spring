package com.chris.fin_shark.m07.service;

import com.chris.fin_shark.m07.converter.TechnicalIndicatorConverter;
import com.chris.fin_shark.m07.domain.TechnicalIndicator;
import com.chris.fin_shark.m07.dto.TechnicalIndicatorDTO;
import com.chris.fin_shark.m07.dto.response.*;
import com.chris.fin_shark.m07.dto.response.CrossSignalsResponse.CrossSignal;
import com.chris.fin_shark.m07.dto.response.OverboughtOversoldResponse.OverboughtOversoldSignal;
import com.chris.fin_shark.m07.dto.response.SpecificIndicatorResponse.IndicatorStatistics;
import com.chris.fin_shark.m07.dto.response.SpecificIndicatorResponse.IndicatorValue;
import com.chris.fin_shark.m07.exception.IndicatorNotFoundException;
import com.chris.fin_shark.m07.mapper.TechnicalIndicatorMapper;
import com.chris.fin_shark.m07.repository.TechnicalIndicatorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 指標查詢服務
 * <p>
 * 提供技術指標的查詢功能
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IndicatorQueryService {

    private final TechnicalIndicatorRepository indicatorRepository;
    private final TechnicalIndicatorMapper indicatorMapper;
    private final TechnicalIndicatorConverter indicatorConverter;

    /**
     * API-M07-001: 查詢單一股票技術指標
     *
     * @param stockId    股票代碼
     * @param startDate  開始日期
     * @param endDate    結束日期
     * @param indicators 指標名稱清單（逗號分隔）
     * @param categories 指標類別（逗號分隔）
     * @return 股票技術指標回應
     */
    @Transactional(readOnly = true)
    public StockIndicatorsResponse getStockIndicators(
            String stockId,
            LocalDate startDate,
            LocalDate endDate,
            String indicators,
            String categories) {

        log.debug("查詢股票指標: stockId={}, startDate={}, endDate={}",
                stockId, startDate, endDate);

        // 1. 查詢技術指標資料
        List<TechnicalIndicator> indicatorList = indicatorRepository
                .findByStockIdAndCalculationDateBetween(stockId, startDate, endDate);

        if (indicatorList.isEmpty()) {
            throw IndicatorNotFoundException.of(stockId, startDate);
        }

        // 2. 轉換為 IndicatorDataPoint
        List<IndicatorDataPoint> dataPoints = indicatorList.stream()
                .map(this::convertToDataPoint)
                .collect(Collectors.toList());

        // 3. 組裝回應
        return StockIndicatorsResponse.builder()
                .stockId(stockId)
                .stockName(getStockName(stockId)) // TODO: 從 M06 StockService 取得
                .indicators(dataPoints)
                .totalCount(dataPoints.size())
                .build();
    }

    /**
     * API-M07-002: 查詢單一股票特定指標
     *
     * @param stockId       股票代碼
     * @param indicatorName 指標名稱
     * @param startDate     開始日期
     * @param endDate       結束日期
     * @return 特定指標回應
     */
    @Transactional(readOnly = true)
    public SpecificIndicatorResponse getSpecificIndicator(
            String stockId,
            String indicatorName,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("查詢特定指標: stockId={}, indicator={}", stockId, indicatorName);

        // 1. 查詢技術指標資料
        List<TechnicalIndicator> indicatorList = indicatorRepository
                .findByStockIdAndCalculationDateBetween(stockId, startDate, endDate);

        if (indicatorList.isEmpty()) {
            throw IndicatorNotFoundException.of(stockId, startDate);
        }

        // 2. 提取特定指標的數值
        List<IndicatorValue> values = indicatorList.stream()
                .map(ind -> extractIndicatorValue(ind, indicatorName))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            throw IndicatorNotFoundException.of(
                    String.format("Indicator '%s' not found for stock '%s'", indicatorName, stockId)
            );
        }

        // 3. 計算統計資訊
        IndicatorStatistics statistics = calculateStatistics(values);

        // 4. 組裝回應
        return SpecificIndicatorResponse.builder()
                .stockId(stockId)
                .indicatorName(indicatorName)
                .indicatorParams(getIndicatorParams(indicatorName)) // TODO: 從 DefinitionService 取得
                .values(values)
                .totalCount(values.size())
                .statistics(statistics)
                .build();
    }

    /**
     * API-M07-003: 批次查詢最新指標
     *
     * @param stockIds   股票代碼清單（逗號分隔）
     * @param indicators 指標名稱清單（逗號分隔）
     * @return 最新指標列表
     */
    @Transactional(readOnly = true)
    public List<LatestIndicatorsResponse> getLatestIndicators(
            String stockIds,
            String indicators) {

        log.debug("批次查詢最新指標: stockIds={}", stockIds);

        // 1. 解析股票代碼清單
        List<String> stockIdList = Arrays.asList(stockIds.split(","));

        // 2. 批次查詢最新指標
        List<TechnicalIndicator> latestIndicators = indicatorMapper
                .findLatestIndicators(stockIdList);

        // 3. 轉換為回應格式
        return latestIndicators.stream()
                .map(this::convertToLatestIndicatorResponse)
                .collect(Collectors.toList());
    }

    /**
     * API-M07-004: 查詢交叉信號
     *
     * @param crossType  交叉類型（GOLDEN, DEATH, KD）
     * @param date       查詢日期
     * @param marketType 市場類型（TWSE, OTC）
     * @return 交叉信號回應
     */
    @Transactional(readOnly = true)
    public CrossSignalsResponse getCrossSignals(
            String crossType,
            LocalDate date,
            String marketType) {

        log.debug("查詢交叉信號: type={}, date={}", crossType, date);

        List<Map<String, Object>> rawSignals;

        // 1. 根據交叉類型查詢
        if ("GOLDEN".equalsIgnoreCase(crossType)) {
            rawSignals = indicatorMapper.findGoldenCrossCandidates(date);
        } else if ("DEATH".equalsIgnoreCase(crossType)) {
            rawSignals = indicatorMapper.findDeathCrossCandidates(date);
        } else {
            // 查詢全部
            rawSignals = new ArrayList<>();
            rawSignals.addAll(indicatorMapper.findGoldenCrossCandidates(date));
            rawSignals.addAll(indicatorMapper.findDeathCrossCandidates(date));
        }

        // 2. 過濾市場類型（如果有指定）
        if (marketType != null && !marketType.isEmpty()) {
            // TODO: 根據 marketType 過濾（需要 JOIN stocks 表）
        }

        // 3. 轉換為強型別信號
        List<CrossSignal> signals = rawSignals.stream()
                .map(this::convertToCrossSignal)
                .collect(Collectors.toList());

        // 4. 組裝回應
        return CrossSignalsResponse.builder()
                .crossDate(date)
                .signals(signals)
                .totalCount(signals.size())
                .build();
    }

    /**
     * API-M07-005: 查詢超買超賣信號
     *
     * @param signalType 信號類型（OVERBOUGHT, OVERSOLD）
     * @param indicator  指標（RSI, KD, WILLIAMS_R）
     * @param date       查詢日期
     * @return 超買超賣信號回應
     */
    @Transactional(readOnly = true)
    public OverboughtOversoldResponse getOverboughtOversoldSignals(
            String signalType,
            String indicator,
            LocalDate date) {

        log.debug("查詢超買超賣信號: type={}, indicator={}", signalType, indicator);

        List<Map<String, Object>> rawSignals;

        // 1. 根據信號類型查詢
        if ("OVERBOUGHT".equalsIgnoreCase(signalType)) {
            rawSignals = indicatorMapper.findOverboughtStocks(date);
        } else if ("OVERSOLD".equalsIgnoreCase(signalType)) {
            rawSignals = indicatorMapper.findOversoldStocks(date);
        } else {
            // 查詢全部
            rawSignals = new ArrayList<>();
            rawSignals.addAll(indicatorMapper.findOverboughtStocks(date));
            rawSignals.addAll(indicatorMapper.findOversoldStocks(date));
        }

        // 2. 過濾指標類型（如果有指定）
        if (indicator != null && !indicator.isEmpty()) {
            // 根據指標過濾（RSI, KD 等）
            // TODO: 在 MyBatis 層面實作更好
        }

        // 3. 轉換為強型別信號
        List<OverboughtOversoldSignal> signals = rawSignals.stream()
                .map(this::convertToOverboughtOversoldSignal)
                .collect(Collectors.toList());

        // 4. 組裝回應
        return OverboughtOversoldResponse.builder()
                .signalDate(date)
                .signals(signals)
                .totalCount(signals.size())
                .build();
    }

    /**
     * 查詢股票的最新指標（內部使用）
     *
     * @param stockId 股票代碼
     * @return 最新技術指標
     * @throws IndicatorNotFoundException 當指標不存在時
     */
    @Transactional(readOnly = true)
    public TechnicalIndicatorDTO getLatestIndicatorByStock(String stockId) {
        log.debug("查詢最新指標: stockId={}", stockId);

        TechnicalIndicator indicator = indicatorRepository
                .findLatestByStockId(stockId)
                .orElseThrow(() -> IndicatorNotFoundException.ofStock(stockId));

        return indicatorConverter.toDTO(indicator);
    }

    // ========== 私有輔助方法 ==========

    /**
     * 轉換為 IndicatorDataPoint
     */
    private IndicatorDataPoint convertToDataPoint(TechnicalIndicator indicator) {
        return IndicatorDataPoint.builder()
                .calculationDate(indicator.getCalculationDate())
                .trend(indicator.getTrendIndicators())
                .momentum(indicator.getMomentumIndicators())
                .volatility(indicator.getVolatilityIndicators())
                .volume(indicator.getVolumeIndicators())
                .build();
    }

    /**
     * 提取特定指標的數值
     */
    private IndicatorValue extractIndicatorValue(TechnicalIndicator indicator, String indicatorName) {
        Object value = null;

        // 根據指標名稱從對應的 JSONB 欄位提取數值
        switch (indicatorName.toUpperCase()) {
            case "MA5":
                value = indicator.getMa5();
                break;
            case "MA20":
                value = indicator.getMa20();
                break;
            case "MA60":
                value = indicator.getMa60();
                break;
            case "RSI":
            case "RSI_14":
                value = indicator.getRsi14();
                break;
            case "MACD":
                // MACD 是複合指標，返回物件
                if (indicator.getMacdValue() != null) {
                    value = Map.of(
                            "macd_line", indicator.getMacdValue(),
                            "signal_line", indicator.getMacdSignal(),
                            "histogram", indicator.getMacdHistogram()
                    );
                }
                break;
            case "KD":
            case "STOCHASTIC":
                if (indicator.getStochK() != null) {
                    value = Map.of(
                            "k", indicator.getStochK(),
                            "d", indicator.getStochD()
                    );
                }
                break;
            default:
                // 從 JSONB 欄位中查找
                value = extractFromJsonb(indicator, indicatorName);
                break;
        }

        if (value == null) {
            return null;
        }

        return IndicatorValue.builder()
                .date(indicator.getCalculationDate().toString())
                .value(value)
                .build();
    }

    /**
     * 從 JSONB 欄位提取指標值
     */
    private Object extractFromJsonb(TechnicalIndicator indicator, String indicatorName) {
        // 優先從趨勢指標找
        if (indicator.getTrendIndicators() != null
                && indicator.getTrendIndicators().containsKey(indicatorName)) {
            return indicator.getTrendIndicators().get(indicatorName);
        }

        // 再從動能指標找
        if (indicator.getMomentumIndicators() != null
                && indicator.getMomentumIndicators().containsKey(indicatorName)) {
            return indicator.getMomentumIndicators().get(indicatorName);
        }

        // 波動性指標
        if (indicator.getVolatilityIndicators() != null
                && indicator.getVolatilityIndicators().containsKey(indicatorName)) {
            return indicator.getVolatilityIndicators().get(indicatorName);
        }

        // 成交量指標
        if (indicator.getVolumeIndicators() != null
                && indicator.getVolumeIndicators().containsKey(indicatorName)) {
            return indicator.getVolumeIndicators().get(indicatorName);
        }

        return null;
    }

    /**
     * 計算統計資訊
     */
    private IndicatorStatistics calculateStatistics(List<IndicatorValue> values) {
        if (values.isEmpty()) {
            return null;
        }

        // 提取數值（假設都是 BigDecimal 或可轉換為 Double）
        List<Double> numericValues = values.stream()
                .map(v -> {
                    Object val = v.getValue();
                    if (val instanceof BigDecimal) {
                        return ((BigDecimal) val).doubleValue();
                    } else if (val instanceof Number) {
                        return ((Number) val).doubleValue();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (numericValues.isEmpty()) {
            return null;
        }

        double max = numericValues.stream().max(Double::compare).orElse(0.0);
        double min = numericValues.stream().min(Double::compare).orElse(0.0);
        double avg = numericValues.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double current = numericValues.get(numericValues.size() - 1);
        double previous = numericValues.size() > 1 ? numericValues.get(numericValues.size() - 2) : current;
        double change = current - previous;

        return IndicatorStatistics.builder()
                .max(max)
                .min(min)
                .avg(avg)
                .current(current)
                .previous(previous)
                .change(change)
                .build();
    }

    /**
     * 轉換為 LatestIndicatorsResponse
     */
    private LatestIndicatorsResponse convertToLatestIndicatorResponse(TechnicalIndicator indicator) {
        Map<String, Object> macd = null;
        if (indicator.getMacdValue() != null) {
            macd = Map.of(
                    "macd_line", indicator.getMacdValue(),
                    "signal_line", indicator.getMacdSignal(),
                    "histogram", indicator.getMacdHistogram()
            );
        }

        return LatestIndicatorsResponse.builder()
                .stockId(indicator.getStockId())
                .stockName(getStockName(indicator.getStockId())) // TODO: 從 M06 取得
                .calculationDate(indicator.getCalculationDate())
                .ma5(indicator.getMa5())
                .ma20(indicator.getMa20())
                .rsi14(indicator.getRsi14())
                .macd(macd)
                .build();
    }

    /**
     * 轉換 Map 為 CrossSignal
     */
    private CrossSignal convertToCrossSignal(Map<String, Object> raw) {
        return CrossSignal.builder()
                .stockId((String) raw.get("stock_id"))
                .stockName(getStockName((String) raw.get("stock_id"))) // TODO: 從 M06 取得
                .crossType((String) raw.get("signal_type"))
                .indicator("MA")
                .shortPeriod(5)
                .longPeriod(20)
                .shortValue((BigDecimal) raw.get("today_ma5"))
                .longValue((BigDecimal) raw.get("today_ma20"))
                .previousShort((BigDecimal) raw.get("yesterday_ma5"))
                .previousLong((BigDecimal) raw.get("yesterday_ma20"))
                .signalStrength(calculateSignalStrength(raw)) // TODO: 計算信號強度
                .confidenceScore(calculateConfidenceScore(raw)) // TODO: 計算信心分數
                .build();
    }

    /**
     * 轉換 Map 為 OverboughtOversoldSignal
     */
    private OverboughtOversoldSignal convertToOverboughtOversoldSignal(Map<String, Object> raw) {
        String signalType = (String) raw.get("signal_type");
        BigDecimal rsi = (BigDecimal) raw.get("rsi_14");
        BigDecimal threshold = "OVERBOUGHT".equals(signalType)
                ? new BigDecimal("70.00")
                : new BigDecimal("30.00");

        return OverboughtOversoldSignal.builder()
                .stockId((String) raw.get("stock_id"))
                .stockName(getStockName((String) raw.get("stock_id"))) // TODO: 從 M06 取得
                .signalType(signalType)
                .indicator("RSI")
                .indicatorValue(rsi)
                .threshold(threshold)
                .durationDays(1) // TODO: 計算持續天數
                .signalStrength(calculateOBOSStrength(rsi, threshold))
                .confidenceScore(calculateOBOSConfidence(rsi, threshold))
                .build();
    }

    /**
     * 計算交叉信號強度
     */
    private String calculateSignalStrength(Map<String, Object> raw) {
        // TODO: 根據交叉幅度、成交量等計算
        return "MEDIUM";
    }

    /**
     * 計算交叉信號信心分數
     */
    private Integer calculateConfidenceScore(Map<String, Object> raw) {
        // TODO: 根據多種因素計算 0-100 的分數
        return 65;
    }

    /**
     * 計算超買超賣信號強度
     */
    private String calculateOBOSStrength(BigDecimal value, BigDecimal threshold) {
        BigDecimal diff = value.subtract(threshold).abs();
        if (diff.compareTo(new BigDecimal("10")) > 0) {
            return "STRONG";
        } else if (diff.compareTo(new BigDecimal("5")) > 0) {
            return "MEDIUM";
        } else {
            return "WEAK";
        }
    }

    /**
     * 計算超買超賣信號信心分數
     */
    private Integer calculateOBOSConfidence(BigDecimal value, BigDecimal threshold) {
        BigDecimal diff = value.subtract(threshold).abs();
        return Math.min(100, diff.multiply(new BigDecimal("5")).intValue());
    }

    /**
     * 取得股票名稱
     * TODO: 整合 M06 StockService
     */
    private String getStockName(String stockId) {
        // 暫時返回空字串，待整合 M06
        return "";
    }

    /**
     * 取得指標參數
     * TODO: 整合 IndicatorDefinitionService
     */
    private Map<String, Object> getIndicatorParams(String indicatorName) {
        // 暫時返回預設參數
        return Map.of("period", 14);
    }
}
