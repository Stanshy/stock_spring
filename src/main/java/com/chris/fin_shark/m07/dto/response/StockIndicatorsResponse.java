package com.chris.fin_shark.m07.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 股票技術指標回應
 * <p>
 * API-M07-001 專用回應格式
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockIndicatorsResponse {

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 股票名稱 */
    @JsonProperty("stock_name")
    private String stockName;

    /** 技術指標列表 */
    @JsonProperty("indicators")
    private List<IndicatorDataPoint> indicators;

    /** 總筆數 */
    @JsonProperty("total_count")
    private Integer totalCount;
}

