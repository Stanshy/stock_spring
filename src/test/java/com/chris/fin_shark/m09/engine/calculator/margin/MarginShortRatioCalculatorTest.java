package com.chris.fin_shark.m09.engine.calculator.margin;

import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 券資比計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("券資比計算器測試")
class MarginShortRatioCalculatorTest {

    private MarginShortRatioCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MarginShortRatioCalculator();
        System.out.println("\n========================================");
        System.out.println("  初始化 券資比計算器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: 基本資訊驗證")
    void testMetadata() {
        System.out.println("  測試: 基本資訊驗證");

        assertThat(calculator.getName()).isEqualTo("MARGIN_SHORT_RATIO");
        assertThat(calculator.getCategory()).isEqualTo(ChipCategory.MARGIN);
        assertThat(calculator.getMetadata().getNameZh()).isEqualTo("券資比");

        System.out.println("  測試通過: 基本資訊正確");
    }

    @Test
    @DisplayName("測試: 正常券資比計算 (NORMAL)")
    void testNormalRatio() {
        System.out.println("  測試: 正常券資比計算 (NORMAL)");

        // Given - 券資比 = 10000 / 100000 * 100 = 10%
        long[] marginBalanceData = {100000};
        long[] shortBalanceData = {10000};

        System.out.println("  輸入資料:");
        System.out.println("    - 融資餘額: " + marginBalanceData[0]);
        System.out.println("    - 融券餘額: " + shortBalanceData[0]);
        System.out.println("    - 預期券資比: 10%");

        ChipSeries series = createMarginShortSeries("2330", marginBalanceData, shortBalanceData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - margin_short_ratio: " + result.get("margin_short_ratio"));
        System.out.println("    - margin_short_ratio_signal: " + result.get("margin_short_ratio_signal"));

        assertThat(result.get("margin_short_ratio")).isEqualTo(10.0);
        assertThat(result.get("margin_short_ratio_signal")).isEqualTo("NORMAL");

        System.out.println("\n  測試通過: 正常券資比計算正確");
    }

    @Test
    @DisplayName("測試: 高券資比訊號 (HIGH)")
    void testHighRatioSignal() {
        System.out.println("  測試: 高券資比訊號 (HIGH)");

        // Given - 券資比 = 35000 / 100000 * 100 = 35% > 30%
        long[] marginBalanceData = {100000};
        long[] shortBalanceData = {35000};

        System.out.println("  輸入資料:");
        System.out.println("    - 融資餘額: " + marginBalanceData[0]);
        System.out.println("    - 融券餘額: " + shortBalanceData[0]);
        System.out.println("    - 預期券資比: 35% (> 30% 閾值)");

        ChipSeries series = createMarginShortSeries("2330", marginBalanceData, shortBalanceData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - margin_short_ratio: " + result.get("margin_short_ratio"));
        System.out.println("    - margin_short_ratio_signal: " + result.get("margin_short_ratio_signal"));

        assertThat(result.get("margin_short_ratio")).isEqualTo(35.0);
        assertThat(result.get("margin_short_ratio_signal")).isEqualTo("HIGH");

        System.out.println("\n  測試通過: 高券資比訊號識別正確");
    }

    @Test
    @DisplayName("測試: 低券資比訊號 (LOW)")
    void testLowRatioSignal() {
        System.out.println("  測試: 低券資比訊號 (LOW)");

        // Given - 券資比 = 3000 / 100000 * 100 = 3% < 5%
        long[] marginBalanceData = {100000};
        long[] shortBalanceData = {3000};

        System.out.println("  輸入資料:");
        System.out.println("    - 融資餘額: " + marginBalanceData[0]);
        System.out.println("    - 融券餘額: " + shortBalanceData[0]);
        System.out.println("    - 預期券資比: 3% (< 5% 閾值)");

        ChipSeries series = createMarginShortSeries("2330", marginBalanceData, shortBalanceData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - margin_short_ratio: " + result.get("margin_short_ratio"));
        System.out.println("    - margin_short_ratio_signal: " + result.get("margin_short_ratio_signal"));

        assertThat(result.get("margin_short_ratio")).isEqualTo(3.0);
        assertThat(result.get("margin_short_ratio_signal")).isEqualTo("LOW");

        System.out.println("\n  測試通過: 低券資比訊號識別正確");
    }

    @Test
    @DisplayName("測試: 融資餘額為零時的處理")
    void testZeroMarginBalance() {
        System.out.println("  測試: 融資餘額為零時的處理");

        // Given - 融資餘額為 0
        long[] marginBalanceData = {0};
        long[] shortBalanceData = {10000};

        ChipSeries series = createMarginShortSeries("2330", marginBalanceData, shortBalanceData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("  計算結果:");
        System.out.println("    - margin_short_ratio: " + result.get("margin_short_ratio"));

        // 除以零應返回 0
        assertThat(result.get("margin_short_ratio")).isEqualTo(0.0);

        System.out.println("\n  測試通過: 零值處理正確");
    }

    @Test
    @DisplayName("測試: 空資料處理")
    void testEmptyData() {
        System.out.println("  測試: 空資料處理");

        // Given - 空資料
        ChipSeries series = ChipSeries.builder()
                .stockId("2330")
                .build();

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        assertThat(result).isEmpty();

        System.out.println("  測試通過: 空資料處理正確");
    }

    /**
     * 建立融資融券測試序列
     */
    private ChipSeries createMarginShortSeries(String stockId, long[] marginBalanceData, long[] shortBalanceData) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Long> marginList = new ArrayList<>();
        List<Long> shortList = new ArrayList<>();

        int size = marginBalanceData.length;
        for (int i = 0; i < size; i++) {
            dateList.add(LocalDate.now().minusDays(size - i - 1));
            marginList.add(marginBalanceData[i]);
            shortList.add(shortBalanceData[i]);
        }

        return ChipSeries.builder()
                .stockId(stockId)
                .dates(dateList)
                .marginBalance(marginList)
                .shortBalance(shortList)
                .build();
    }
}
