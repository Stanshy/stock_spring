package com.chris.fin_shark.m06.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 股票更新請求
 * <p>
 * 用於更新股票資料
 * 所有欄位皆為可選
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdateRequest {

    /** 股票名稱（中文） */
    @JsonProperty("stock_name")
    private String stockName;

    /** 股票名稱（英文） */
    @JsonProperty("stock_name_en")
    private String stockNameEn;

    /** 產業別 */
    @JsonProperty("industry")
    private String industry;

    /** 產業子分類 */
    @JsonProperty("sector")
    private String sector;

    /** 下市日期 */
    @JsonProperty("delisting_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate delistingDate;

    /** 是否為活躍股票 */
    @JsonProperty("is_active")
    private Boolean isActive;

    /** 面額 */
    @Positive(message = "Par value must be positive")
    @JsonProperty("par_value")
    private BigDecimal parValue;

    /** 已發行股數 */
    @Positive(message = "Issued shares must be positive")
    @JsonProperty("issued_shares")
    private Long issuedShares;

    /** 市值 */
    @Positive(message = "Market cap must be positive")
    @JsonProperty("market_cap")
    private BigDecimal marketCap;

    /** 標籤 */
    @JsonProperty("tags")
    private String[] tags;

    /** 額外資訊 */
    @JsonProperty("extra_info")
    private Map<String, Object> extraInfo;
}
