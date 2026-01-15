package com.chris.fin_shark.m09.engine.calculator.margin;

import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 融資餘額計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("融資餘額計算器測試")
class MarginBalanceCalculatorTest {

    private MarginBalanceCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new MarginBalanceCalculator();
        System.out.println("\n========================================");
        System.out.println("  初始化 融資餘額計算器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: 基本資訊驗證")
    void testMetadata() {
        System.out.println("  測試: 基本資訊驗證");

        assertThat(calculator.getName()).isEqualTo("MARGIN_BALANCE");
        assertThat(calculator.getCategory()).isEqualTo(ChipCategory.MARGIN);
        assertThat(calculator.getMetadata().getNameZh()).isEqualTo("融資餘額");

        System.out.println("  測試通過: 基本資訊正確");
    }

    @Test
    @DisplayName("測試: 融資餘額與增減計算")
    void testMarginBalance_Calculation() {
        System.out.println("  測試: 融資餘額與增減計算");

        // Given - 10 天融資餘額資料（逐日增加）
        long[] marginBalanceData = {
                100000, 102000, 105000, 103000, 108000,
                110000, 112000, 115000, 118000, 120000
        };
        double[] marginUsageRateData = {
                40.0, 40.8, 42.0, 41.2, 43.2,
                44.0, 44.8, 46.0, 47.2, 48.0
        };

        System.out.println("  輸入資料:");
        System.out.println("    - 資料天數: " + marginBalanceData.length);
        System.out.println("    - 最後一天融資餘額: " + marginBalanceData[9]);
        System.out.println("    - 前一天融資餘額: " + marginBalanceData[8]);

        ChipSeries series = createMarginSeries("2330", marginBalanceData, marginUsageRateData);

        // When
        System.out.println("\n  執行計算...");
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - margin_balance: " + result.get("margin_balance"));
        System.out.println("    - margin_change: " + result.get("margin_change"));
        System.out.println("    - margin_usage_rate: " + result.get("margin_usage_rate"));
        System.out.println("    - margin_continuous_days: " + result.get("margin_continuous_days"));

        // 驗證融資餘額
        assertThat(result.get("margin_balance")).isEqualTo(120000L);

        // 驗證融資增減 (120000 - 118000 = 2000)
        assertThat(result.get("margin_change")).isEqualTo(2000L);

        // 驗證融資使用率
        assertThat(result.get("margin_usage_rate")).isEqualTo(48.0);

        // 驗證融資連續增加天數（最後 3 天: 115000->118000->120000 都是增加）
        assertThat((Integer) result.get("margin_continuous_days")).isGreaterThan(0);

        System.out.println("\n  測試通過: 融資餘額計算正確");
    }

    @Test
    @DisplayName("測試: 融資連續減少")
    void testMarginContinuousDecrease() {
        System.out.println("  測試: 融資連續減少");

        // Given - 融資連續減少的資料
        long[] marginBalanceData = {
                150000, 148000, 145000, 142000, 140000  // 連續減少
        };
        double[] marginUsageRateData = {60.0, 59.2, 58.0, 56.8, 56.0};

        System.out.println("  輸入資料:");
        System.out.println("    - 趨勢: 融資連續減少");

        ChipSeries series = createMarginSeries("2330", marginBalanceData, marginUsageRateData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - margin_continuous_days: " + result.get("margin_continuous_days"));

        // 連續減少返回負數
        int continuousDays = (Integer) result.get("margin_continuous_days");
        assertThat(continuousDays).isLessThan(0);
        assertThat(Math.abs(continuousDays)).isEqualTo(4);  // 4 天連續減少

        System.out.println("\n  測試通過: 融資連續減少識別正確");
    }

    @Test
    @DisplayName("測試: 融資增減 5 日均線")
    void testMarginChangeMa5() {
        System.out.println("  測試: 融資增減 5 日均線");

        // Given - 6 天融資餘額（產生 5 天增減資料）
        long[] marginBalanceData = {
                100000, 101000, 103000, 102000, 105000, 108000
        };
        double[] marginUsageRateData = {40.0, 40.4, 41.2, 40.8, 42.0, 43.2};

        System.out.println("  輸入資料:");
        System.out.println("    - 資料天數: " + marginBalanceData.length);

        ChipSeries series = createMarginSeries("2330", marginBalanceData, marginUsageRateData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - margin_change_ma5: " + result.get("margin_change_ma5"));

        // 增減序列: 1000, 2000, -1000, 3000, 3000
        // 最後 5 天均值: (1000+2000-1000+3000+3000)/5 = 1600
        assertThat(result).containsKey("margin_change_ma5");
        Double ma5 = (Double) result.get("margin_change_ma5");
        assertThat(ma5).isCloseTo(1600.0, org.assertj.core.data.Offset.offset(1.0));

        System.out.println("\n  測試通過: 融資增減 5 日均線計算正確");
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
    private ChipSeries createMarginSeries(String stockId, long[] marginBalanceData, double[] marginUsageRateData) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Long> marginList = new ArrayList<>();
        List<BigDecimal> usageRateList = new ArrayList<>();

        int size = marginBalanceData.length;
        for (int i = 0; i < size; i++) {
            dateList.add(LocalDate.now().minusDays(size - i - 1));
            marginList.add(marginBalanceData[i]);
            usageRateList.add(BigDecimal.valueOf(marginUsageRateData[i]));
        }

        return ChipSeries.builder()
                .stockId(stockId)
                .dates(dateList)
                .marginBalance(marginList)
                .marginUsageRate(usageRateList)
                .build();
    }
}
