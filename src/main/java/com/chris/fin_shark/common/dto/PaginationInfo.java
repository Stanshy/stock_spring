package com.chris.fin_shark.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分頁資訊物件
 *
 * 作為 PageResponse 的巢狀物件，包含分頁相關資訊
 *
 * JSON 輸出範例:
 * {
 *   "page": 1,
 *   "page_size": 20,
 *   "total_items": 156,
 *   "total_pages": 8,
 *   "has_next": true,
 *   "has_prev": false
 * }
 *
 * @author chris
 * @since 2025-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationInfo {

    /**
     * 當前頁碼（從 1 開始）
     *
     * 注意: 總綱 4.4.5 規範頁碼從 1 開始，與 Spring Data 從 0 開始不同
     */
    private Integer page;

    /**
     * 每頁筆數
     *
     * 總綱 4.4.5: 預設 20，最大 100
     *
     * JSON 輸出為 snake_case: "page_size"
     */
    @JsonProperty("page_size")
    private Integer pageSize;

    /**
     * 總筆數
     *
     * JSON 輸出為 snake_case: "total_items"
     */
    @JsonProperty("total_items")
    private Long totalItems;

    /**
     * 總頁數
     *
     * JSON 輸出為 snake_case: "total_pages"
     */
    @JsonProperty("total_pages")
    private Integer totalPages;

    /**
     * 是否有下一頁
     *
     * JSON 輸出為 snake_case: "has_next"
     */
    @JsonProperty("has_next")
    private Boolean hasNext;

    /**
     * 是否有上一頁
     *
     * JSON 輸出為 snake_case: "has_prev"
     */
    @JsonProperty("has_prev")
    private Boolean hasPrev;
}

