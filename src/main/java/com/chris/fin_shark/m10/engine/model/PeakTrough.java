package com.chris.fin_shark.m10.engine.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 波峰/波谷模型
 * <p>
 * 用於識別價格序列中的局部極值點
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeakTrough {

    /**
     * 極值類型
     */
    public enum Type {
        PEAK,   // 波峰（局部最高點）
        TROUGH  // 波谷（局部最低點）
    }

    /**
     * 類型（波峰或波谷）
     */
    private Type type;

    /**
     * 日期
     */
    private LocalDate date;

    /**
     * 價格
     */
    private BigDecimal price;

    /**
     * 在原始序列中的索引
     */
    private int index;

    /**
     * 強度（基於比較範圍）
     */
    private int strength;

    /**
     * 成交量
     */
    private Long volume;

    // === 判斷方法 ===

    /**
     * 是否為波峰
     */
    public boolean isPeak() {
        return type == Type.PEAK;
    }

    /**
     * 是否為波谷
     */
    public boolean isTrough() {
        return type == Type.TROUGH;
    }

    // === 比較方法 ===

    /**
     * 此極值點是否高於另一個
     */
    public boolean isHigherThan(PeakTrough other) {
        return this.price.compareTo(other.getPrice()) > 0;
    }

    /**
     * 此極值點是否低於另一個
     */
    public boolean isLowerThan(PeakTrough other) {
        return this.price.compareTo(other.getPrice()) < 0;
    }

    /**
     * 計算與另一個極值點的價差百分比
     */
    public double getPriceDifferencePercent(PeakTrough other) {
        if (other.getPrice().compareTo(BigDecimal.ZERO) == 0) {
            return 0;
        }
        return this.price.subtract(other.getPrice())
                .divide(other.getPrice(), 6, java.math.RoundingMode.HALF_UP)
                .doubleValue() * 100;
    }

    /**
     * 計算與另一個極值點的天數差
     */
    public long getDaysBetween(PeakTrough other) {
        return Math.abs(java.time.temporal.ChronoUnit.DAYS.between(this.date, other.getDate()));
    }

    // === 靜態工廠方法 ===

    /**
     * 建立波峰
     */
    public static PeakTrough peak(LocalDate date, BigDecimal price, int index) {
        return PeakTrough.builder()
                .type(Type.PEAK)
                .date(date)
                .price(price)
                .index(index)
                .build();
    }

    /**
     * 建立波谷
     */
    public static PeakTrough trough(LocalDate date, BigDecimal price, int index) {
        return PeakTrough.builder()
                .type(Type.TROUGH)
                .date(date)
                .price(price)
                .index(index)
                .build();
    }

    /**
     * 建立波峰（含成交量）
     */
    public static PeakTrough peak(LocalDate date, BigDecimal price, int index, Long volume) {
        return PeakTrough.builder()
                .type(Type.PEAK)
                .date(date)
                .price(price)
                .index(index)
                .volume(volume)
                .build();
    }

    /**
     * 建立波谷（含成交量）
     */
    public static PeakTrough trough(LocalDate date, BigDecimal price, int index, Long volume) {
        return PeakTrough.builder()
                .type(Type.TROUGH)
                .date(date)
                .price(price)
                .index(index)
                .volume(volume)
                .build();
    }
}
