package com.chris.fin_shark.m07.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 指標定義資料傳輸物件
 * <p>
 * 用於 API 請求和回應
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorDefinitionDTO {

    /** 定義ID */
    @JsonProperty("definition_id")
    private Long definitionId;

    /** 指標名稱（英文） */
    @JsonProperty("indicator_name")
    private String indicatorName;

    /** 指標類別 */
    @JsonProperty("indicator_category")
    private String indicatorCategory;

    /** 指標名稱（中文） */
    @JsonProperty("indicator_name_zh")
    private String indicatorNameZh;

    /** 指標說明 */
    @JsonProperty("description")
    private String description;

    /** 預設參數 */
    @JsonProperty("default_params")
    private Map<String, Object> defaultParams;

    /** 參數範圍 */
    @JsonProperty("param_ranges")
    private Map<String, Object> paramRanges;

    /** 計算公式說明 */
    @JsonProperty("calculation_formula")
    private String calculationFormula;

    /** pandas-ta 函數名稱 */
    @JsonProperty("pandas_ta_function")
    private String pandasTaFunction;

    /** 最少資料點數 */
    @JsonProperty("min_data_points")
    private Integer minDataPoints;

    /** 輸出欄位 */
    @JsonProperty("output_fields")
    private Map<String, Object> outputFields;

    /** 數值範圍 */
    @JsonProperty("value_range")
    private Map<String, Object> valueRange;

    /** 優先級 */
    @JsonProperty("priority")
    private String priority;

    /** 是否啟用 */
    @JsonProperty("is_active")
    private Boolean isActive;

    /** 是否快取 */
    @JsonProperty("is_cached")
    private Boolean isCached;

    /** 建立時間 */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
