package com.chris.fin_shark.m09.engine.calculator.institutional;

import com.chris.fin_shark.m09.engine.model.ChipSeries;
import com.chris.fin_shark.m09.enums.ChipCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 外資買賣超計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("外資買賣超計算器測試")
class ForeignNetCalculatorTest {

    private ForeignNetCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ForeignNetCalculator();
        System.out.println("\n========================================");
        System.out.println("  初始化 外資買賣超計算器測試");
        System.out.println("========================================\n");
    }

    @Test
    @DisplayName("測試: 基本資訊驗證")
    void testMetadata() {
        System.out.println("  測試: 基本資訊驗證");

        assertThat(calculator.getName()).isEqualTo("FOREIGN_NET");
        assertThat(calculator.getCategory()).isEqualTo(ChipCategory.INSTITUTIONAL);
        assertThat(calculator.getMetadata().getNameZh()).isEqualTo("外資買賣超");
        assertThat(calculator.getMetadata().getMinDataDays()).isEqualTo(20);

        System.out.println("  測試通過: 基本資訊正確");
    }

    @Test
    @DisplayName("測試: 外資買賣超計算")
    void testForeignNet_Calculation() {
        System.out.println("  測試: 外資買賣超計算");

        // Given - 20 天外資買賣超資料
        long[] foreignNetData = {
                1000000, 500000, -200000, 300000, 800000,   // 1-5
                1200000, -100000, 400000, 600000, 900000,   // 6-10
                -500000, 200000, 700000, 1100000, 1500000,  // 11-15
                800000, 300000, 1000000, 1300000, 2000000   // 16-20
        };
        long[] trustNetData = new long[20];
        long[] dealerNetData = new long[20];

        System.out.println("  輸入資料:");
        System.out.println("    - 資料天數: " + foreignNetData.length);
        System.out.println("    - 最後一天外資買賣超: " + foreignNetData[19]);

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        System.out.println("\n  執行計算...");
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - foreign_net: " + result.get("foreign_net"));
        System.out.println("    - foreign_net_ma5: " + result.get("foreign_net_ma5"));
        System.out.println("    - foreign_net_ma20: " + result.get("foreign_net_ma20"));
        System.out.println("    - foreign_accumulated_5d: " + result.get("foreign_accumulated_5d"));
        System.out.println("    - foreign_accumulated_20d: " + result.get("foreign_accumulated_20d"));

        // 驗證最後一天買賣超
        assertThat(result.get("foreign_net")).isEqualTo(2000000L);

        // 驗證 5 日均值 (最後 5 天: 800000+300000+1000000+1300000+2000000) / 5 = 1080000
        double expectedMa5 = (800000.0 + 300000 + 1000000 + 1300000 + 2000000) / 5;
        assertThat((Double) result.get("foreign_net_ma5")).isCloseTo(expectedMa5, org.assertj.core.data.Offset.offset(1.0));

        // 驗證 5 日累計
        long expectedAccum5d = 800000 + 300000 + 1000000 + 1300000 + 2000000;
        assertThat(result.get("foreign_accumulated_5d")).isEqualTo(expectedAccum5d);

        // 驗證有 20 日均值和累計
        assertThat(result).containsKey("foreign_net_ma20");
        assertThat(result).containsKey("foreign_accumulated_20d");

        System.out.println("\n  測試通過: 外資買賣超計算正確");
    }

    @Test
    @DisplayName("測試: 資料不足時的處理")
    void testInsufficientData() {
        System.out.println("  測試: 資料不足時的處理");

        // Given - 只有 3 天資料
        long[] foreignNetData = {1000000, 500000, 2000000};
        long[] trustNetData = new long[3];
        long[] dealerNetData = new long[3];

        System.out.println("  輸入資料:");
        System.out.println("    - 資料天數: " + foreignNetData.length);

        ChipSeries series = ChipSeries.createTestInstitutional("2330", foreignNetData, trustNetData, dealerNetData);

        // When
        Map<String, Object> result = calculator.calculate(series, Map.of());

        // Then
        System.out.println("\n  計算結果:");
        System.out.println("    - foreign_net: " + result.get("foreign_net"));
        System.out.println("    - foreign_net_ma5: " + result.get("foreign_net_ma5"));
        System.out.println("    - foreign_net_ma20: " + result.get("foreign_net_ma20"));

        // 應有最新買賣超
        assertThat(result.get("foreign_net")).isEqualTo(2000000L);
        // 不應有 5 日均值（資料不足）
        assertThat(result).doesNotContainKey("foreign_net_ma5");
        // 不應有 20 日均值
        assertThat(result).doesNotContainKey("foreign_net_ma20");

        System.out.println("\n  測試通過: 資料不足處理正確");
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
}
