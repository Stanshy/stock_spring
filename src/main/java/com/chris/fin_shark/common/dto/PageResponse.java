package com.chris.fin_shark.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分頁回應包裝類
 *
 * 用於包裝分頁查詢結果，作為 ApiResponse 的 data 內容
 *
 * JSON 輸出範例:
 * {
 *   "code": 200,
 *   "message": "Success",
 *   "data": {
 *     "items": [ ... ],
 *     "pagination": {
 *       "page": 1,
 *       "page_size": 20,
 *       "total_items": 156,
 *       "total_pages": 8,
 *       "has_next": true,
 *       "has_prev": false
 *     }
 *   }
 * }
 *
 * @param <T> 分頁資料的泛型類型
 * @author chris
 * @since 2025-12-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    /**
     * 資料項目列表
     * JSON 輸出為 "items"
     */
    private List<T> items;

    /**
     * 分頁資訊
     * JSON 輸出為巢狀的 "pagination" 物件
     */
    private PaginationInfo pagination;

    /**
     * 建立分頁回應
     *
     * @param items 資料項目列表
     * @param page 當前頁碼（從 1 開始）
     * @param pageSize 每頁筆數
     * @param totalItems 總筆數
     * @return PageResponse
     */
    public static <T> PageResponse<T> of(List<T> items, int page, int pageSize, long totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        PaginationInfo pagination = PaginationInfo.builder()
                .page(page)
                .pageSize(pageSize)
                .totalItems(totalItems)
                .totalPages(totalPages)
                .hasNext(page < totalPages)
                .hasPrev(page > 1)
                .build();

        return PageResponse.<T>builder()
                .items(items)
                .pagination(pagination)
                .build();
    }

    /**
     * 從 Spring Data Page 物件轉換
     *
     * 注意: Spring Data Page 的頁碼從 0 開始，需要 +1 轉換為從 1 開始
     *
     * @param page Spring Data Page 物件
     * @return PageResponse
     */
    public static <T> PageResponse<T> fromPage(org.springframework.data.domain.Page<T> page) {
        int currentPage = page.getNumber() + 1;  // 從 0 轉換為從 1 開始

        PaginationInfo pagination = PaginationInfo.builder()
                .page(currentPage)
                .pageSize(page.getSize())
                .totalItems(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrev(page.hasPrevious())
                .build();

        return PageResponse.<T>builder()
                .items(page.getContent())
                .pagination(pagination)
                .build();
    }
}
