package com.chris.fin_shark.m10.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * K 線資料模型
 * <p>
 * 封裝單根 K 線的 OHLCV 資料及計算方法
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CandleStick {

    /**
     * 交易日期
     */
    private LocalDate date;

    /**
     * 開盤價
     */
    private BigDecimal open;

    /**
     * 最高價
     */
    private BigDecimal high;

    /**
     * 最低價
     */
    private BigDecimal low;

    /**
     * 收盤價
     */
    private BigDecimal close;

    /**
     * 成交量
     */
    private Long volume;

    // === 基本計算方法 ===

    /**
     * 取得實體大小（絕對值）
     */
    public BigDecimal getBody() {
        return close.subtract(open).abs();
    }

    /**
     * 取得實體（含正負號，正為陽線，負為陰線）
     */
    public BigDecimal getSignedBody() {
        return close.subtract(open);
    }

    /**
     * 取得上影線長度
     */
    public BigDecimal getUpperShadow() {
        BigDecimal bodyHigh = open.max(close);
        return high.subtract(bodyHigh);
    }

    /**
     * 取得下影線長度
     */
    public BigDecimal getLowerShadow() {
        BigDecimal bodyLow = open.min(close);
        return bodyLow.subtract(low);
    }

    /**
     * 取得全距（最高到最低）
     */
    public BigDecimal getRange() {
        return high.subtract(low);
    }

    /**
     * 取得實體中點
     */
    public BigDecimal getBodyMidpoint() {
        return open.add(close).divide(BigDecimal.valueOf(2), 4, RoundingMode.HALF_UP);
    }

    // === 判斷方法 ===

    /**
     * 是否為陽線（收盤 > 開盤）
     */
    public boolean isBullish() {
        return close.compareTo(open) > 0;
    }

    /**
     * 是否為陰線（收盤 < 開盤）
     */
    public boolean isBearish() {
        return close.compareTo(open) < 0;
    }

    /**
     * 是否為平盤（收盤 = 開盤）
     */
    public boolean isDoji() {
        return close.compareTo(open) == 0;
    }

    /**
     * 是否為近似十字星（實體小於全距的指定比例）
     *
     * @param threshold 閾值比例（如 0.1 表示實體小於全距的 10%）
     */
    public boolean isNearDoji(double threshold) {
        BigDecimal range = getRange();
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        BigDecimal ratio = getBody().divide(range, 4, RoundingMode.HALF_UP);
        return ratio.doubleValue() <= threshold;
    }

    /**
     * 是否有長下影線
     *
     * @param multiplier 下影線對實體的倍數（如 2.0 表示下影線 >= 實體的 2 倍）
     */
    public boolean hasLongLowerShadow(double multiplier) {
        BigDecimal body = getBody();
        if (body.compareTo(BigDecimal.ZERO) == 0) {
            return getLowerShadow().compareTo(BigDecimal.ZERO) > 0;
        }
        return getLowerShadow().doubleValue() >= body.doubleValue() * multiplier;
    }

    /**
     * 是否有長上影線
     *
     * @param multiplier 上影線對實體的倍數
     */
    public boolean hasLongUpperShadow(double multiplier) {
        BigDecimal body = getBody();
        if (body.compareTo(BigDecimal.ZERO) == 0) {
            return getUpperShadow().compareTo(BigDecimal.ZERO) > 0;
        }
        return getUpperShadow().doubleValue() >= body.doubleValue() * multiplier;
    }

    /**
     * 是否有短上影線
     *
     * @param maxRatio 上影線對實體的最大比例
     */
    public boolean hasShortUpperShadow(double maxRatio) {
        BigDecimal body = getBody();
        if (body.compareTo(BigDecimal.ZERO) == 0) {
            return getUpperShadow().compareTo(BigDecimal.ZERO) == 0;
        }
        return getUpperShadow().doubleValue() <= body.doubleValue() * maxRatio;
    }

    /**
     * 是否有短下影線
     *
     * @param maxRatio 下影線對實體的最大比例
     */
    public boolean hasShortLowerShadow(double maxRatio) {
        BigDecimal body = getBody();
        if (body.compareTo(BigDecimal.ZERO) == 0) {
            return getLowerShadow().compareTo(BigDecimal.ZERO) == 0;
        }
        return getLowerShadow().doubleValue() <= body.doubleValue() * maxRatio;
    }

    /**
     * 是否為大實體 K 線
     *
     * @param minRatio 實體對全距的最小比例（如 0.6 表示實體 >= 全距的 60%）
     */
    public boolean isLargeBody(double minRatio) {
        BigDecimal range = getRange();
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }
        BigDecimal ratio = getBody().divide(range, 4, RoundingMode.HALF_UP);
        return ratio.doubleValue() >= minRatio;
    }

    /**
     * 是否為小實體 K 線
     *
     * @param maxRatio 實體對全距的最大比例
     */
    public boolean isSmallBody(double maxRatio) {
        BigDecimal range = getRange();
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        BigDecimal ratio = getBody().divide(range, 4, RoundingMode.HALF_UP);
        return ratio.doubleValue() <= maxRatio;
    }

    // === 吞噬判斷 ===

    /**
     * 此 K 線是否吞噬另一根 K 線
     *
     * @param other 被吞噬的 K 線
     * @return true 如果此 K 線的實體完全包覆 other 的實體
     */
    public boolean engulfs(CandleStick other) {
        BigDecimal thisBodyHigh = open.max(close);
        BigDecimal thisBodyLow = open.min(close);
        BigDecimal otherBodyHigh = other.getOpen().max(other.getClose());
        BigDecimal otherBodyLow = other.getOpen().min(other.getClose());

        return thisBodyHigh.compareTo(otherBodyHigh) >= 0 &&
               thisBodyLow.compareTo(otherBodyLow) <= 0;
    }

    /**
     * 此 K 線是否被另一根 K 線包含（孕線）
     *
     * @param other 外層 K 線
     * @return true 如果此 K 線的實體完全在 other 的實體內
     */
    public boolean isInsideOf(CandleStick other) {
        BigDecimal thisBodyHigh = open.max(close);
        BigDecimal thisBodyLow = open.min(close);
        BigDecimal otherBodyHigh = other.getOpen().max(other.getClose());
        BigDecimal otherBodyLow = other.getOpen().min(other.getClose());

        return thisBodyHigh.compareTo(otherBodyHigh) <= 0 &&
               thisBodyLow.compareTo(otherBodyLow) >= 0;
    }

    // === 工具方法 ===

    /**
     * 從 double 陣列建立（用於測試）
     */
    public static CandleStick of(LocalDate date, double open, double high, double low, double close, long volume) {
        return CandleStick.builder()
                .date(date)
                .open(BigDecimal.valueOf(open))
                .high(BigDecimal.valueOf(high))
                .low(BigDecimal.valueOf(low))
                .close(BigDecimal.valueOf(close))
                .volume(volume)
                .build();
    }

    /**
     * 從 double 陣列建立（無成交量）
     */
    public static CandleStick of(LocalDate date, double open, double high, double low, double close) {
        return of(date, open, high, low, close, 0L);
    }
}
