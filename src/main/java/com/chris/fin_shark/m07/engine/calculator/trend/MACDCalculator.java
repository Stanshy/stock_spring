package com.chris.fin_shark.m07.engine.calculator.trend;

import com.chris.fin_shark.m07.engine.calculator.IndicatorCalculator;
import com.chris.fin_shark.m07.engine.model.IndicatorMetadata;
import com.chris.fin_shark.m07.engine.model.PriceSeries;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MACD（指數平滑異同移動平均線）計算器
 * <p>
 * 計算公式：
 * MACD Line = EMA(12) - EMA(26)
 * Signal Line = EMA(9) of MACD Line
 * Histogram = MACD Line - Signal Line
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class MACDCalculator implements IndicatorCalculator {

    private final EMACalculator emaCalculator;

    @Override
    public String getName() {
        return "MACD";
    }

    @Override
    public String getCategory() {
        return "TREND";
    }

    @Override
    public IndicatorMetadata getMetadata() {
        return IndicatorMetadata.builder()
                .name("MACD")
                .category("TREND")
                .nameZh("MACD指標")
                .description("指數平滑異同移動平均線")
                .minDataPoints(35)  // 26 + 9
                .defaultParams(Map.of(
                        "fast", 12,
                        "slow", 26,
                        "signal", 9
                ))
                .priority("P0")
                .build();
    }

    @Override
    public Map<String, Object> calculate(PriceSeries series, Map<String, Object> params) {
        // 取得參數
        int fast = (int) params.getOrDefault("fast", 12);
        int slow = (int) params.getOrDefault("slow", 26);
        int signal = (int) params.getOrDefault("signal", 9);

        // 取得收盤價
        double[] closePrices = series.getCloseArray();

        if (closePrices.length < slow + signal) {
            // 資料不足，返回空結果
            return Map.of();
        }

        // 計算 EMA
        double[] emaFast = emaCalculator.calculateEMASeries(closePrices, fast);
        double[] emaSlow = emaCalculator.calculateEMASeries(closePrices, slow);

        // 計算 MACD Line
        double[] macdLine = new double[closePrices.length];
        for (int i = slow - 1; i < closePrices.length; i++) {
            macdLine[i] = emaFast[i] - emaSlow[i];
        }

        // 計算 Signal Line（MACD 的 EMA）
        double[] signalLine = calculateSignalLine(macdLine, slow - 1, signal);

        // 計算 Histogram
        int lastIndex = closePrices.length - 1;
        double macdValue = macdLine[lastIndex];
        double signalValue = signalLine[lastIndex];
        double histogram = macdValue - signalValue;

        // 組裝結果（MACD 是複合指標，用 Map 包裝）
        Map<String, Object> macdResult = new HashMap<>();
        macdResult.put("macd_line", round(macdValue));
        macdResult.put("signal_line", round(signalValue));
        macdResult.put("histogram", round(histogram));
        macdResult.put("macd_signal", getSignal(macdValue, signalValue, histogram));

        // 返回
        Map<String, Object> result = new HashMap<>();
        result.put("macd", macdResult);

        return result;
    }

    /**
     * 計算 Signal Line
     */
    private double[] calculateSignalLine(double[] macdLine, int startIndex, int period) {
        // 從 MACD Line 中提取有效資料
        int validLength = macdLine.length - startIndex;
        double[] validMacd = new double[validLength];
        System.arraycopy(macdLine, startIndex, validMacd, 0, validLength);

        // 計算 Signal Line（MACD 的 EMA）
        double[] signalFull = emaCalculator.calculateEMASeries(validMacd, period);

        // 將結果對齊到原始陣列長度
        double[] signal = new double[macdLine.length];
        System.arraycopy(signalFull, 0, signal, startIndex, signalFull.length);

        return signal;
    }

    /**
     * 判斷信號
     */
    private String getSignal(double macd, double signal, double histogram) {
        if (histogram > 0 && macd > signal) {
            return "BULLISH";  // 多頭
        } else if (histogram < 0 && macd < signal) {
            return "BEARISH";  // 空頭
        } else {
            return "NEUTRAL";  // 中性
        }
    }

    /**
     * 四捨五入
     */
    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
