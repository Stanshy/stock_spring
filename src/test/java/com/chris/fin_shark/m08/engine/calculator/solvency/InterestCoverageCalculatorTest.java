package com.chris.fin_shark.m08.engine.calculator.solvency;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 利息保障倍數計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("利息保障倍數計算器測試")
class InterestCoverageCalculatorTest {

    private InterestCoverageCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new InterestCoverageCalculator();
    }

    @Test
    @DisplayName("測試: 使用 EBIT 計算")
    void testCalculateWithEbit() {
        // Given - EBIT 100000, 利息 20000, 利息保障倍數 = 5.0
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .ebit(BigDecimal.valueOf(100000))
                .interestExpense(BigDecimal.valueOf(20000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("interest_coverage");
        BigDecimal interestCoverage = result.getSolvencyIndicators().get("interest_coverage");
        assertThat(interestCoverage).isEqualByComparingTo(BigDecimal.valueOf(5.00));
    }

    @Test
    @DisplayName("測試: 使用營業利益計算（無 EBIT）")
    void testCalculateWithOperatingIncome() {
        // Given - 無 EBIT，使用營業利益
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .operatingIncome(BigDecimal.valueOf(80000))
                .interestExpense(BigDecimal.valueOf(20000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("interest_coverage");
        BigDecimal interestCoverage = result.getSolvencyIndicators().get("interest_coverage");
        assertThat(interestCoverage).isEqualByComparingTo(BigDecimal.valueOf(4.00));
    }

    @Test
    @DisplayName("測試: 利息費用為零（無負債壓力）")
    void testZeroInterestExpense() {
        // Given - 無利息費用
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .ebit(BigDecimal.valueOf(100000))
                .interestExpense(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("interest_coverage");
        BigDecimal interestCoverage = result.getSolvencyIndicators().get("interest_coverage");
        assertThat(interestCoverage).isEqualByComparingTo(BigDecimal.valueOf(999.99));
    }

    @Test
    @DisplayName("測試: 缺少 EBIT 和營業利益")
    void testMissingEbitAndOperatingIncome() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .interestExpense(BigDecimal.valueOf(20000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("interest_coverage");
    }

    @Test
    @DisplayName("測試: 缺少利息費用")
    void testMissingInterestExpense() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .ebit(BigDecimal.valueOf(100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("interest_coverage");
    }

    @Test
    @DisplayName("測試: 利息費用為負（異常）")
    void testNegativeInterestExpense() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .ebit(BigDecimal.valueOf(100000))
                .interestExpense(BigDecimal.valueOf(-10000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).doesNotContainKey("interest_coverage");
    }

    @Test
    @DisplayName("測試: 利息保障倍數過低警告")
    void testLowInterestCoverageWarning() {
        // Given - 利息保障倍數 < 1.5
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .ebit(BigDecimal.valueOf(25000))
                .interestExpense(BigDecimal.valueOf(20000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("interest_coverage");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("利息保障倍數過低");
    }

    @Test
    @DisplayName("測試: 利息保障倍數小於1警告")
    void testInterestCoverageBelowOne() {
        // Given - 利息保障倍數 < 1
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .ebit(BigDecimal.valueOf(15000))
                .interestExpense(BigDecimal.valueOf(20000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("interest_coverage");
        assertThat(result.getDiagnostics().getWarnings().size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("測試: EBIT 為負")
    void testNegativeEbit() {
        // Given - EBIT 為負
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .ebit(BigDecimal.valueOf(-20000))
                .interestExpense(BigDecimal.valueOf(20000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getSolvencyIndicators()).containsKey("interest_coverage");
        BigDecimal interestCoverage = result.getSolvencyIndicators().get("interest_coverage");
        assertThat(interestCoverage).isEqualByComparingTo(BigDecimal.valueOf(-1.00));
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("interest_coverage");
        assertThat(metadata.getCategory()).isEqualTo("SOLVENCY");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("倍");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
