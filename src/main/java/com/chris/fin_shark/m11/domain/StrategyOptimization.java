package com.chris.fin_shark.m11.domain;

import com.chris.fin_shark.m11.enums.ObjectiveFunction;
import com.chris.fin_shark.m11.enums.OptimizationMethod;
import com.chris.fin_shark.m11.enums.OptimizationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 策略參數優化記錄實體
 * <p>
 * 對應資料表: strategy_optimizations
 * 儲存策略參數優化的執行記錄與結果
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyOptimization {

    /**
     * 優化 ID（格式: OPT_yyyyMMdd_xxx）
     */
    private String optimizationId;

    /**
     * 策略 ID
     */
    private String strategyId;

    /**
     * 策略版本
     */
    private Integer strategyVersion;

    /**
     * 優化方法
     */
    private OptimizationMethod optimizationMethod;

    /**
     * 目標函數
     */
    private ObjectiveFunction objectiveFunction;

    /**
     * 參數搜索空間配置（JSONB）
     */
    private Map<String, Object> parameterConfig;

    /**
     * 回測配置（JSONB）
     */
    private Map<String, Object> backtestConfig;

    /**
     * 優化狀態
     */
    @Builder.Default
    private OptimizationStatus status = OptimizationStatus.QUEUED;

    /**
     * 總參數組合數
     */
    private Integer totalCombinations;

    /**
     * 已完成組合數
     */
    @Builder.Default
    private Integer completedCombinations = 0;

    /**
     * 進度百分比
     */
    @Builder.Default
    private BigDecimal progressPercent = BigDecimal.ZERO;

    /**
     * 最佳參數組合（JSONB）
     */
    private Map<String, Object> bestParameters;

    /**
     * 最佳目標值
     */
    private BigDecimal bestObjectiveValue;

    /**
     * 所有結果（JSONB）
     */
    private Map<String, Object> allResults;

    /**
     * 執行時間（毫秒）
     */
    private Integer executionTimeMs;

    /**
     * 錯誤訊息
     */
    private String errorMessage;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;

    /**
     * 開始時間
     */
    private LocalDateTime startedAt;

    /**
     * 完成時間
     */
    private LocalDateTime completedAt;
}
