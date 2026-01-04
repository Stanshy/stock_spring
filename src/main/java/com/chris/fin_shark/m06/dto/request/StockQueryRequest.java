package com.chris.fin_shark.m06.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 股票查詢請求
 * <p>
 * 用於分頁查詢股票列表
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQueryRequest {

    /** 市場類型（TWSE/OTC/EMERGING） */
    @JsonProperty("market_type")
    private String marketType;

    /** 產業別 */
    @JsonProperty("industry")
    private String industry;

    /** 股票名稱（模糊查詢） */
    @JsonProperty("stock_name")
    private String stockName;

    /** 是否僅查詢活躍股票 */
    @JsonProperty("active_only")
    private Boolean activeOnly;

    /** 頁碼（從 1 開始） */
    @JsonProperty("page")
    @Builder.Default
    private Integer page = 1;

    /** 每頁筆數 */
    @JsonProperty("size")
    @Builder.Default
    private Integer size = 20;
}
