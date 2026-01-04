package com.chris.fin_shark.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 數學計算工具
 *
 * 提供高精度計算和常用數學函數
 *
 * @author chris
 * @since 2025-12-24
 */
public final class MathUtil {

    private MathUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 預設精度（小數位數）
     */
    private static final int DEFAULT_SCALE = 4;

    /**
     * 預設捨入模式（四捨五入）
     */
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    // ========================================================================
    // 高精度計算（使用 BigDecimal 避免浮點誤差）
    // ========================================================================

    /**
     * 加法
     *
     * @param v1 被加數
     * @param v2 加數
     * @return 和
     */
    public static double add(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.add(b2).doubleValue();
    }

    /**
     * 減法
     *
     * @param v1 被減數
     * @param v2 減數
     * @return 差
     */
    public static double subtract(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.subtract(b2).doubleValue();
    }

    /**
     * 乘法
     *
     * @param v1 被乘數
     * @param v2 乘數
     * @return 積
     */
    public static double multiply(double v1, double v2) {
        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.multiply(b2).doubleValue();
    }

    /**
     * 除法
     *
     * @param v1 被除數
     * @param v2 除數
     * @return 商
     */
    public static double divide(double v1, double v2) {
        return divide(v1, v2, DEFAULT_SCALE);
    }

    /**
     * 除法（指定精度）
     *
     * @param v1 被除數
     * @param v2 除數
     * @param scale 精度（小數位數）
     * @return 商
     */
    public static double divide(double v1, double v2, int scale) {
        if (v2 == 0) {
            throw new ArithmeticException("Division by zero");
        }

        BigDecimal b1 = BigDecimal.valueOf(v1);
        BigDecimal b2 = BigDecimal.valueOf(v2);
        return b1.divide(b2, scale, DEFAULT_ROUNDING_MODE).doubleValue();
    }

    /**
     * 四捨五入
     *
     * @param value 值
     * @param scale 精度（小數位數）
     * @return 四捨五入後的值
     */
    public static double round(double value, int scale) {
        BigDecimal b = BigDecimal.valueOf(value);
        return b.setScale(scale, DEFAULT_ROUNDING_MODE).doubleValue();
    }

    // ========================================================================
    // 百分比計算
    // ========================================================================

    /**
     * 計算百分比變化
     *
     * @param oldValue 舊值
     * @param newValue 新值
     * @return 百分比變化（例如: 10.5 表示增加 10.5%）
     */
    public static double calculatePercentageChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return 0;
        }
        return divide((newValue - oldValue) * 100, oldValue, 2);
    }

    /**
     * 計算漲跌幅
     *
     * @param previousClose 前收盤價
     * @param currentPrice 當前價格
     * @return 漲跌幅（%）
     */
    public static double calculatePriceChangeRate(double previousClose, double currentPrice) {
        return calculatePercentageChange(previousClose, currentPrice);
    }

    // ========================================================================
    // 統計計算
    // ========================================================================

    /**
     * 計算平均值
     *
     * @param values 數值陣列
     * @return 平均值
     */
    public static double average(double... values) {
        if (values == null || values.length == 0) {
            return 0;
        }

        double sum = 0;
        for (double value : values) {
            sum = add(sum, value);
        }
        return divide(sum, values.length);
    }

    /**
     * 計算最大值
     *
     * @param values 數值陣列
     * @return 最大值
     */
    public static double max(double... values) {
        if (values == null || values.length == 0) {
            return 0;
        }

        double max = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > max) {
                max = values[i];
            }
        }
        return max;
    }

    /**
     * 計算最小值
     *
     * @param values 數值陣列
     * @return 最小值
     */
    public static double min(double... values) {
        if (values == null || values.length == 0) {
            return 0;
        }

        double min = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < min) {
                min = values[i];
            }
        }
        return min;
    }

    /**
     * 計算標準差
     *
     * @param values 數值陣列
     * @return 標準差
     */
    public static double standardDeviation(double... values) {
        if (values == null || values.length == 0) {
            return 0;
        }

        // 計算平均值
        double mean = average(values);

        // 計算方差
        double variance = 0;
        for (double value : values) {
            double diff = subtract(value, mean);
            variance = add(variance, multiply(diff, diff));
        }
        variance = divide(variance, values.length);

        // 返回標準差（方差的平方根）
        return Math.sqrt(variance);
    }

    // TODO: M07 開發時補充技術指標計算方法
    // 範例:
    // public static double calculateSMA(List<Double> prices, int period) { ... }
    // public static double calculateEMA(List<Double> prices, int period) { ... }
    // public static double calculateRSI(List<Double> prices, int period) { ... }
    // public static double calculateMACD(List<Double> prices) { ... }
    // public static double calculateBollingerBands(List<Double> prices, int period) { ... }

    // TODO: 各模組開發時，可以在此補充數學計算方法
}
