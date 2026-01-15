package com.chris.fin_shark.m11.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 策略版本歷史實體
 * <p>
 * 對應資料表: strategy_versions
 * 儲存策略的版本快照，支援版本追溯與比較
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyVersion {

    /**
     * 版本記錄 ID（自增）
     */
    private Long versionId;

    /**
     * 策略 ID
     */
    private String strategyId;

    /**
     * 版本號
     */
    private Integer version;

    /**
     * 版本快照 - 策略名稱
     */
    private String strategyName;

    /**
     * 版本快照 - 描述
     */
    private String description;

    /**
     * 版本快照 - 條件定義
     */
    private Map<String, Object> conditions;

    /**
     * 版本快照 - 參數
     */
    private Map<String, Object> parameters;

    /**
     * 版本快照 - 輸出配置
     */
    private Map<String, Object> outputConfig;

    /**
     * 版本變更摘要
     */
    private String changeSummary;

    /**
     * 建立者
     */
    private String createdBy;

    /**
     * 建立時間
     */
    private LocalDateTime createdAt;
}
