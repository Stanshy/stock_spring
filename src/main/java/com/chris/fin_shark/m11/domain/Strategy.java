package com.chris.fin_shark.m11.domain;

import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.enums.StrategyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 策略定義實體
 * <p>
 * 對應資料表: strategies
 * 使用 MyBatis 進行持久化，conditions/parameters/outputConfig 為 JSONB
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Strategy {

    /**
     * 策略唯一識別碼
     */
    private String strategyId;

    /**
     * 策略名稱
     */
    private String strategyName;

    /**
     * 策略類型
     */
    private StrategyType strategyType;

    /**
     * 策略描述
     */
    private String description;

    /**
     * 當前版本號
     */
    @Builder.Default
    private Integer currentVersion = 1;

    /**
     * 策略狀態
     */
    @Builder.Default
    private StrategyStatus status = StrategyStatus.DRAFT;

    /**
     * 是否為預設策略
     */
    @Builder.Default
    private Boolean isPreset = false;

    /**
     * 策略條件定義（JSONB）
     * <pre>
     * {
     *   "logic": "AND",
     *   "conditions": [
     *     {"factor_id": "M07_RSI_14", "operator": "LESS_THAN", "value": 30},
     *     ...
     *   ]
     * }
     * </pre>
     */
    private Map<String, Object> conditions;

    /**
     * 策略參數（可調整）
     */
    private Map<String, Object> parameters;

    /**
     * 輸出配置（信號類型、信心度公式）
     */
    private Map<String, Object> outputConfig;

    /**
     * 總執行次數（快取）
     */
    @Builder.Default
    private Integer totalExecutions = 0;

    /**
     * 總信號數（快取）
     */
    @Builder.Default
    private Integer totalSignals = 0;

    /**
     * 最後執行時間
     */
    private LocalDateTime lastExecutionAt;

    /**
     * 建立者
     */
    private String createdBy;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;

    /**
     * 更新時間
     */
    private LocalDateTime updatedAt;
}
