package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 資料品質檢核規則資料傳輸物件
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataQualityCheckDTO {

    /** 檢核 ID */
    @JsonProperty("check_id")
    private Long checkId;

    /** 檢核名稱 */
    @JsonProperty("check_name")
    private String checkName;

    /** 檢核類型 */
    @JsonProperty("check_type")
    private String checkType;

    /** 目標資料表 */
    @JsonProperty("target_table")
    private String targetTable;

    /** 檢核規則 */
    @JsonProperty("check_rule")
    private String checkRule;

    /** 嚴重性 */
    @JsonProperty("severity")
    private String severity;

    /** 是否啟用 */
    @JsonProperty("is_active")
    private Boolean isActive;

    /** 最後檢核時間 */
    @JsonProperty("last_check_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCheckTime;

    /** 最後檢核結果 */
    @JsonProperty("last_result")
    private String lastResult;
}
