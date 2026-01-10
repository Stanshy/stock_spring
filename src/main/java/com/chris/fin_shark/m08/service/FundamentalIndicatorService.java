package com.chris.fin_shark.m08.service;

import com.chris.fin_shark.m08.converter.FundamentalIndicatorConverter;
import com.chris.fin_shark.m08.domain.FundamentalIndicator;
import com.chris.fin_shark.m08.dto.FundamentalIndicatorDTO;
import com.chris.fin_shark.m08.dto.request.BatchQueryRequest;
import com.chris.fin_shark.m08.dto.request.TrendQueryRequest;
import com.chris.fin_shark.m08.engine.FundamentalEngine;
import com.chris.fin_shark.m08.engine.model.CalculationResult;
import com.chris.fin_shark.m08.engine.model.FinancialData;
import com.chris.fin_shark.m08.enums.ReportType;
import com.chris.fin_shark.m08.exception.FundamentalIndicatorNotFoundException;
import com.chris.fin_shark.m08.mapper.FundamentalIndicatorMapper;
import com.chris.fin_shark.m08.repository.FundamentalIndicatorRepository;
import com.chris.fin_shark.m08.vo.IndicatorTrendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 基本面財務指標服務
 * <p>
 * 功能編號: F-M08-001 ~ F-M08-008
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FundamentalIndicatorService {

    private final FundamentalIndicatorRepository repository;
    private final FundamentalIndicatorMapper mapper;
    private final FundamentalEngine engine;
    private final FundamentalIndicatorConverter converter;

    // ========== 依賴 M06 模組 ==========
    // TODO: 注入以下 M06 依賴
    // private final FinancialStatementRepository financialStatementRepository;
    // private final StockPriceRepository stockPriceRepository;
    // private final StockRepository stockRepository;

    /**
     * 查詢單一股票財務指標（P0 核心功能）
     * <p>
     * API: GET /api/stocks/{stockId}/fundamentals
     * </p>
     *
     * @param stockId 股票代碼
     * @param year    年度（可選）
     * @param quarter 季度（可選）
     * @return 財務指標 DTO
     */
    @Cacheable(value = "fund:indicators", key = "#stockId + ':' + #year + ':' + #quarter")
    public FundamentalIndicatorDTO getIndicators(String stockId, Integer year, Integer quarter) {
        log.info("查詢財務指標: stockId={}, year={}, quarter={}", stockId, year, quarter);

        FundamentalIndicator entity;

        if (year != null && quarter != null) {
            // 查詢指定季度
            entity = repository.findByStockIdAndYearAndQuarterAndReportType(
                            stockId, year, quarter, ReportType.Q)
                    .orElseThrow(() -> FundamentalIndicatorNotFoundException.of(stockId, year, quarter));
        } else {
            // 查詢最新季度
            entity = repository.findLatestByStockId(stockId)
                    .orElseThrow(() -> FundamentalIndicatorNotFoundException.ofLatest(stockId));
        }

        FundamentalIndicatorDTO dto = converter.toDTO(entity);

        // TODO: 從 M06 StockRepository 取得股票名稱
        // Stock stock = stockRepository.findById(stockId).orElse(null);
        // if (stock != null) {
        //     dto.setStockName(stock.getStockName());
        // }

        log.debug("財務指標查詢成功: {}", dto);
        return dto;
    }

    /**
     * 批次查詢財務指標（P0 核心功能）
     * <p>
     * API: POST /api/fundamentals/batch
     * </p>
     *
     * @param request 批次查詢請求
     * @return 財務指標列表
     */
    public List<FundamentalIndicatorDTO> batchQuery(BatchQueryRequest request) {
        log.info("批次查詢財務指標: stockIds={}, year={}, quarter={}",
                request.getStockIds(), request.getYear(), request.getQuarter());

        // 使用 MyBatis 批次查詢
        List<FundamentalIndicator> entities = mapper.batchQuery(
                request.getStockIds(),
                request.getYear(),
                request.getQuarter()
        );

        List<FundamentalIndicatorDTO> dtos = converter.toDTOList(entities);

        // TODO: 批次取得股票名稱
        // Map<String, String> stockNames = stockRepository.findByStockIdIn(request.getStockIds())
        //         .stream()
        //         .collect(Collectors.toMap(Stock::getStockId, Stock::getStockName));
        // dtos.forEach(dto -> dto.setStockName(stockNames.get(dto.getStockId())));

        log.info("批次查詢完成，共 {} 筆", dtos.size());
        return dtos;
    }

    /**
     * 查詢指標歷史趨勢（P0 核心功能）
     * <p>
     * API: POST /api/fundamentals/trends
     * </p>
     *
     * @param request 趨勢查詢請求
     * @return 趨勢資料列表
     */
    public List<IndicatorTrendVO> queryTrend(TrendQueryRequest request) {
        log.info("查詢指標趨勢: stockId={}, indicator={}, period={}Q{}-{}Q{}",
                request.getStockId(), request.getIndicator(),
                request.getStartYear(), request.getStartQuarter(),
                request.getEndYear(), request.getEndQuarter());

        List<IndicatorTrendVO> trends = mapper.queryTrend(
                request.getStockId(),
                request.getIndicator(),
                request.getStartYear(),
                request.getStartQuarter(),
                request.getEndYear(),
                request.getEndQuarter()
        );

        log.info("趨勢查詢完成，共 {} 筆資料", trends.size());
        return trends;
    }

    /**
     * 計算並儲存財務指標（P0 核心功能）
     * <p>
     * 由 Job 呼叫，執行指標計算並儲存
     * </p>
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     * @return 計算結果
     */
    @Transactional
    @CacheEvict(value = "fund:indicators", key = "#stockId + ':' + #year + ':' + #quarter")
    public CalculationResult calculateAndSave(String stockId, Integer year, Integer quarter) {
        log.info("開始計算財務指標: stockId={}, year={}, quarter={}", stockId, year, quarter);

        // 1. 準備輸入資料
        FinancialData data = prepareFinancialData(stockId, year, quarter);

        // 2. 驗證資料完整性
        if (!data.validate()) {
            log.error("財務資料驗證失敗: stockId={}, year={}, quarter={}", stockId, year, quarter);
            throw new IllegalArgumentException("財務資料不完整，無法計算指標");
        }

        // 3. 委派引擎計算
        CalculationResult result = engine.calculate(data);

        // 4. 檢查計算錯誤
        if (result.hasErrors()) {
            log.error("指標計算失敗: {}", result.getDiagnostics().getErrors());
            throw new RuntimeException("指標計算失敗: " + result.getDiagnostics().getErrors());
        }

        // 5. 轉換並儲存
        FundamentalIndicator entity = converter.toEntity(result);
        enrichEntity(entity, stockId, year, quarter, data);
        repository.save(entity);

        log.info("財務指標計算完成: stockId={}, 指標數量={}, 耗時={}ms",
                stockId, result.getTotalIndicatorCount(),
                result.getDiagnostics().getCalculationTime());

        return result;
    }

    /**
     * 準備財務資料（從 M06 取得）
     */
    private FinancialData prepareFinancialData(String stockId, Integer year, Integer quarter) {
        // ========== 依賴 M06 模組 ==========
        // TODO: 從 M06 查詢財報資料
        // FinancialStatement currentStatement = financialStatementRepository
        //     .findByStockIdAndYearAndQuarter(stockId, year, quarter)
        //     .orElseThrow(() -> new FinancialStatementNotFoundException(...));

        // TODO: 從 M06 查詢去年同季財報（用於成長率計算）
        // FinancialStatement lastYearStatement = financialStatementRepository
        //     .findByStockIdAndYearAndQuarter(stockId, year - 1, quarter)
        //     .orElse(null);

        // TODO: 從 M06 查詢股價
        // StockPrice stockPrice = stockPriceRepository
        //     .findLatestByStockId(stockId)
        //     .orElse(null);

        // 臨時實作（範例資料）
        return FinancialData.builder()
                .stockId(stockId)
                .year(year)
                .quarter(quarter)
                .calculationDate(LocalDate.now())
                // TODO: 填入實際財報資料
                .revenue(BigDecimal.valueOf(226300000)) // 範例值
                .operatingCost(BigDecimal.valueOf(1052000000))
                .operatingIncome(BigDecimal.valueOf(957000000))
                .netIncome(BigDecimal.valueOf(934000000))
                .totalAssets(BigDecimal.valueOf(5000000000L))
                .currentAssets(BigDecimal.valueOf(3000000000L))
                .totalLiabilities(BigDecimal.valueOf(1500000000L))
                .currentLiabilities(BigDecimal.valueOf(1000000000L))
                .totalEquity(BigDecimal.valueOf(3500000000L))
                .operatingCashFlow(BigDecimal.valueOf(1000000000))
                .capitalExpenditure(BigDecimal.valueOf(300000000))
                .eps(BigDecimal.valueOf(36.05))
                .bookValuePerShare(BigDecimal.valueOf(135.00))
                .outstandingShares(25900000000L)
                .stockPrice(BigDecimal.valueOf(580.00))
                .marketCap(BigDecimal.valueOf(15000000000L))
                // 歷史資料（用於成長率）
                .lastYearRevenue(BigDecimal.valueOf(1910000000))
                .lastYearNetIncome(BigDecimal.valueOf(746000000))
                .lastYearEps(BigDecimal.valueOf(28.80))
                .build();
    }

    /**
     * 補充 Entity 資訊
     */
    private void enrichEntity(FundamentalIndicator entity, String stockId,
                              Integer year, Integer quarter, FinancialData data) {
        entity.setStockId(stockId);
        entity.setYear(year);
        entity.setQuarter(quarter);
        entity.setReportType(ReportType.Q);
        entity.setCalculationDate(data.getCalculationDate());
        entity.setStockPrice(data.getStockPrice());

        // 從 JSONB 提取常用指標到冗餘欄位（加速查詢）
        Map<String, BigDecimal> valuation = entity.getValuationIndicators();
        if (valuation != null) {
            entity.setPeRatio(valuation.get("pe_ratio"));
            entity.setPbRatio(valuation.get("pb_ratio"));
        }

        Map<String, BigDecimal> profitability = entity.getProfitabilityIndicators();
        if (profitability != null) {
            entity.setRoe(profitability.get("roe"));
            entity.setEps(profitability.get("eps"));
        }

        Map<String, BigDecimal> structure = entity.getFinancialStructureIndicators();
        if (structure != null) {
            entity.setDebtRatio(structure.get("debt_ratio"));
        }

        Map<String, BigDecimal> solvency = entity.getSolvencyIndicators();
        if (solvency != null) {
            entity.setCurrentRatio(solvency.get("current_ratio"));
        }
    }

    // ========== P1 進階功能（TODO） ==========

    /**
     * TODO: P1 - 查詢指標排名
     *
     * @param indicator 指標名稱
     * @param year      年度
     * @param quarter   季度
     * @param ascending 是否升序
     * @param limit     數量限制
     * @return 排名列表
     */
    // public List<IndicatorRankingVO> queryRanking(...) {
    //     throw new UnsupportedOperationException("P1 功能尚未實作");
    // }

    /**
     * TODO: P1 - 查詢指標統計資訊（全市場）
     *
     * @param indicator 指標名稱
     * @param year      年度
     * @param quarter   季度
     * @return 統計資訊（min, max, avg, median, p25, p75）
     */
    // public IndicatorStatisticsVO queryStatistics(...) {
    //     throw new UnsupportedOperationException("P1 功能尚未實作");
    // }
}
