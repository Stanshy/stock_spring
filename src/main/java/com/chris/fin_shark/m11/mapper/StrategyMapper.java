package com.chris.fin_shark.m11.mapper;

import com.chris.fin_shark.m11.domain.*;
import com.chris.fin_shark.m11.dto.request.StrategyQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 策略 MyBatis Mapper
 *
 * @author chris
 * @since 1.0.0
 */
@Mapper
public interface StrategyMapper {

    // ==================== 策略定義 ====================

    /**
     * 查詢策略清單
     */
    List<Strategy> selectStrategies(StrategyQueryRequest request);

    /**
     * 計算策略總數
     */
    int countStrategies(StrategyQueryRequest request);

    /**
     * 根據 ID 查詢策略
     */
    Strategy selectById(@Param("strategyId") String strategyId);

    /**
     * 根據 ID 和版本查詢策略
     */
    Strategy selectByIdAndVersion(
            @Param("strategyId") String strategyId,
            @Param("version") Integer version);

    /**
     * 新增策略
     */
    int insert(Strategy strategy);

    /**
     * 更新策略
     */
    int update(Strategy strategy);

    /**
     * 更新策略狀態
     */
    int updateStatus(
            @Param("strategyId") String strategyId,
            @Param("status") String status);

    /**
     * 更新策略統計
     */
    int updateStatistics(
            @Param("strategyId") String strategyId,
            @Param("signalsGenerated") int signalsGenerated);

    /**
     * 刪除策略（邏輯刪除，設為 ARCHIVED）
     */
    int archive(@Param("strategyId") String strategyId);

    /**
     * 查詢所有啟用的策略
     */
    List<Strategy> selectActiveStrategies();

    /**
     * 查詢預設策略
     */
    List<Strategy> selectPresetStrategies();

    // ==================== 策略版本 ====================

    /**
     * 新增策略版本
     */
    int insertVersion(StrategyVersion version);

    /**
     * 查詢策略版本歷史
     */
    List<StrategyVersion> selectVersions(@Param("strategyId") String strategyId);

    // ==================== 策略執行 ====================

    /**
     * 新增執行記錄
     */
    int insertExecution(StrategyExecution execution);

    /**
     * 更新執行記錄
     */
    int updateExecution(StrategyExecution execution);

    /**
     * 查詢執行記錄
     */
    StrategyExecution selectExecutionById(@Param("executionId") String executionId);

    /**
     * 查詢策略執行歷史
     */
    List<StrategyExecution> selectExecutionHistory(
            @Param("strategyId") String strategyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("offset") int offset,
            @Param("size") int size);

    /**
     * 計算執行歷史總數
     */
    int countExecutionHistory(
            @Param("strategyId") String strategyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ==================== 策略信號 ====================

    /**
     * 批次新增信號
     */
    int batchInsertSignals(@Param("list") List<StrategySignal> signals);

    /**
     * 查詢策略信號
     */
    List<StrategySignal> selectSignals(
            @Param("strategyId") String strategyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("signalType") String signalType,
            @Param("stockId") String stockId,
            @Param("minConfidence") java.math.BigDecimal minConfidence,
            @Param("offset") int offset,
            @Param("size") int size);

    /**
     * 計算信號總數
     */
    int countSignals(
            @Param("strategyId") String strategyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("signalType") String signalType,
            @Param("stockId") String stockId,
            @Param("minConfidence") java.math.BigDecimal minConfidence);

    /**
     * 查詢未消費的信號（供 M13 使用）
     */
    List<StrategySignal> selectUnconsumedSignals(
            @Param("tradeDate") LocalDate tradeDate,
            @Param("strategyId") String strategyId,
            @Param("signalType") String signalType,
            @Param("minConfidence") java.math.BigDecimal minConfidence,
            @Param("limit") int limit);

    /**
     * 標記信號已消費
     */
    int markSignalsConsumed(
            @Param("signalIds") List<String> signalIds,
            @Param("tradeDate") LocalDate tradeDate,
            @Param("consumedBy") String consumedBy);

    /**
     * 查詢當日信號統計
     */
    int countTodaySignals(
            @Param("strategyId") String strategyId,
            @Param("tradeDate") LocalDate tradeDate);

    // ==================== 因子元數據 ====================

    /**
     * 查詢所有啟用的因子
     */
    List<FactorMetadata> selectActiveFactors();

    /**
     * 根據類別查詢因子
     */
    List<FactorMetadata> selectFactorsByCategory(@Param("category") String category);

    /**
     * 根據 ID 查詢因子
     */
    FactorMetadata selectFactorById(@Param("factorId") String factorId);

    /**
     * 計算啟用的因子數量
     */
    int countActiveFactors();

    // ==================== 維護作業 ====================

    /**
     * 更新所有策略的統計資訊
     */
    int updateAllStatistics();

    /**
     * 刪除過期的策略信號
     */
    int deleteOldSignals(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * 清除過期執行記錄的診斷資訊
     */
    int clearOldExecutionDiagnostics(@Param("cutoffDate") LocalDate cutoffDate);
}
