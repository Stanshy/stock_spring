package com.chris.fin_shark.m06.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 股票資料傳輸物件
 * <p>
 * 用於 API 請求和回應
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {

    /** 股票代碼 */
    @JsonProperty("stock_id")
    private String stockId;

    /** 股票名稱（中文） */
    @JsonProperty("stock_name")
    private String stockName;

    /** 股票名稱（英文） */
    @JsonProperty("stock_name_en")
    private String stockNameEn;

    /** 市場類型 */
    @JsonProperty("market_type")
    private String marketType;

    /** 產業別 */
    @JsonProperty("industry")
    private String industry;

    /** 產業子分類 */
    @JsonProperty("sector")
    private String sector;

    /** 上市日期 */
    @JsonProperty("listing_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate listingDate;

    /** 下市日期 */
    @JsonProperty("delisting_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate delistingDate;

    /** 是否為活躍股票 */
    @JsonProperty("is_active")
    private Boolean isActive;

    /** 面額 */
    @JsonProperty("par_value")
    private BigDecimal parValue;

    /** 已發行股數 */
    @JsonProperty("issued_shares")
    private Long issuedShares;

    /** 市值 */
    @JsonProperty("market_cap")
    private BigDecimal marketCap;

    /** 標籤 */
    @JsonProperty("tags")
    private String[] tags;

    /** 額外資訊 */
    @JsonProperty("extra_info")
    private Map<String, Object> extraInfo;

    /** 建立時間 */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 更新時間 */
    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
