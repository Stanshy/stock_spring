package com.chris.fin_shark.m10.engine;

import com.chris.fin_shark.m10.engine.model.PatternMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 型態偵測器註冊表
 * <p>
 * 管理所有已註冊的型態偵測器，提供按類別、優先級、型態 ID 的查詢功能
 * </p>
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Component
public class PatternDetectorRegistry {

    /**
     * 偵測器名稱 → 偵測器實例
     */
    private final Map<String, PatternDetector> detectors = new ConcurrentHashMap<>();

    /**
     * 型態 ID → 偵測器名稱（反向索引）
     */
    private final Map<String, String> patternIdToDetector = new ConcurrentHashMap<>();

    /**
     * 型態 ID → 元資料
     */
    private final Map<String, PatternMetadata> metadata = new ConcurrentHashMap<>();

    /**
     * 建構子：自動註冊所有偵測器
     *
     * @param detectorList Spring 自動注入的所有 PatternDetector 實例
     */
    public PatternDetectorRegistry(List<PatternDetector> detectorList) {
        detectorList.forEach(this::register);

        log.info("========================================");
        log.info("M10 型態偵測器註冊表初始化完成");
        log.info("========================================");
        log.info("已註冊 {} 個偵測器，支援 {} 種型態", detectors.size(), patternIdToDetector.size());

        // 按類別分組顯示
        Map<String, List<PatternDetector>> byCategory = detectors.values().stream()
                .collect(Collectors.groupingBy(PatternDetector::getCategory));

        byCategory.forEach((category, list) -> {
            log.info("  {} ({} 個偵測器):", category, list.size());
            list.forEach(d -> {
                log.info("    - {} [{}] 支援 {} 種型態",
                        d.getName(),
                        d.getPriority(),
                        d.getSupportedPatternIds().size());
            });
        });
    }

    /**
     * 註冊偵測器
     *
     * @param detector 偵測器實例
     */
    public void register(PatternDetector detector) {
        String name = detector.getName();

        // 檢查是否啟用
        if (!detector.isEnabled()) {
            log.debug("跳過未啟用的偵測器: {}", name);
            return;
        }

        // 註冊偵測器
        detectors.put(name, detector);

        // 建立型態 ID 的反向索引
        for (String patternId : detector.getSupportedPatternIds()) {
            patternIdToDetector.put(patternId, name);

            // 快取元資料
            PatternMetadata meta = detector.getMetadata(patternId);
            if (meta != null) {
                metadata.put(patternId, meta);
            }
        }

        log.debug("已註冊偵測器: {} ({}), 支援型態: {}",
                name, detector.getCategory(), detector.getSupportedPatternIds());
    }

    // === 查詢方法 ===

    /**
     * 取得偵測器
     *
     * @param detectorName 偵測器名稱
     * @return 偵測器實例
     */
    public Optional<PatternDetector> getDetector(String detectorName) {
        return Optional.ofNullable(detectors.get(detectorName));
    }

    /**
     * 根據型態 ID 取得偵測器
     *
     * @param patternId 型態 ID（如 KLINE001）
     * @return 偵測器實例
     */
    public Optional<PatternDetector> getDetectorByPatternId(String patternId) {
        String detectorName = patternIdToDetector.get(patternId);
        return detectorName != null ? getDetector(detectorName) : Optional.empty();
    }

    /**
     * 取得型態元資料
     *
     * @param patternId 型態 ID
     * @return 元資料
     */
    public Optional<PatternMetadata> getMetadata(String patternId) {
        return Optional.ofNullable(metadata.get(patternId));
    }

    /**
     * 取得所有已註冊的偵測器名稱
     */
    public Set<String> getAllDetectorNames() {
        return new HashSet<>(detectors.keySet());
    }

    /**
     * 取得所有支援的型態 ID
     */
    public Set<String> getAllPatternIds() {
        return new HashSet<>(patternIdToDetector.keySet());
    }

    /**
     * 根據類別取得偵測器
     *
     * @param category 類別（如 KLINE_SINGLE, CHART_REVERSAL）
     * @return 偵測器列表
     */
    public List<PatternDetector> getDetectorsByCategory(String category) {
        return detectors.values().stream()
                .filter(d -> category.equals(d.getCategory()))
                .collect(Collectors.toList());
    }

    /**
     * 根據優先級取得偵測器
     *
     * @param priority 優先級（P0, P1, P2）
     * @return 偵測器列表
     */
    public List<PatternDetector> getDetectorsByPriority(String priority) {
        return detectors.values().stream()
                .filter(d -> priority.equals(d.getPriority()))
                .collect(Collectors.toList());
    }

    /**
     * 取得 K 線型態偵測器
     */
    public List<PatternDetector> getKLineDetectors() {
        return detectors.values().stream()
                .filter(d -> d.getCategory().startsWith("KLINE"))
                .collect(Collectors.toList());
    }

    /**
     * 取得圖表型態偵測器
     */
    public List<PatternDetector> getChartPatternDetectors() {
        return detectors.values().stream()
                .filter(d -> d.getCategory().startsWith("CHART"))
                .collect(Collectors.toList());
    }

    /**
     * 取得趨勢偵測器
     */
    public List<PatternDetector> getTrendDetectors() {
        return detectors.values().stream()
                .filter(d -> "TREND".equals(d.getCategory()))
                .collect(Collectors.toList());
    }

    /**
     * 取得所有偵測器
     */
    public Map<String, PatternDetector> getAllDetectors() {
        return new HashMap<>(detectors);
    }

    /**
     * 取得偵測器數量
     */
    public int getDetectorCount() {
        return detectors.size();
    }

    /**
     * 取得型態數量
     */
    public int getPatternCount() {
        return patternIdToDetector.size();
    }
}
