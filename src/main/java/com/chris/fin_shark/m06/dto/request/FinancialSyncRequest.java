package com.chris.fin_shark.m06.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 財報同步請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSyncRequest {

    /**
     * 年度
     */
    @NotNull(message = "年度不可為空")
    @Min(value = 2000, message = "年度不可小於 2000")
    @Max(value = 2100, message = "年度不可大於 2100")
    private Integer year;

    /**
     * 季度
     */
    @NotNull(message = "季度不可為空")
    @Min(value = 1, message = "季度必須在 1-4 之間")
    @Max(value = 4, message = "季度必須在 1-4 之間")
    private Short quarter;

    /**
     * 股票代碼（可選，若為空則同步所有股票）
     */
    @JsonProperty("stock_id")
    private String stockId;

    /**
     * 報表類型（Q=季報, A=年報）
     */
    @JsonProperty("report_type")
    private String reportType;

    /**
     * 是否強制重新同步（即使資料已存在）
     */
    @JsonProperty("force_sync")
    private Boolean forceSync;
}
