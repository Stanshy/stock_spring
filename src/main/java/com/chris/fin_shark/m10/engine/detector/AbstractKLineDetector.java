package com.chris.fin_shark.m10.engine.detector;

import com.chris.fin_shark.m07.engine.model.PriceSeries;
import com.chris.fin_shark.m10.engine.PatternDetector;
import com.chris.fin_shark.m10.engine.model.CandleStick;
import com.chris.fin_shark.m10.engine.model.DetectedPattern;
import com.chris.fin_shark.m10.engine.model.PatternMetadata;
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
 * K 線型態偵測器基底類別
 * <p>
 * 提供 K 線型態偵測的共用功能：
 * - 價格序列轉 CandleStick
 * - 常用比例計算
 * - 型態強度評估
 * - 結果建構
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractKLineDetector implements PatternDetector {

    // === 常用閾值常數 ===

    /**
     * 十字星實體比例閾值（實體 / 全距 <= 10%）
     */
    protected static final double DOJI_BODY_RATIO = 0.10;

    /**
     * 大實體比例閾值（實體 / 全距 >= 60%）
     */
    protected static final double LARGE_BODY_RATIO = 0.60;

    /**
     * 小實體比例閾值（實體 / 全距 <= 30%）
     */
    protected static final double SMALL_BODY_RATIO = 0.30;

    /**
     * 長影線倍數（影線 >= 實體的 2 倍）
     */
    protected static final double LONG_SHADOW_MULTIPLIER = 2.0;

    /**
     * 短影線比例（影線 <= 實體的 30%）
     */
    protected static final double SHORT_SHADOW_RATIO = 0.30;

    /**
     * 型態元資料快取
     */
    protected final Map<String, PatternMetadata> metadataCache = new HashMap<>();

    // === 抽象方法 ===

    /**
     * 初始化元資料
     * 子類需實作此方法來定義支援的型態
     */
    protected abstract void initializeMetadata();

    /**
     * 執行型態偵測
     *
     * @param candles      K 線資料
     * @param params       偵測參數
     * @param trendContext 趨勢背景
     * @return 偵測到的型態
     */
    protected abstract List<DetectedPattern> doDetect(
            List<CandleStick> candles,
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

        // 執行偵測
        return doDetect(candles, params, context);
    }

    // === 工具方法 ===

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

    /**
     * 取得最後 N 根 K 線
     */
    protected List<CandleStick> getLastNCandles(List<CandleStick> candles, int n) {
        int size = candles.size();
        if (size < n) {
            return candles;
        }
        return candles.subList(size - n, size);
    }

    /**
     * 計算平均成交量
     */
    protected long calculateAverageVolume(List<CandleStick> candles, int period) {
        if (candles.isEmpty()) {
            return 0L;
        }

        int count = Math.min(period, candles.size());
        long total = 0L;

        for (int i = candles.size() - count; i < candles.size(); i++) {
            total += candles.get(i).getVolume() != null ? candles.get(i).getVolume() : 0L;
        }

        return total / count;
    }

    /**
     * 計算型態強度基準分數
     *
     * @param baseScore 基本分數
     * @param factors   調整因子 (每個 true 加 10 分)
     */
    protected int calculateStrength(int baseScore, boolean... factors) {
        int strength = baseScore;
        for (boolean factor : factors) {
            if (factor) {
                strength += 10;
            }
        }
        return Math.min(100, Math.max(0, strength));
    }

    /**
     * 檢查成交量是否放大
     */
    protected boolean isVolumeIncreased(CandleStick candle, long avgVolume, double multiplier) {
        if (candle.getVolume() == null || avgVolume == 0) {
            return false;
        }
        return candle.getVolume() >= avgVolume * multiplier;
    }

    /**
     * 計算價格變化比例
     */
    protected double calculatePriceChange(CandleStick candle1, CandleStick candle2) {
        if (candle1.getClose().compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return candle2.getClose().subtract(candle1.getClose())
                .divide(candle1.getClose(), 4, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 取得高低點區間
     */
    protected BigDecimal[] getPriceRange(List<CandleStick> candles) {
        BigDecimal min = BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal max = BigDecimal.ZERO;

        for (CandleStick c : candles) {
            if (c.getLow().compareTo(min) < 0) min = c.getLow();
            if (c.getHigh().compareTo(max) > 0) max = c.getHigh();
        }

        return new BigDecimal[]{min, max};
    }

    /**
     * 判斷是否處於下跌趨勢（簡易判斷：收盤價低於前 N 天均價）
     */
    protected boolean isInDowntrend(List<CandleStick> candles, int lookback) {
        if (candles.size() < lookback + 1) {
            return false;
        }

        int startIdx = candles.size() - lookback - 1;
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = startIdx; i < candles.size() - 1; i++) {
            sum = sum.add(candles.get(i).getClose());
        }
        BigDecimal avg = sum.divide(BigDecimal.valueOf(lookback), 4, RoundingMode.HALF_UP);

        CandleStick last = candles.get(candles.size() - 1);
        return last.getClose().compareTo(avg) < 0;
    }

    /**
     * 判斷是否處於上漲趨勢
     */
    protected boolean isInUptrend(List<CandleStick> candles, int lookback) {
        if (candles.size() < lookback + 1) {
            return false;
        }

        int startIdx = candles.size() - lookback - 1;
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = startIdx; i < candles.size() - 1; i++) {
            sum = sum.add(candles.get(i).getClose());
        }
        BigDecimal avg = sum.divide(BigDecimal.valueOf(lookback), 4, RoundingMode.HALF_UP);

        CandleStick last = candles.get(candles.size() - 1);
        return last.getClose().compareTo(avg) > 0;
    }

    // === 結果建構方法 ===

    /**
     * 建構偵測結果
     */
    protected DetectedPattern buildPattern(
            String patternId,
            List<CandleStick> involvedCandles,
            int strength,
            TrendDirection trendContext,
            Map<String, Object> additionalData) {

        PatternMetadata meta = getMetadata(patternId);
        if (meta == null) {
            log.warn("找不到型態元資料: {}", patternId);
            return null;
        }

        List<LocalDate> dates = involvedCandles.stream()
                .map(CandleStick::getDate)
                .toList();

        BigDecimal[] range = getPriceRange(involvedCandles);

        DetectedPattern.DetectedPatternBuilder builder = DetectedPattern.builder()
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
                .patternLow(range[0])
                .patternHigh(range[1])
                .trendContext(trendContext != null ? trendContext.name() : null)
                .description(meta.getDescription());

        // 設定目標價和止損（簡易計算）
        BigDecimal priceRange = range[1].subtract(range[0]);
        CandleStick lastCandle = involvedCandles.get(involvedCandles.size() - 1);

        if (meta.getSignalType() != null && meta.getSignalType().isBullish()) {
            builder.targetPrice(lastCandle.getClose().add(priceRange));
            builder.stopLoss(range[0].subtract(priceRange.multiply(BigDecimal.valueOf(0.5))));
        } else if (meta.getSignalType() != null && meta.getSignalType().isBearish()) {
            builder.targetPrice(lastCandle.getClose().subtract(priceRange));
            builder.stopLoss(range[1].add(priceRange.multiply(BigDecimal.valueOf(0.5))));
        }

        // 設定成交量確認
        if (additionalData != null && additionalData.containsKey("volumeConfirmation")) {
            builder.volumeConfirmation((Boolean) additionalData.get("volumeConfirmation"));
        }

        // 設定成交量比率
        if (additionalData != null && additionalData.containsKey("volumeRatio")) {
            builder.volumeRatio((BigDecimal) additionalData.get("volumeRatio"));
        }

        // 設定關鍵價位
        if (additionalData != null && additionalData.containsKey("keyLevels")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> keyLevels = (Map<String, Object>) additionalData.get("keyLevels");
            builder.keyLevels(keyLevels);
        }

        return builder.build();
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
                .description(description)
                .enabled(true)
                .build();
    }
}
