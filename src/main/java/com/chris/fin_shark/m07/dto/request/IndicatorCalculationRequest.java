package com.chris.fin_shark.m07.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 指標計算請求
 * <p>
 * 用於手動觸發指標計算
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorCalculationRequest {

    /** 計算日期 */
    @JsonProperty("calculation_date")
    @NotNull(message = "calculation_date is required")
    private LocalDate calculationDate;

    /** 股票代碼清單 */
    @JsonProperty("stock_ids")
    private List<String> stockIds;

    /** 指標優先級（P0/P1/P2） */
    @JsonProperty("indicator_priority")
    @Builder.Default
    private String indicatorPriority = "P0";

    /** 是否強制重新計算 */
    @JsonProperty("force_recalculate")
    @Builder.Default
    private Boolean forceRecalculate = false;
}
