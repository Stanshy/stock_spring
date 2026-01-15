package com.chris.fin_shark.m10.engine.detector;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import com.chris.fin_shark.m10.engine.PatternDetector;
import com.chris.fin_shark.m10.engine.model.CandleStick;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.engine.model.PatternMetadata;
import com.chris.fin_shark.m10.engine.model.PeakTrough;
import com.chris.fin_shark.m10.enums.PatternCategory;
import com.chris.fin_shark.m10.enums.PatternStatus;
import com.chris.fin_shark.m10.enums.SignalType;
import com.chris.fin_shark.m10.enums.TrendDirection;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

/**
 * 圖表型態偵測器基底類別
 * <p>
 * 提供圖表型態偵測的共用功能：
 * - 波峰波谷識別
 * - 趨勢線計算
 * - 頸線識別
 * - 型態完整性評估
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractChartPatternDetector implements PatternDetector {

    // === 常用閾值常數 ===

    /**
     * 波峰波谷容忍度（價格差異在此範圍內視為同一水平）
     */
    protected static final double LEVEL_TOLERANCE = 0.02; // 2%

    /**
     * 最小波動幅度（用於識別有效的波峰波谷）
     */
    protected static final double MIN_SWING_PERCENT = 0.03; // 3%

    /**
     * 突破確認閾值
     */
    protected static final double BREAKOUT_THRESHOLD = 0.01; // 1%

    /**
     * 型態元資料快取
     */
    protected final Map<String, PatternMetadata> metadataCache = new HashMap<>();

    // === 抽象方法 ===

    /**
     * 初始化元資料
     */
    protected abstract void initializeMetadata();

    /**
     * 執行型態偵測
     */
    protected abstract List<DetectedPattern> doDetect(
            List<CandleStick> candles,
            List<PeakTrough> peaksTroughs,
            Map<String, Object> params,
            TrendDirection trendContext
    );

    // === PatternDetector 介面實作 ===

    @Override
    public List<String> getSupportedPatternIds() {
        if (metadataCache.isEmpty()) {
            initializeMetadata();
        }
        return new ArrayList<>(metadataCache.keySet());
    }

    @Override
    public PatternMetadata getMetadata(String patternId) {
        if (metadataCache.isEmpty()) {
            initializeMetadata();
        }
        return metadataCache.get(patternId);
    }

    @Override
    public List<DetectedPattern> detect(PriceSeries series, Map<String, Object> params, TrendDirection context) {
        if (!hasEnoughData(series)) {
            log.debug("{}: 資料不足，需要 {} 天，實際 {} 天",
                    getName(), getMinDataPoints(), series.size());
            return Collections.emptyList();
        }

        // 轉換為 CandleStick
        List<CandleStick> candles = convertToCandleSticks(series);

        // 識別波峰波谷
        List<PeakTrough> peaksTroughs = findPeaksTroughs(candles, params);

        // 執行偵測
        return doDetect(candles, peaksTroughs, params, context);
    }

    // === 波峰波谷識別 ===

    /**
     * 識別波峰和波谷
     *
     * @param candles K 線資料
     * @param params  參數（可包含 swingThreshold）
     * @return 波峰波谷列表（按時間排序）
     */
    protected List<PeakTrough> findPeaksTroughs(List<CandleStick> candles, Map<String, Object> params) {
        List<PeakTrough> result = new ArrayList<>();

        double swingThreshold = (double) params.getOrDefault("swingThreshold", MIN_SWING_PERCENT);
        int lookback = (int) params.getOrDefault("pivotLookback", 5);

        for (int i = lookback; i < candles.size() - lookback; i++) {
            CandleStick current = candles.get(i);

            // 檢查是否為局部高點
            if (isPivotHigh(candles, i, lookback)) {
                // 驗證波動幅度
                if (isSignificantSwing(candles, i, lookback, swingThreshold, true)) {
                    result.add(PeakTrough.builder()
                            .type(PeakTrough.Type.PEAK)
                            .date(current.getDate())
                            .price(current.getHigh())
                            .index(i)
                            .strength(calculatePivotStrength(candles, i, true))
                            .build());
                }
            }

            // 檢查是否為局部低點
            if (isPivotLow(candles, i, lookback)) {
                if (isSignificantSwing(candles, i, lookback, swingThreshold, false)) {
                    result.add(PeakTrough.builder()
                            .type(PeakTrough.Type.TROUGH)
                            .date(current.getDate())
                            .price(current.getLow())
                            .index(i)
                            .strength(calculatePivotStrength(candles, i, false))
                            .build());
                }
            }
        }

        // 按時間排序
        result.sort(Comparator.comparing(PeakTrough::getDate));

        return result;
    }

    /**
     * 檢查是否為樞紐高點
     */
    protected boolean isPivotHigh(List<CandleStick> candles, int index, int lookback) {
        BigDecimal high = candles.get(index).getHigh();

        // 檢查左邊
        for (int i = index - lookback; i < index; i++) {
            if (candles.get(i).getHigh().compareTo(high) >= 0) {
                return false;
            }
        }

        // 檢查右邊
        for (int i = index + 1; i <= index + lookback; i++) {
            if (candles.get(i).getHigh().compareTo(high) >= 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 檢查是否為樞紐低點
     */
    protected boolean isPivotLow(List<CandleStick> candles, int index, int lookback) {
        BigDecimal low = candles.get(index).getLow();

        // 檢查左邊
        for (int i = index - lookback; i < index; i++) {
            if (candles.get(i).getLow().compareTo(low) <= 0) {
                return false;
            }
        }

        // 檢查右邊
        for (int i = index + 1; i <= index + lookback; i++) {
            if (candles.get(i).getLow().compareTo(low) <= 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * 檢查波動是否顯著
     */
    protected boolean isSignificantSwing(List<CandleStick> candles, int index, int lookback,
                                        double threshold, boolean isPeak) {
        BigDecimal pivotPrice = isPeak ? candles.get(index).getHigh() : candles.get(index).getLow();

        // 找出周圍的反向極值
        BigDecimal oppositeExtreme = isPeak ?
                findLowestLow(candles, index - lookback, index + lookback) :
                findHighestHigh(candles, index - lookback, index + lookback);

        // 計算波動幅度
        BigDecimal swing = pivotPrice.subtract(oppositeExtreme).abs();
        BigDecimal swingPercent = swing.divide(pivotPrice, 4, RoundingMode.HALF_UP);

        return swingPercent.doubleValue() >= threshold;
    }

    /**
     * 計算樞紐點強度
     */
    protected int calculatePivotStrength(List<CandleStick> candles, int index, boolean isPeak) {
        int strength = 50; // 基礎分數

        // 因子 1: 突出程度
        BigDecimal pivotPrice = isPeak ? candles.get(index).getHigh() : candles.get(index).getLow();
        int count = 0;
        for (int i = Math.max(0, index - 10); i < Math.min(candles.size(), index + 10); i++) {
            if (i == index) continue;
            BigDecimal comparePrice = isPeak ? candles.get(i).getHigh() : candles.get(i).getLow();
            if ((isPeak && pivotPrice.compareTo(comparePrice) > 0) ||
                (!isPeak && pivotPrice.compareTo(comparePrice) < 0)) {
                count++;
            }
        }
        strength += (count >= 15 ? 20 : count >= 10 ? 10 : 0);

        // 因子 2: 成交量放大
        if (candles.get(index).getVolume() != null) {
            long avgVol = calculateAverageVolume(candles, index, 20);
            if (candles.get(index).getVolume() > avgVol * 1.5) {
                strength += 15;
            }
        }

        return Math.min(100, strength);
    }

    // === 輔助方法 ===

    /**
     * 找出最高價
     */
    protected BigDecimal findHighestHigh(List<CandleStick> candles, int fromIdx, int toIdx) {
        BigDecimal highest = BigDecimal.ZERO;
        for (int i = Math.max(0, fromIdx); i <= Math.min(candles.size() - 1, toIdx); i++) {
            if (candles.get(i).getHigh().compareTo(highest) > 0) {
                highest = candles.get(i).getHigh();
            }
        }
        return highest;
    }

    /**
     * 找出最低價
     */
    protected BigDecimal findLowestLow(List<CandleStick> candles, int fromIdx, int toIdx) {
        BigDecimal lowest = BigDecimal.valueOf(Double.MAX_VALUE);
        for (int i = Math.max(0, fromIdx); i <= Math.min(candles.size() - 1, toIdx); i++) {
            if (candles.get(i).getLow().compareTo(lowest) < 0) {
                lowest = candles.get(i).getLow();
            }
        }
        return lowest;
    }

    /**
     * 計算平均成交量
     */
    protected long calculateAverageVolume(List<CandleStick> candles, int endIndex, int period) {
        int start = Math.max(0, endIndex - period);
        long total = 0L;
        int count = 0;

        for (int i = start; i < endIndex; i++) {
            if (candles.get(i).getVolume() != null) {
                total += candles.get(i).getVolume();
                count++;
            }
        }

        return count > 0 ? total / count : 0L;
    }

    /**
     * 檢查兩個價格是否在同一水平（容忍度內）
     */
    protected boolean isSameLevel(BigDecimal price1, BigDecimal price2, double tolerance) {
        BigDecimal diff = price1.subtract(price2).abs();
        BigDecimal avgPrice = price1.add(price2).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
        BigDecimal percentDiff = diff.divide(avgPrice, 4, RoundingMode.HALF_UP);
        return percentDiff.doubleValue() <= tolerance;
    }

    /**
     * 計算頸線
     */
    protected BigDecimal calculateNeckline(PeakTrough trough1, PeakTrough trough2) {
        return trough1.getPrice().add(trough2.getPrice())
                .divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
    }

    /**
     * 檢查是否突破頸線
     */
    protected boolean isBreakingNeckline(CandleStick candle, BigDecimal neckline, boolean isUpward) {
        if (isUpward) {
            return candle.getClose().compareTo(neckline.multiply(BigDecimal.valueOf(1 + BREAKOUT_THRESHOLD))) > 0;
        } else {
            return candle.getClose().compareTo(neckline.multiply(BigDecimal.valueOf(1 - BREAKOUT_THRESHOLD))) < 0;
        }
    }

    /**
     * 將 PriceSeries 轉換為 CandleStick 列表
     */
    protected List<CandleStick> convertToCandleSticks(PriceSeries series) {
        List<CandleStick> candles = new ArrayList<>();

        List<LocalDate> dates = series.getDates();
        double[] opens = series.getOpenArray();
        double[] highs = series.getHighArray();
        double[] lows = series.getLowArray();
        double[] closes = series.getCloseArray();
        long[] volumes = series.getVolumeArray();

        for (int i = 0; i < series.size(); i++) {
            candles.add(CandleStick.builder()
                    .date(dates.get(i))
                    .open(BigDecimal.valueOf(opens[i]))
                    .high(BigDecimal.valueOf(highs[i]))
                    .low(BigDecimal.valueOf(lows[i]))
                    .close(BigDecimal.valueOf(closes[i]))
                    .volume(volumes != null && volumes.length > i ? volumes[i] : 0L)
                    .build());
        }

        return candles;
    }

    // === 結果建構方法 ===

    /**
     * 建構圖表型態偵測結果
     */
    protected DetectedPattern buildChartPattern(
            String patternId,
            List<CandleStick> involvedCandles,
            int strength,
            TrendDirection trendContext,
            BigDecimal neckline,
            BigDecimal targetPrice,
            Map<String, Object> keyLevels) {

        PatternMetadata meta = getMetadata(patternId);
        if (meta == null) {
            log.warn("找不到型態元資料: {}", patternId);
            return null;
        }

        List<LocalDate> dates = involvedCandles.stream()
                .map(CandleStick::getDate)
                .toList();

        BigDecimal patternLow = involvedCandles.stream()
                .map(CandleStick::getLow)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        BigDecimal patternHigh = involvedCandles.stream()
                .map(CandleStick::getHigh)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        CandleStick lastCandle = involvedCandles.get(involvedCandles.size() - 1);

        // 計算止損
        BigDecimal stopLoss = null;
        if (meta.getSignalType() != null) {
            BigDecimal patternHeight = patternHigh.subtract(patternLow);
            if (meta.getSignalType().isBullish()) {
                stopLoss = patternLow.subtract(patternHeight.multiply(BigDecimal.valueOf(0.2)));
            } else if (meta.getSignalType().isBearish()) {
                stopLoss = patternHigh.add(patternHeight.multiply(BigDecimal.valueOf(0.2)));
            }
        }

        return DetectedPattern.builder()
                .patternId(patternId)
                .patternName(meta.getNameZh())
                .englishName(meta.getNameEn())
                .category(meta.getCategory())
                .signalType(meta.getSignalType())
                .status(PatternStatus.CONFIRMED)
                .strength(strength)
                .confidence(strength)
                .detectionDate(dates.get(dates.size() - 1))
                .involvedDates(dates)
                .involvedCandles(involvedCandles)
                .patternLow(patternLow)
                .patternHigh(patternHigh)
                .neckline(neckline)
                .targetPrice(targetPrice)
                .stopLoss(stopLoss)
                .trendContext(trendContext != null ? trendContext.name() : null)
                .keyLevels(keyLevels)
                .description(meta.getDescription())
                .build();
    }

    /**
     * 註冊元資料
     */
    protected void registerMetadata(PatternMetadata metadata) {
        metadataCache.put(metadata.getPatternId(), metadata);
    }

    /**
     * 建立標準元資料
     */
    protected PatternMetadata createMetadata(
            String patternId,
            String nameZh,
            String nameEn,
            PatternCategory category,
            SignalType signalType,
            int minDataPoints,
            String description) {

        return PatternMetadata.builder()
                .patternId(patternId)
                .nameZh(nameZh)
                .nameEn(nameEn)
                .category(category)
                .signalType(signalType)
                .priority("P0")
                .minDataPoints(minDataPoints)
                .recommendedDataPoints(minDataPoints * 2)
                .description(description)
                .enabled(true)
                .build();
    }
}
