package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.m06.converter.StockPriceConverter;
import com.chris.fin_shark.m06.domain.StockPrice;
import com.chris.fin_shark.m06.dto.InstitutionalTradingDTO;
import com.chris.fin_shark.m06.dto.MarginTradingDTO;
import com.chris.fin_shark.m06.dto.StockPriceDTO;
import com.chris.fin_shark.m06.exception.StockNotFoundException;
import com.chris.fin_shark.m06.mapper.StockPriceMapper;
import com.chris.fin_shark.m06.repository.StockPriceRepository;
import com.chris.fin_shark.m06.repository.StockRepository;
import com.chris.fin_shark.m06.vo.StockPriceStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 市場資料查詢服務
 * <p>
 * 功能編號: F-M06-007
 * 功能名稱: 資料查詢 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MarketDataQueryService {

    private final StockRepository stockRepository;
    private final StockPriceRepository stockPriceRepository;
    private final StockPriceMapper stockPriceMapper;
    private final StockPriceConverter stockPriceConverter;

    /**
     * 查詢股票歷史股價
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @param days      查詢天數
     * @return 股價列表
     */
    @Transactional(readOnly = true)
    public List<StockPriceDTO> queryStockPrices(String stockId, LocalDate startDate,
            LocalDate endDate, Integer days) {
        log.debug("查詢股價: stockId={}, startDate={}, endDate={}, days={}",
                stockId, startDate, endDate, days);

        // 驗證股票是否存在
        if (!stockRepository.existsByStockId(stockId)) {
            throw StockNotFoundException.of(stockId);
        }

        List<StockPrice> prices;

        if (startDate != null && endDate != null) {
            // 使用日期範圍查詢
            prices = stockPriceRepository.findByStockIdAndDateRange(stockId, startDate, endDate);
        } else {
            // 使用天數查詢（查詢最近 N 天）
            int queryDays = (days == null || days <= 0) ? 30 : days; // 給一個預設值，例：30 天

            // 找這檔股票最新一筆資料
            var latestOpt = stockPriceRepository.findTopByStockIdOrderByTradeDateDesc(stockId);
            if (latestOpt.isEmpty()) {
                // 這支股票完全沒有股價資料
                return List.of();
            }

            LocalDate end = latestOpt.get().getTradeDate();
            LocalDate start = end.minusDays(queryDays - 1);

            log.debug("使用最近 N 天查詢: stockId={}, start={}, end={}, days={}",
                    stockId, start, end, queryDays);

            prices = stockPriceRepository.findByStockIdAndDateRange(stockId, start, end);
        }

        return stockPriceConverter.toDTOList(prices);
    }

    /**
     * 查詢最新股價
     *
     * @param stockId 股票代碼
     * @return 最新股價
     */
    @Transactional(readOnly = true)
    public StockPriceDTO getLatestPrice(String stockId) {
        log.debug("查詢最新股價: stockId={}", stockId);

        StockPrice price = stockPriceRepository.findTopByStockIdOrderByTradeDateDesc(stockId)
                .orElseThrow(() -> new RuntimeException("No price data found for " + stockId));

        return stockPriceConverter.toDTO(price);
    }

    /**
     * 查詢股價統計資訊
     *
     * @param stockId 股票代碼
     * @param days    查詢天數
     * @return 股價統計資訊
     */
    @Transactional(readOnly = true)
    public List<StockPriceStatisticsVO> getStockPriceStatistics(String stockId, Integer days) {
        log.debug("查詢股價統計: stockId={}, days={}", stockId, days);

        return stockPriceMapper.getStatistics(stockId, days);
    }

    /**
     * 查詢法人買賣超
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @param days      查詢天數
     * @return 法人買賣超列表
     */
    @Transactional(readOnly = true)
    public List<InstitutionalTradingDTO> queryInstitutionalTrading(String stockId,
            LocalDate startDate,
            LocalDate endDate,
            Integer days) {
        log.debug("查詢法人買賣: stockId={}", stockId);

        // TODO: 實作法人買賣查詢邏輯
        return List.of();
    }

    /**
     * 查詢融資融券
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @param days      查詢天數
     * @return 融資融券列表
     */
    @Transactional(readOnly = true)
    public List<MarginTradingDTO> queryMarginTrading(String stockId,
            LocalDate startDate,
            LocalDate endDate,
            Integer days) {
        log.debug("查詢融資融券: stockId={}", stockId);

        // TODO: 實作融資融券查詢邏輯
        return List.of();
    }
}
