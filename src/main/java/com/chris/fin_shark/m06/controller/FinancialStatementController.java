package com.chris.fin_shark.m06.controller;

import com.chris.fin_shark.common.dto.ApiResponse;
import com.chris.fin_shark.m06.converter.FinancialStatementConverter;
import com.chris.fin_shark.m06.domain.FinancialStatement;
import com.chris.fin_shark.m06.dto.FinancialStatementDTO;
import com.chris.fin_shark.m06.repository.FinancialStatementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 財務報表 Controller
 * <p>
 * 功能編號: F-M06-003, F-M06-007
 * 功能名稱: 財報資料同步、資料查詢 API
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/financials")
@Slf4j
@RequiredArgsConstructor
public class FinancialStatementController {

    private final FinancialStatementRepository financialStatementRepository;
    private final FinancialStatementConverter financialStatementConverter;

    /**
     * 查詢指定股票的財報歷史
     *
     * @param stockId  股票代碼
     * @param quarters 查詢季度數（預設 8）
     * @return 財報列表
     */
    @GetMapping("/{stockId}")
    public ApiResponse<List<FinancialStatementDTO>> getFinancialStatements(
            @PathVariable String stockId,
            @RequestParam(defaultValue = "8") Integer quarters) {

        log.info("GET /api/financials/{}?quarters={}", stockId, quarters);

        List<FinancialStatement> data = financialStatementRepository
                .findByStockIdOrderByYearDescQuarterDesc(stockId, PageRequest.of(0, quarters))
                .getContent();

        return ApiResponse.success(financialStatementConverter.toDTOList(data));
    }

    /**
     * 查詢最新財報
     *
     * @param stockId 股票代碼
     * @return 最新財報
     */
    @GetMapping("/{stockId}/latest")
    public ApiResponse<FinancialStatementDTO> getLatestFinancialStatement(
            @PathVariable String stockId) {

        log.info("GET /api/financials/{}/latest", stockId);

        Optional<FinancialStatement> latest = financialStatementRepository.findLatestByStockId(stockId);

        return latest.map(data -> ApiResponse.success(financialStatementConverter.toDTO(data)))
                .orElse(ApiResponse.success(null));
    }

    /**
     * 查詢指定年度季度的財報
     *
     * @param stockId 股票代碼
     * @param year    年度
     * @param quarter 季度
     * @return 財報
     */
    @GetMapping("/{stockId}/{year}/{quarter}")
    public ApiResponse<FinancialStatementDTO> getFinancialStatementByPeriod(
            @PathVariable String stockId,
            @PathVariable Integer year,
            @PathVariable Short quarter) {

        log.info("GET /api/financials/{}/{}/{}", stockId, year, quarter);

        Optional<FinancialStatement> statement = financialStatementRepository
                .findByStockIdAndYearAndQuarter(stockId, year, quarter);

        return statement.map(data -> ApiResponse.success(financialStatementConverter.toDTO(data)))
                .orElse(ApiResponse.success(null));
    }

    /**
     * 查詢指定年度季度的全市場財報
     *
     * @param year    年度
     * @param quarter 季度
     * @return 全市場財報列表
     */
    @GetMapping("/market/{year}/{quarter}")
    public ApiResponse<List<FinancialStatementDTO>> getMarketFinancialStatements(
            @PathVariable Integer year,
            @PathVariable Short quarter) {

        log.info("GET /api/financials/market/{}/{}", year, quarter);

        List<FinancialStatement> data = financialStatementRepository.findByYearAndQuarter(year, quarter);
        return ApiResponse.success(financialStatementConverter.toDTOList(data));
    }
}
