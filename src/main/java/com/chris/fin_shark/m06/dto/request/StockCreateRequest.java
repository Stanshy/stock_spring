package com.chris.fin_shark.m06.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * 股票建立請求
 * <p>
 * 用於新增股票資料
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCreateRequest {

    /** 股票代碼（4-6 位數字） */
    @NotBlank(message = "Stock ID is required")
    @Pattern(regexp = "^[0-9]{4,6}$", message = "Stock ID must be 4-6 digits")
    @JsonProperty("stock_id")
    private String stockId;

    /** 股票名稱（中文） */
    @NotBlank(message = "Stock name is required")
    @JsonProperty("stock_name")
    private String stockName;

    /** 股票名稱（英文） */
    @JsonProperty("stock_name_en")
    private String stockNameEn;

    /** 市場類型 */
    @NotBlank(message = "Market type is required")
    @Pattern(regexp = "^(TWSE|OTC|EMERGING)$", message = "Market type must be TWSE, OTC, or EMERGING")
    @JsonProperty("market_type")
    private String marketType;

    /** 產業別 */
    @JsonProperty("industry")
    private String industry;

    /** 產業子分類 */
    @JsonProperty("sector")
    private String sector;

    /** 上市日期 */
    @NotNull(message = "Listing date is required")
    @JsonProperty("listing_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate listingDate;

    /** 面額 */
    @Positive(message = "Par value must be positive")
    @JsonProperty("par_value")
    private BigDecimal parValue;

    /** 已發行股數 */
    @Positive(message = "Issued shares must be positive")
    @JsonProperty("issued_shares")
    private Long issuedShares;

    /** 標籤 */
    @JsonProperty("tags")
    private String[] tags;

    /** 額外資訊 */
    @JsonProperty("extra_info")
    private Map<String, Object> extraInfo;
}
