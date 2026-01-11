package com.chris.fin_shark.m06.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 品質檢核執行請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityCheckExecuteRequest {

    /**
     * 檢核類型列表
     * <p>
     * 可選值：COMPLETENESS, CONSISTENCY, TIMELINESS, ACCURACY, UNIQUENESS
     * 若為空則執行所有類型
     * </p>
     */
    @JsonProperty("check_types")
    private List<String> checkTypes;

    /**
     * 目標資料表列表
     * <p>
     * 可選值：stock_prices, institutional_trading, margin_trading, financial_statements
     * 若為空則檢核所有資料表
     * </p>
     */
    @JsonProperty("target_tables")
    private List<String> targetTables;

    /**
     * 開始日期（用於日期範圍檢核）
     */
    @JsonProperty("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 結束日期（用於日期範圍檢核）
     */
    @JsonProperty("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 是否只檢核活躍股票
     */
    @JsonProperty("active_stocks_only")
    private Boolean activeStocksOnly;

    /**
     * 是否記錄問題到 data_quality_issues 表
     */
    @JsonProperty("record_issues")
    private Boolean recordIssues;
}
