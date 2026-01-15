package com.chris.fin_shark.m10.service;

import com.chris.fin_shark.m10.engine.PatternDetectionPlan;
import com.chris.fin_shark.m10.engine.PatternDetectionResult;

import java.time.LocalDate;
import java.util.Map;

/**
 * 型態分析服務介面
 * <p>
 * 提供型態偵測與分析功能
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
public interface PatternAnalysisService {

    /**
     * 執行單支股票的型態分析
     *
     * @param stockId 股票代碼
     * @param plan    偵測計劃
     * @return 偵測結果
     */
    PatternDetectionResult analyzePatterns(String stockId, PatternDetectionPlan plan);

    /**
     * 執行單支股票的完整型態分析
     *
     * @param stockId 股票代碼
     * @return 偵測結果
     */
    PatternDetectionResult analyzePatternsFull(String stockId);

    /**
     * 執行單支股票的快速型態分析（只偵測 K 線型態）
     *
     * @param stockId 股票代碼
     * @return 偵測結果
     */
    PatternDetectionResult analyzePatternsQuick(String stockId);

    /**
     * 批次分析多支股票
     *
     * @param stockIds 股票代碼列表
     * @param plan     偵測計劃
     * @return 股票代碼 → 偵測結果
     */
    Map<String, PatternDetectionResult> batchAnalyze(Iterable<String> stockIds, PatternDetectionPlan plan);

    /**
     * 儲存分析結果
     *
     * @param result 偵測結果
     */
    void saveAnalysisResult(PatternDetectionResult result);

    /**
     * 分析並儲存結果
     *
     * @param stockId 股票代碼
     * @param plan    偵測計劃
     * @return 偵測結果
     */
    PatternDetectionResult analyzeAndSave(String stockId, PatternDetectionPlan plan);
}
