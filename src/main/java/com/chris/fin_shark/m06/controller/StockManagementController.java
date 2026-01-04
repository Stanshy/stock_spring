package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m06.dto.StockDTO;
import com.chris.fin_shark.m06.dto.request.StockCreateRequest;
import com.chris.fin_shark.m06.dto.request.StockQueryRequest;
import com.chris.fin_shark.m06.dto.request.StockUpdateRequest;
import com.chris.fin_shark.m06.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 股票管理 Controller
 * <p>
 * 功能編號: F-M06-001
 * 功能名稱: 股票清單管理
 * 提供股票的 CRUD 操作
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/stocks")
@Slf4j
@RequiredArgsConstructor
public class StockManagementController {

    private final StockService stockService;

    /**
     * 查詢單一股票資訊
     *
     * @param stockId 股票代碼
     * @return 股票資訊
     */
    @GetMapping("/{stockId}")
    public ApiResponse<StockDTO> getStock(@PathVariable String stockId) {
        log.info("GET /api/stocks/{}", stockId);
        StockDTO stock = stockService.getStockById(stockId);
        return ApiResponse.success(stock);
    }

    /**
     * 分頁查詢股票清單
     *
     * @param marketType 市場類型（可選，TWSE/TPEX/EMERGING）
     * @param industry   產業別（可選）
     * @param stockName  股票名稱（可選，模糊查詢）
     * @param activeOnly 僅查詢活躍股票（可選）
     * @param page       頁碼（預設 1）
     * @param size       每頁筆數（預設 20）
     * @return 股票分頁結果
     */
    @GetMapping
    public ApiResponse<PageResponse<StockDTO>> queryStocks(
            @RequestParam(required = false) String marketType,
            @RequestParam(required = false) String industry,
            @RequestParam(required = false) String stockName,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {

        log.info("GET /api/stocks?marketType={}&page={}&size={}", marketType, page, size);

        StockQueryRequest request = StockQueryRequest.builder()
                .marketType(marketType)
                .industry(industry)
                .stockName(stockName)
                .activeOnly(activeOnly)
                .page(page)
                .size(size)
                .build();
        PageResponse<StockDTO> pageResponse = stockService.queryStocks(request);
        return ApiResponse.success(pageResponse);
    }

    /**
     * 新增股票
     *
     * @param request 股票建立請求
     * @return 新增的股票資訊
     */
    @PostMapping
    public ApiResponse<StockDTO> createStock(@Valid @RequestBody StockCreateRequest request) {
        log.info("POST /api/stocks: {}", request.getStockId());
        StockDTO stock = stockService.createStock(request);
        return ApiResponse.success(stock);
    }

    /**
     * 更新股票資訊
     *
     * @param stockId 股票代碼
     * @param request 股票更新請求
     * @return 更新後的股票資訊
     */
    @PutMapping("/{stockId}")
    public ApiResponse<StockDTO> updateStock(
            @PathVariable String stockId,
            @Valid @RequestBody StockUpdateRequest request) {

        log.info("PUT /api/stocks/{}", stockId);
        StockDTO stock = stockService.updateStock(stockId, request);
        return ApiResponse.success(stock);
    }

    /**
     * 刪除股票（軟刪除）
     *
     * @param stockId 股票代碼
     * @return 空回應
     */
    @DeleteMapping("/{stockId}")
    public ApiResponse<Void> deleteStock(@PathVariable String stockId) {
        log.info("DELETE /api/stocks/{}", stockId);
        stockService.deleteStock(stockId);
        return ApiResponse.success(null);
    }

    /**
     * 查詢所有活躍股票
     *
     * @return 活躍股票列表
     */
    @GetMapping("/active")
    public ApiResponse<List<StockDTO>> getAllActiveStocks() {
        log.info("GET /api/stocks/active");
        List<StockDTO> stocks = stockService.getAllActiveStocks();
        return ApiResponse.success(stocks);
    }
}