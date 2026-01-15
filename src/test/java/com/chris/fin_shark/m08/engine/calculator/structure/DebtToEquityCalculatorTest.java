package com.chris.fin_shark.m08.engine.calculator.structure;

import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.Diagnostics;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 負債權益比計算器測試
 *
 * @author chris
 * @since 1.0.0
 */
@DisplayName("負債權益比計算器測試")
class DebtToEquityCalculatorTest {

    private DebtToEquityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DebtToEquityCalculator();
    }

    @Test
    @DisplayName("測試: 正常計算負債權益比")
    void testNormalCalculation() {
        // Given - 負債 400000, 權益 600000, D/E = 0.67
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(400000))
                .totalEquity(BigDecimal.valueOf(600000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("debt_to_equity");
        BigDecimal debtToEquity = result.getFinancialStructureIndicators().get("debt_to_equity");
        assertThat(debtToEquity).isEqualByComparingTo(BigDecimal.valueOf(0.67));
    }

    @Test
    @DisplayName("測試: 低槓桿公司")
    void testLowLeverageCompany() {
        // Given - 負債 100000, 權益 900000, D/E = 0.11
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(100000))
                .totalEquity(BigDecimal.valueOf(900000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("debt_to_equity");
        BigDecimal debtToEquity = result.getFinancialStructureIndicators().get("debt_to_equity");
        assertThat(debtToEquity).isEqualByComparingTo(BigDecimal.valueOf(0.11));
        assertThat(result.getDiagnostics().getWarnings()).isEmpty();
    }

    @Test
    @DisplayName("測試: 缺少負債")
    void testMissingLiabilities() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalEquity(BigDecimal.valueOf(600000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("debt_to_equity");
    }

    @Test
    @DisplayName("測試: 缺少權益")
    void testMissingEquity() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(400000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("debt_to_equity");
    }

    @Test
    @DisplayName("測試: 權益為零")
    void testZeroEquity() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(400000))
                .totalEquity(BigDecimal.ZERO)
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("debt_to_equity");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
    }

    @Test
    @DisplayName("測試: 權益為負")
    void testNegativeEquity() {
        // Given
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(400000))
                .totalEquity(BigDecimal.valueOf(-100000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).doesNotContainKey("debt_to_equity");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("股東權益為負或零");
    }

    @Test
    @DisplayName("測試: 高槓桿警告")
    void testHighLeverageWarning() {
        // Given - D/E > 2.0
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(700000))
                .totalEquity(BigDecimal.valueOf(300000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("debt_to_equity");
        assertThat(result.getDiagnostics().getWarnings()).isNotEmpty();
        assertThat(result.getDiagnostics().getWarnings().get(0)).contains("負債權益比過高");
    }

    @Test
    @DisplayName("測試: 極高槓桿")
    void testVeryHighLeverage() {
        // Given - D/E > 3.0
        FinancialData data = FinancialData.builder()
                .stockId("2330")
                .totalLiabilities(BigDecimal.valueOf(800000))
                .totalEquity(BigDecimal.valueOf(200000))
                .build();

        CalculationResult result = createResult();

        // When
        calculator.calculate(data, result);

        // Then
        assertThat(result.getFinancialStructureIndicators()).containsKey("debt_to_equity");
        BigDecimal debtToEquity = result.getFinancialStructureIndicators().get("debt_to_equity");
        assertThat(debtToEquity).isEqualByComparingTo(BigDecimal.valueOf(4.00));
    }

    @Test
    @DisplayName("測試: 元數據正確")
    void testMetadata() {
        // When
        var metadata = calculator.getMetadata();

        // Then
        assertThat(metadata.getName()).isEqualTo("debt_to_equity");
        assertThat(metadata.getCategory()).isEqualTo("FINANCIAL_STRUCTURE");
        assertThat(metadata.getPriority()).isEqualTo("P0");
        assertThat(metadata.getUnit()).isEqualTo("倍");
    }

    private CalculationResult createResult() {
        return CalculationResult.builder()
                .diagnostics(new Diagnostics())
                .build();
    }
}
