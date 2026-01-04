package com.chris.fin_shark.m07.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 指標查詢請求
 * <p>
 * 用於查詢技術指標
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndicatorQueryRequest {

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 指標名稱清單（逗號分隔） */
    @JsonProperty("indicators")
    private String indicators;

    /** 指標類別 */
    @JsonProperty("categories")
    private String categories;

    /** 開始日期 */
    @JsonProperty("start_date")
    private LocalDate startDate;

    /** 結束日期 */
    @JsonProperty("end_date")
    private LocalDate endDate;

    /** 頁碼（從 1 開始） */
    @JsonProperty("page")
    @Builder.Default
    private Integer page = 1;

    /** 每頁筆數 */
    @JsonProperty("size")
    @Builder.Default
    private Integer size = 20;
}
