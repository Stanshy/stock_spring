package com.chris.fin_shark.m06.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 資料補齊請求 DTO
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataRepairRequest {

    /**
     * 補齊策略
     * <p>
     * 可選值：AUTO_DETECT, DATE_RANGE, SINGLE_STOCK, FULL_MARKET
     * </p>
     */
    @NotNull(message = "補齊策略不可為空")
    private String strategy;

    /**
     * 資料類型
     * <p>
     * 可選值：STOCK_PRICE, INSTITUTIONAL, MARGIN, FINANCIAL
     * </p>
     */
    @NotNull(message = "資料類型不可為空")
    @JsonProperty("data_type")
    private String dataType;

    /**
     * 開始日期（DATE_RANGE 策略必填）
     */
    @JsonProperty("start_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    /**
     * 結束日期（DATE_RANGE 策略必填）
     */
    @JsonProperty("end_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    /**
     * 股票代碼（SINGLE_STOCK 策略必填）
     */
    @JsonProperty("stock_id")
    private String stockId;

    /**
     * 是否模擬執行（只偵測不實際補齊）
     */
    @JsonProperty("dry_run")
    private Boolean dryRun;
}
