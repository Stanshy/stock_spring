package com.chris.fin_shark.m08.engine.calculator.valuation;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PEG 比率計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("PEG 計算器測試")
class PEGCalculatorTest {

    private PEGCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new PEGCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算 PEG")
    void testNormalCalculation() {
        // Given - 股價 200, EPS 10, 去年EPS 8
        // EPS成長率 = (10-8)/8 = 25%
        // P/E = 200/10 = 20
        // PEG = 20/25 = 0.80
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(200))
                .eps(BigDecimal.valueOf(10))
                .lastYearEps(BigDecimal.valueOf(8))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("peg_ratio");
        BigDecimal pegRatio = result.getValuationIndicators().get("peg_ratio");
        assertThat(pegRatio).isEqualByComparingTo(BigDecimal.valueOf(0.80));
    }

    @Test
    @DisplayName("測試: 高成長股 PEG < 1")
    void testHighGrowthStock() {
        // Given - 股價 300, EPS 15, 去年EPS 5
        // EPS成長率 = (15-5)/5 = 200%
        // P/E = 300/15 = 20
        // PEG = 20/200 = 0.10
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(300))
                .eps(BigDecimal.valueOf(15))
                .lastYearEps(BigDecimal.valueOf(5))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("peg_ratio");
        BigDecimal pegRatio = result.getValuationIndicators().get("peg_ratio");
        assertThat(pegRatio.compareTo(BigDecimal.ONE)).isLessThan(0);
    }

    @Test
    @DisplayName("測試: 缺少股價")
    void testMissingStockPrice() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .eps(BigDecimal.valueOf(10))
                .lastYearEps(BigDecimal.valueOf(8))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("peg_ratio");
    }

    @Test
    @DisplayName("測試: 缺少去年 EPS")
    void testMissingLastYearEps() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(200))
                .eps(BigDecimal.valueOf(10))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("peg_ratio");
    }

    @Test
    @DisplayName("測試: EPS 為負")
    void testNegativeEps() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(200))
                .eps(BigDecimal.valueOf(-5))
                .lastYearEps(BigDecimal.valueOf(8))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("peg_ratio");
    }

    @Test
    @DisplayName("測試: 去年 EPS 為負")
    void testNegativeLastYearEps() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(200))
                .eps(BigDecimal.valueOf(10))
                .lastYearEps(BigDecimal.valueOf(-5))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("peg_ratio");
    }

    @Test
    @DisplayName("測試: EPS 成長率為負")
    void testNegativeGrowth() {
        // Given - EPS 從 10 跌到 8，成長率為負
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(200))
                .eps(BigDecimal.valueOf(8))
                .lastYearEps(BigDecimal.valueOf(10))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).doesNotContainKey("peg_ratio");
    }

    @Test
    @DisplayName("測試: PEG 異常高警告")
    void testHighPegWarning() {
        // Given - 股價 500, EPS 10, 去年EPS 9
        // EPS成長率 = (10-9)/9 = 11.11%
        // P/E = 500/10 = 50
        // PEG = 50/11.11 = 4.50
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .stockPrice(BigDecimal.valueOf(500))
                .eps(BigDecimal.valueOf(10))
                .lastYearEps(BigDecimal.valueOf(9))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getValuationIndicators()).containsKey("peg_ratio");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("PEG 異常高");
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("peg_ratio");
        assertThat(metadata.getCategory()).isEqualTo("VALUATION");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getRequiresHistory()).isTrue();
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
