package com.chris.fin_shark.m08.engine.calculator.profitability;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 每股盈餘 (EPS) 計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("EPS 計算器測試")
class EPSCalculatorTest {

    private EPSCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new EPSCalculator();
    }

    @Test
    @DisplayName("測試: 使用財報提供的 EPS")
    void testUsingProvidedEps() {
        // Given - 財報已提供 EPS
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .eps(BigDecimal.valueOf(25.50))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("eps");
        BigDecimal eps = result.getProfitabilityIndicators().get("eps");
        assertThat(eps).isEqualByComparingTo(BigDecimal.valueOf(25.50));
    }

    @Test
    @DisplayName("測試: 自行計算 EPS")
    void testCalculateEps() {
        // Given - 淨利 100000千元 = 100000000元, 流通股數 5000000股
        // EPS = 100000 * 1000 / 5000000 = 20.00
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .outstandingShares(5000000L)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("eps");
        BigDecimal eps = result.getProfitabilityIndicators().get("eps");
        assertThat(eps).isEqualByComparingTo(BigDecimal.valueOf(20.00));
    }

    @Test
    @DisplayName("測試: 缺少淨利且無 EPS")
    void testMissingNetIncomeAndEps() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .outstandingShares(5000000L)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("eps");
    }

    @Test
    @DisplayName("測試: 缺少流通股數且無 EPS")
    void testMissingSharesAndEps() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("eps");
    }

    @Test
    @DisplayName("測試: 流通股數為零")
    void testZeroShares() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .outstandingShares(0L)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("eps");
    }

    @Test
    @DisplayName("測試: 流通股數為負")
    void testNegativeShares() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(100000))
                .outstandingShares(-1000000L)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).doesNotContainKey("eps");
    }

    @Test
    @DisplayName("測試: 虧損公司 EPS 為負")
    void testNegativeEps() {
        // Given - 提供負 EPS
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .eps(BigDecimal.valueOf(-5.00))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("eps");
        BigDecimal eps = result.getProfitabilityIndicators().get("eps");
        assertThat(eps).isEqualByComparingTo(BigDecimal.valueOf(-5.00));
    }

    @Test
    @DisplayName("測試: 計算得出負 EPS")
    void testCalculateNegativeEps() {
        // Given - 淨利 -50000千元, 流通股數 5000000股
        // EPS = -50000 * 1000 / 5000000 = -10.00
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .netIncome(BigDecimal.valueOf(-50000))
                .outstandingShares(5000000L)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getProfitabilityIndicators()).containsKey("eps");
        BigDecimal eps = result.getProfitabilityIndicators().get("eps");
        assertThat(eps).isEqualByComparingTo(BigDecimal.valueOf(-10.00));
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("eps");
        assertThat(metadata.getCategory()).isEqualTo("PROFITABILITY");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("元");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
