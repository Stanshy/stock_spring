package com.chris.fin_shark.m11.domain;

import com.chris.fin_shark.m11.enums.ExecutionStatus;
import com.chris.fin_shark.m11.enums.ExecutionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 策略執行記錄實體
 * <p>
 * 對應資料表: strategy_executions
 * 記錄每次策略執行的詳細資訊與統計
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyExecution {

    /**
     * 執行 ID（格式: EXEC_yyyyMMdd_xxx）
     */
    private String executionId;

    /**
     * 策略 ID
     */
    private String strategyId;

    /**
     * 策略版本
     */
    private Integer strategyVersion;

    /**
     * 執行日期
     */
    private LocalDate executionDate;

    /**
     * 執行類型
     */
    @Builder.Default
    private ExecutionType executionType = ExecutionType.SCHEDULED;

    /**
     * 股票範圍配置（JSONB）
     */
    private Map<String, Object> stockUniverse;

    /**
     * 評估股票數
     */
    private Integer stocksEvaluated;

    /**
     * 產生信號數
     */
    @Builder.Default
    private Integer signalsGenerated = 0;

    /**
     * 買進信號數
     */
    @Builder.Default
    private Integer buySignals = 0;

    /**
     * 賣出信號數
     */
    @Builder.Default
    private Integer sellSignals = 0;

    /**
     * 持有信號數
     */
    @Builder.Default
    private Integer holdSignals = 0;

    /**
     * 平均信心度
     */
    private BigDecimal avgConfidence;

    /**
     * 執行時間（毫秒）
     */
    private Integer executionTimeMs;

    /**
     * 執行狀態
     */
    @Builder.Default
    private ExecutionStatus status = ExecutionStatus.RUNNING;

    /**
     * 錯誤訊息
     */
    private String errorMessage;

    /**
     * 診斷資訊（JSONB）
     */
    private Map<String, Object> diagnostics;

    /**
     * 開始時間
     */
    private LocalDateTime startedAt;

    /**
     * 完成時間
     */
    private LocalDateTime completedAt;
}
