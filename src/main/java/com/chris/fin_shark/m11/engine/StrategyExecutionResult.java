package com.chris.fin_shark.m11.engine;

import com.chris.fin_shark.m11.domain.StrategySignal;
import com.chris.fin_shark.m11.enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 策略執行結果
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyExecutionResult {

    /**
     * 執行 ID
     */
    private String executionId;

    /**
     * 策略 ID
     */
    private String strategyId;

    /**
     * 策略名稱
     */
    private String strategyName;

    /**
     * 執行日期
     */
    private LocalDate executionDate;

    /**
     * 評估股票數
     */
    private int stocksEvaluated;

    /**
     * 產生的信號列表
     */
    @Builder.Default
    private List<StrategySignal> signals = new ArrayList<>();

    /**
     * 執行時間（毫秒）
     */
    private long executionTimeMs;

    /**
     * 執行狀態
     */
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.SUCCESS;

    /**
     * 錯誤訊息
     */
    private String errorMessage;

    /**
     * 診斷資訊
     */
    @Builder.Default
    private Diagnostics diagnostics = new Diagnostics();

    /**
     * 執行時間
     */
    private LocalDateTime executedAt;

    // ==================== 便捷方法 ====================

    /**
     * 取得信號數量
     */
    public int getSignalCount() {
        return signals != null ? signals.size() : 0;
    }

    /**
     * 取得買進信號數
     */
    public int getBuySignalCount() {
        if (signals == null) return 0;
        return (int) signals.stream()
                .filter(s -> "BUY".equals(s.getSignalType().name()))
                .count();
    }

    /**
     * 取得賣出信號數
     */
    public int getSellSignalCount() {
        if (signals == null) return 0;
        return (int) signals.stream()
                .filter(s -> "SELL".equals(s.getSignalType().name()))
                .count();
    }

    /**
     * 取得平均信心度
     */
    public BigDecimal getAvgConfidence() {
        if (signals == null || signals.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sum = signals.stream()
                .map(StrategySignal::getConfidenceScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(BigDecimal.valueOf(signals.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 新增信號
     */
    public void addSignal(StrategySignal signal) {
        if (signals == null) {
            signals = new ArrayList<>();
        }
        signals.add(signal);
    }

    /**
     * 標記為失敗
     */
    public void markFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.errorMessage = errorMessage;
    }
}
