package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m06.converter.StockConverter;
import com.chris.fin_shark.m06.domain.Stock;
import com.chris.fin_shark.m06.dto.StockDTO;
import com.chris.fin_shark.m06.dto.request.StockCreateRequest;
import com.chris.fin_shark.m06.dto.request.StockQueryRequest;
import com.chris.fin_shark.m06.dto.request.StockUpdateRequest;
import com.chris.fin_shark.m06.exception.StockNotFoundException;
import com.chris.fin_shark.m06.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 股票資料服務層
 * <p>
 * 提供股票基本資料的業務邏輯處理
 * 包含 CRUD 操作和查詢功能
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class StockService {

    private final StockRepository stockRepository;
    private final StockConverter stockConverter;

    /**
     * 根據股票代碼查詢股票
     *
     * @param stockId 股票代碼
     * @return 股票 DTO
     * @throws StockNotFoundException 當股票不存在時
     */
    @Transactional(readOnly = true)
    public StockDTO getStockById(String stockId) {
        log.debug("查詢股票: {}", stockId);

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> StockNotFoundException.of(stockId));

        return stockConverter.toDTO(stock);
    }

    /**
     * 分頁查詢股票列表
     *
     * @param request 查詢條件
     * @return 股票分頁結果
     */
    @Transactional(readOnly = true)
    public PageResponse<StockDTO> queryStocks(StockQueryRequest request) {
        log.debug("查詢股票列表: {}", request);

        // 建立分頁參數（JPA Page 從 0 開始）
        Pageable pageable = PageRequest.of(
                request.getPage() - 1,
                request.getSize(),
                Sort.by("stockId").ascending()
        );

        // 根據條件查詢
        Page<Stock> stockPage;

        if (request.getStockName() != null && !request.getStockName().trim().isEmpty()) {
            // 模糊查詢股票名稱
            stockPage = stockRepository.findByStockNameContaining(
                    request.getStockName(), pageable);
        } else if (request.getMarketType() != null && Boolean.TRUE.equals(request.getActiveOnly())) {
            // 查詢指定市場的活躍股票
            stockPage = stockRepository.findByMarketTypeAndIsActiveTrue(
                    request.getMarketType(), pageable);
        } else {
            // 查詢全部
            stockPage = stockRepository.findAll(pageable);
        }

        // 轉換為 DTO
        List<StockDTO> dtoList = stockConverter.toDTOList(stockPage.getContent());

        // 組裝分頁回應
        return PageResponse.of(
                dtoList,
                request.getPage(),
                request.getSize(),
                stockPage.getTotalElements()  // ✅ 只要 4 個參數
        );
    }

    /**
     * 建立股票
     *
     * @param request 建立請求
     * @return 建立後的股票 DTO
     * @throws com.chris.fin_shark.common.exception.BusinessException 當股票已存在時
     */
    @Transactional
    public StockDTO createStock(StockCreateRequest request) {
        log.info("建立股票: {}", request.getStockId());

        // 檢查股票是否已存在
        if (stockRepository.existsByStockId(request.getStockId())) {
            throw new com.chris.fin_shark.common.exception.BusinessException(
                    com.chris.fin_shark.m06.enums.M06ErrorCode.M06_STOCK_ALREADY_EXISTS,
                    "Stock already exists: " + request.getStockId()
            );
        }

        // 轉換並儲存
        Stock stock = stockConverter.toEntity(request);
        Stock savedStock = stockRepository.save(stock);

        log.info("股票建立成功: {}", savedStock.getStockId());
        return stockConverter.toDTO(savedStock);
    }

    /**
     * 更新股票
     *
     * @param stockId 股票代碼
     * @param request 更新請求
     * @return 更新後的股票 DTO
     * @throws StockNotFoundException 當股票不存在時
     */
    @Transactional
    public StockDTO updateStock(String stockId, StockUpdateRequest request) {
        log.info("更新股票: {}", stockId);

        // 查詢股票
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> StockNotFoundException.of(stockId));

        // 更新欄位（僅更新非 null 的欄位）
        stockConverter.updateEntity(request, stock);

        // 儲存
        Stock updatedStock = stockRepository.save(stock);

        log.info("股票更新成功: {}", updatedStock.getStockId());
        return stockConverter.toDTO(updatedStock);
    }

    /**
     * 刪除股票（軟刪除）
     *
     * @param stockId 股票代碼
     * @throws StockNotFoundException 當股票不存在時
     */
    @Transactional
    public void deleteStock(String stockId) {
        log.info("刪除股票: {}", stockId);

        // 查詢股票
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> StockNotFoundException.of(stockId));

        // 軟刪除（設定為不活躍）
        stock.setIsActive(false);
        stockRepository.save(stock);

        log.info("股票刪除成功: {}", stockId);
    }

    /**
     * 查詢所有活躍股票
     *
     * @return 活躍股票列表
     */
    @Transactional(readOnly = true)
    public List<StockDTO> getAllActiveStocks() {
        log.debug("查詢所有活躍股票");

        List<Stock> stocks = stockRepository.findByIsActiveTrue();
        return stockConverter.toDTOList(stocks);
    }

    /**
     * 根據市場類型查詢股票
     *
     * @param marketType 市場類型（TWSE/OTC/EMERGING）
     * @return 股票列表
     */
    @Transactional(readOnly = true)
    public List<StockDTO> getStocksByMarketType(String marketType) {
        log.debug("查詢市場類型: {}", marketType);

        List<Stock> stocks = stockRepository.findByMarketType(marketType);
        return stockConverter.toDTOList(stocks);
    }
}
