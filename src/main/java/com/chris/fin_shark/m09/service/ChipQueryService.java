package com.chris.fin_shark.m09.service;

import com.chris.fin_shark.m09.converter.ChipAnalysisResultConverter;
import com.chris.fin_shark.m09.converter.ChipSignalConverter;
import com.chris.fin_shark.m09.domain.ChipAnalysisResult;
import com.chris.fin_shark.m09.domain.ChipSignalEntity;
import com.chris.fin_shark.m09.dto.ChipAnalysisResultDTO;
import com.chris.fin_shark.m09.dto.ChipRankingDTO;
import com.chris.fin_shark.m09.dto.ChipSignalDTO;
import com.chris.fin_shark.m09.dto.request.ChipQueryRequest;
import com.chris.fin_shark.m09.dto.request.ChipRankingRequest;
import com.chris.fin_shark.m09.dto.response.ChipRankingResponse;
import com.chris.fin_shark.m09.dto.response.ChipSignalsResponse;
import com.chris.fin_shark.m09.dto.response.StockChipResponse;
import com.chris.fin_shark.m09.exception.ChipNotFoundException;
import com.chris.fin_shark.m09.mapper.ChipAnalysisMapper;
import com.chris.fin_shark.m09.repository.ChipAnalysisResultRepository;
import com.chris.fin_shark.m09.repository.ChipSignalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 籌碼查詢服務
 * <p>
 * 提供籌碼分析結果、排行榜、異常訊號的查詢功能。
 * 對應 API: API-M09-001 ~ API-M09-005
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ChipQueryService {

    private final ChipAnalysisResultRepository analysisResultRepository;
    private final ChipSignalRepository signalRepository;
    private final ChipAnalysisMapper chipAnalysisMapper;
    private final ChipAnalysisResultConverter resultConverter;
    private final ChipSignalConverter signalConverter;

    /**
     * API-M09-001: 查詢個股籌碼分析
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 個股籌碼分析回應
     */
    @Transactional(readOnly = true)
    public StockChipResponse getStockChipAnalysis(
            String stockId,
            LocalDate startDate,
            LocalDate endDate) {

        log.debug("查詢個股籌碼: stockId={}, startDate={}, endDate={}",
                stockId, startDate, endDate);

        // 1. 查詢最新籌碼分析
        ChipAnalysisResult latest = analysisResultRepository
                .findLatestByStockId(stockId)
                .orElseThrow(() -> ChipNotFoundException.of(stockId, null));

        // 2. 查詢歷史資料
        List<ChipAnalysisResult> history = analysisResultRepository
                .findByStockIdAndTradeDateBetween(stockId, startDate, endDate);

        // 3. 查詢相關訊號
        List<ChipSignalEntity> signals = signalRepository
                .findByStockIdAndTradeDateBetween(stockId, startDate, endDate);

        // 4. 轉換並組裝回應
        return StockChipResponse.builder()
                .stockId(stockId)
                .stockName(getStockName(stockId))
                .latest(resultConverter.toDTO(latest))
                .history(resultConverter.toDTOList(history))
                .signals(signalConverter.toDTOList(signals))
                .signalCount(signals.size())
                .build();
    }

    /**
     * API-M09-002: 查詢籌碼排行榜
     *
     * @param request 排行榜請求
     * @return 籌碼排行榜回應
     */
    @Transactional(readOnly = true)
    public ChipRankingResponse getChipRanking(ChipRankingRequest request) {
        log.debug("查詢籌碼排行榜: rankType={}, date={}",
                request.getRankType(), request.getTradeDate());

        LocalDate tradeDate = request.getTradeDate() != null
                ? request.getTradeDate()
                : getLatestTradeDate();

        String rankType = request.getRankType();
        int limit = request.getLimit() != null ? request.getLimit() : 50;
        String marketType = request.getMarketType();

        List<ChipRankingDTO> rankings;

        // 根據排行榜類型查詢
        switch (rankType) {
            case "RANK001":  // 外資買超排行
                rankings = chipAnalysisMapper.selectForeignBuyRanking(tradeDate, marketType, limit);
                break;
            case "RANK002":  // 外資賣超排行
                rankings = chipAnalysisMapper.selectForeignSellRanking(tradeDate, marketType, limit);
                break;
            case "RANK003":  // 投信買超排行
                rankings = chipAnalysisMapper.selectTrustBuyRanking(tradeDate, marketType, limit);
                break;
            case "RANK004":  // 投信賣超排行
                rankings = chipAnalysisMapper.selectTrustSellRanking(tradeDate, marketType, limit);
                break;
            case "RANK005":  // 外資連續買超排行
                rankings = chipAnalysisMapper.selectForeignContinuousBuyRanking(tradeDate, marketType, limit);
                break;
            case "RANK006":  // 融資增加排行
                rankings = chipAnalysisMapper.selectMarginIncreaseRanking(tradeDate, marketType, limit);
                break;
            case "RANK007":  // 融資減少排行
                rankings = chipAnalysisMapper.selectMarginDecreaseRanking(tradeDate, marketType, limit);
                break;
            case "RANK008":  // 券資比排行
                rankings = chipAnalysisMapper.selectShortRatioRanking(tradeDate, marketType, limit);
                break;
            case "RANK009":  // 三大法人合計買超
                rankings = chipAnalysisMapper.selectTotalNetBuyRanking(tradeDate, marketType, limit);
                break;
            default:
                throw new IllegalArgumentException("Unknown rank_type: " + rankType);
        }

        // 補上排名
        for (int i = 0; i < rankings.size(); i++) {
            rankings.get(i).setRank(i + 1);
        }

        return ChipRankingResponse.builder()
                .rankType(rankType)
                .rankName(getRankName(rankType))
                .tradeDate(tradeDate)
                .marketType(request.getMarketType())
                .totalCount(rankings.size())
                .rankings(rankings)
                .build();
    }

    /**
     * API-M09-003: 查詢籌碼異常訊號
     *
     * @param tradeDate  交易日期
     * @param severity   嚴重度過濾
     * @param signalType 訊號類型過濾
     * @return 籌碼訊號回應
     */
    @Transactional(readOnly = true)
    public ChipSignalsResponse getChipSignals(
            LocalDate tradeDate,
            String severity,
            String signalType) {

        log.debug("查詢籌碼訊號: date={}, severity={}, type={}",
                tradeDate, severity, signalType);

        LocalDate queryDate = tradeDate != null ? tradeDate : getLatestTradeDate();

        List<ChipSignalEntity> signals;

        // 根據條件查詢
        if (severity != null && !severity.isEmpty()) {
            signals = signalRepository.findByTradeDateAndSeverity(queryDate, severity);
        } else if (signalType != null && !signalType.isEmpty()) {
            signals = signalRepository.findByTradeDateAndSignalType(queryDate, signalType);
        } else {
            signals = signalRepository.findByTradeDate(queryDate);
        }

        // 統計各嚴重度數量
        Map<String, Integer> severityCounts = signals.stream()
                .collect(Collectors.groupingBy(
                        ChipSignalEntity::getSeverity,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        return ChipSignalsResponse.builder()
                .tradeDate(queryDate)
                .totalCount(signals.size())
                .severityCounts(severityCounts)
                .signals(signalConverter.toDTOList(signals))
                .build();
    }

    /**
     * 查詢個股最新籌碼（內部使用）
     *
     * @param stockId 股票代碼
     * @return 籌碼分析結果 DTO
     */
    @Transactional(readOnly = true)
    public ChipAnalysisResultDTO getLatestByStock(String stockId) {
        ChipAnalysisResult result = analysisResultRepository
                .findLatestByStockId(stockId)
                .orElseThrow(() -> ChipNotFoundException.ofStock(stockId));

        return resultConverter.toDTO(result);
    }

    /**
     * 批次查詢多檔股票最新籌碼
     *
     * @param stockIds 股票代碼列表
     * @return 籌碼分析結果列表
     */
    @Transactional(readOnly = true)
    public List<ChipAnalysisResultDTO> getLatestByStocks(List<String> stockIds) {
        List<ChipAnalysisResult> results = analysisResultRepository
                .findLatestByStockIds(stockIds);
        return resultConverter.toDTOList(results);
    }

    /**
     * 查詢個股籌碼歷史
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 籌碼分析結果列表
     */
    @Transactional(readOnly = true)
    public List<ChipAnalysisResultDTO> getStockHistory(
            String stockId, LocalDate startDate, LocalDate endDate) {

        List<ChipAnalysisResult> results = analysisResultRepository
                .findByStockIdAndTradeDateBetween(stockId, startDate, endDate);

        if (results.isEmpty()) {
            throw ChipNotFoundException.of(stockId, startDate);
        }

        return resultConverter.toDTOList(results);
    }

    /**
     * 查詢股票相關訊號
     *
     * @param stockId   股票代碼
     * @param startDate 開始日期
     * @param endDate   結束日期
     * @return 訊號 DTO 列表
     */
    @Transactional(readOnly = true)
    public List<ChipSignalDTO> getStockSignals(
            String stockId, LocalDate startDate, LocalDate endDate) {

        List<ChipSignalEntity> signals = signalRepository
                .findByStockIdAndTradeDateBetween(stockId, startDate, endDate);

        return signalConverter.toDTOList(signals);
    }

    // ========== 私有方法 ==========

    /**
     * 取得最新交易日期
     */
    private LocalDate getLatestTradeDate() {
        return analysisResultRepository.findLatestTradeDate()
                .orElse(LocalDate.now());
    }

    /**
     * 取得股票名稱
     * TODO: 整合 M06 StockService
     */
    private String getStockName(String stockId) {
        return "";
    }

    /**
     * 取得排行榜名稱
     */
    private String getRankName(String rankType) {
        return switch (rankType) {
            case "RANK001" -> "外資買超排行";
            case "RANK002" -> "外資賣超排行";
            case "RANK003" -> "投信買超排行";
            case "RANK004" -> "投信賣超排行";
            case "RANK005" -> "外資連續買超排行";
            case "RANK006" -> "融資增加排行";
            case "RANK007" -> "融資減少排行";
            case "RANK008" -> "券資比排行";
            case "RANK009" -> "三大法人合計買超";
            default -> rankType;
        };
    }
}
