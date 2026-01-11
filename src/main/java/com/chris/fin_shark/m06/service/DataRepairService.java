package com.chris.fin_shark.m06.service;

import com.chris.fin_shark.common.enums.TriggerType;
import com.chris.fin_shark.m06.dto.DataRepairResultDTO;
import com.chris.fin_shark.m06.dto.request.DataRepairRequest;
import com.chris.fin_shark.m06.enums.RepairStrategy;
import com.chris.fin_shark.m06.exception.DataRepairException;
import com.chris.fin_shark.m06.mapper.DataQualityMapper;
import com.chris.fin_shark.m06.mapper.InstitutionalTradingMapper;
import com.chris.fin_shark.m06.mapper.MarginTradingMapper;
import com.chris.fin_shark.m06.mapper.StockPriceMapper;
import com.chris.fin_shark.m06.vo.MissingDataVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 資料補齊服務
 * <p>
 * 功能編號: F-M06-009
 * 功能名稱: 資料補齊機制
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class DataRepairService {

    private final StockPriceMapper stockPriceMapper;
    private final InstitutionalTradingMapper institutionalTradingMapper;
    private final MarginTradingMapper marginTradingMapper;
    private final DataQualityMapper dataQualityMapper;
    private final StockPriceSyncService stockPriceSyncService;
    private final InstitutionalTradingSyncService institutionalTradingSyncService;
    private final MarginTradingSyncService marginTradingSyncService;

    /**
     * 偵測缺漏資料
     *
     * @param request 補齊請求
     * @return 補齊結果（只包含偵測資訊）
     */
    public DataRepairResultDTO detectMissingData(DataRepairRequest request) {
        log.info("開始偵測缺漏資料: request={}", request);

        LocalDateTime startTime = LocalDateTime.now();
        LocalDate startDate = request.getStartDate() != null ?
                request.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null ?
                request.getEndDate() : LocalDate.now();

        // 驗證日期範圍
        if (startDate.isAfter(endDate)) {
            throw DataRepairException.invalidDateRange(startDate.toString(), endDate.toString());
        }

        List<MissingDataVO> missingData;
        String dataType = request.getDataType();

        // 依資料類型偵測缺漏
        switch (dataType.toUpperCase()) {
            case "STOCK_PRICE":
                missingData = dataQualityMapper.checkStockPriceCompleteness(startDate, endDate);
                break;
            case "INSTITUTIONAL":
                missingData = dataQualityMapper.checkInstitutionalCompleteness(startDate, endDate);
                break;
            case "MARGIN":
                missingData = dataQualityMapper.checkMarginCompleteness(startDate, endDate);
                break;
            default:
                throw DataRepairException.invalidStrategy("Unknown data type: " + dataType);
        }

        // 若指定單一股票，過濾結果
        if (request.getStockId() != null && !request.getStockId().isEmpty()) {
            String stockId = request.getStockId();
            missingData = missingData.stream()
                    .filter(m -> m.getStockId().equals(stockId))
                    .collect(Collectors.toList());
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        // 收集缺漏日期
        List<LocalDate> missingDates = missingData.stream()
                .map(MissingDataVO::getMissingDate)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        return DataRepairResultDTO.builder()
                .strategy(request.getStrategy())
                .dataType(dataType)
                .startDate(startDate)
                .endDate(endDate)
                .stockId(request.getStockId())
                .totalMissing(missingData.size())
                .successCount(0)
                .failedCount(0)
                .status("DETECTED")
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .repairedDates(List.of())
                .failedDates(missingDates)
                .build();
    }

    /**
     * 執行資料補齊
     *
     * @param request 補齊請求
     * @return 補齊結果
     */
    @Transactional
    public DataRepairResultDTO executeRepair(DataRepairRequest request) {
        log.info("開始執行資料補齊: request={}", request);

        // 若為模擬執行，只偵測不補齊
        if (Boolean.TRUE.equals(request.getDryRun())) {
            return detectMissingData(request);
        }

        LocalDateTime startTime = LocalDateTime.now();
        LocalDate startDate = request.getStartDate() != null ?
                request.getStartDate() : LocalDate.now().minusDays(30);
        LocalDate endDate = request.getEndDate() != null ?
                request.getEndDate() : LocalDate.now();

        // 驗證日期範圍
        if (startDate.isAfter(endDate)) {
            throw DataRepairException.invalidDateRange(startDate.toString(), endDate.toString());
        }

        // 先偵測缺漏
        DataRepairResultDTO detectResult = detectMissingData(request);

        if (detectResult.getTotalMissing() == 0) {
            detectResult.setStatus("NO_MISSING_DATA");
            return detectResult;
        }

        // 執行補齊
        List<LocalDate> repairedDates = new ArrayList<>();
        List<LocalDate> failedDates = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;

        String dataType = request.getDataType();
        List<LocalDate> datesToRepair = detectResult.getFailedDates();

        for (LocalDate date : datesToRepair) {
            try {
                repairDataForDate(dataType, date, request.getStockId());
                repairedDates.add(date);
                successCount++;
                log.debug("補齊成功: dataType={}, date={}", dataType, date);

            } catch (Exception e) {
                failedDates.add(date);
                failedCount++;
                log.error("補齊失敗: dataType={}, date={}", dataType, date, e);
            }

            // 避免 API 限流
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

        // 決定狀態
        String status;
        if (failedCount == 0) {
            status = "SUCCESS";
        } else if (successCount == 0) {
            status = "FAILED";
        } else {
            status = "PARTIAL";
        }

        return DataRepairResultDTO.builder()
                .strategy(request.getStrategy())
                .dataType(dataType)
                .startDate(startDate)
                .endDate(endDate)
                .stockId(request.getStockId())
                .totalMissing(detectResult.getTotalMissing())
                .successCount(successCount)
                .failedCount(failedCount)
                .status(status)
                .startTime(startTime)
                .endTime(endTime)
                .durationMs(durationMs)
                .repairedDates(repairedDates)
                .failedDates(failedDates)
                .build();
    }

    /**
     * 依日期補齊資料
     */
    private void repairDataForDate(String dataType, LocalDate date, String stockId) {
        log.debug("補齊資料: dataType={}, date={}, stockId={}", dataType, date, stockId);

        // TODO: 實作實際的資料補齊邏輯
        // 這裡應該呼叫對應的 SyncService 來重新同步資料

        switch (dataType.toUpperCase()) {
            case "STOCK_PRICE":
                stockPriceSyncService.syncStockPricesForDate(date, TriggerType.MANUAL);
                break;
            case "INSTITUTIONAL":
                institutionalTradingSyncService.syncInstitutionalTradingForDate(date, TriggerType.MANUAL);
                break;
            case "MARGIN":
                marginTradingSyncService.syncMarginTradingForDate(date, TriggerType.MANUAL);
                break;
            default:
                throw DataRepairException.invalidStrategy("Unknown data type: " + dataType);
        }
    }
}
