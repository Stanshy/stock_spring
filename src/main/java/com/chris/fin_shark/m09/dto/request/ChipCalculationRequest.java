package com.chris.fin_shark.m09.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 籌碼計算請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChipCalculationRequest {

    /**
     * 計算日期（預設為今天）
     */
    @JsonProperty("calculation_date")
    private LocalDate calculationDate;

    /**
     * 指定股票代碼列表（可選，若為空則計算全市場）
     */
    @JsonProperty("stock_ids")
    private List<String> stockIds;

    /**
     * 優先級（P0, P1, P2）
     */
    @JsonProperty("priority")
    @Builder.Default
    private String priority = "P0";

    /**
     * 是否包含三大法人指標
     */
    @JsonProperty("include_institutional")
    @Builder.Default
    private Boolean includeInstitutional = true;

    /**
     * 是否包含融資融券指標
     */
    @JsonProperty("include_margin")
    @Builder.Default
    private Boolean includeMargin = true;

    /**
     * 是否偵測異常訊號
     */
    @JsonProperty("include_signals")
    @Builder.Default
    private Boolean includeSignals = true;

    /**
     * 是否強制重新計算
     */
    @JsonProperty("force_recalculate")
    @Builder.Default
    private Boolean forceRecalculate = false;
}
