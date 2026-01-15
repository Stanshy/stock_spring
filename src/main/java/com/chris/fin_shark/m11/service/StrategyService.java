package com.chris.fin_shark.m11.service;

import com.chris.fin_shark.common.dto.PageResponse;
import com.chris.fin_shark.m11.converter.StrategyConverter;
import com.chris.fin_shark.m11.domain.Strategy;
import com.chris.fin_shark.m11.domain.StrategyVersion;
import com.chris.fin_shark.m11.dto.StrategyDTO;
import com.chris.fin_shark.m11.dto.request.StrategyCreateRequest;
import com.chris.fin_shark.m11.dto.request.StrategyQueryRequest;
import com.chris.fin_shark.m11.dto.request.StrategyUpdateRequest;
import com.chris.fin_shark.m11.enums.StrategyStatus;
import com.chris.fin_shark.m11.exception.InvalidStrategyDefinitionException;
import com.chris.fin_shark.m11.exception.StrategyNotFoundException;
import com.chris.fin_shark.m11.mapper.StrategyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 策略管理服務
 *
 * @author chris
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StrategyService {

    private final StrategyMapper strategyMapper;
    private final StrategyConverter strategyConverter;

    private static final DateTimeFormatter ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final AtomicInteger idCounter = new AtomicInteger(0);

    /**
     * 查詢策略清單
     */
    public PageResponse<StrategyDTO> getStrategies(StrategyQueryRequest request) {
        log.debug("查詢策略清單: {}", request);

        List<Strategy> strategies = strategyMapper.selectStrategies(request);
        int total = strategyMapper.countStrategies(request);

        List<StrategyDTO> dtos = strategyConverter.toDTOList(strategies);

        // 補充當日信號數
        LocalDate today = LocalDate.now();
        for (StrategyDTO dto : dtos) {
            int todaySignals = strategyMapper.countTodaySignals(dto.getStrategyId(), today);
            dto.setTotalSignalsToday(todaySignals);
        }

        return PageResponse.of(dtos, request.getPage() + 1, request.getSize(), total);
    }

    /**
     * 查詢策略詳情
     */
    public StrategyDTO getStrategy(String strategyId) {
        Strategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new StrategyNotFoundException(strategyId);
        }
        return strategyConverter.toDetailDTO(strategy);
    }

    /**
     * 查詢策略詳情（指定版本）
     */
    public StrategyDTO getStrategy(String strategyId, Integer version) {
        Strategy strategy = strategyMapper.selectByIdAndVersion(strategyId, version);
        if (strategy == null) {
            throw new StrategyNotFoundException(strategyId, version);
        }
        return strategyConverter.toDetailDTO(strategy);
    }

    /**
     * 建立策略
     */
    @Transactional
    public StrategyDTO createStrategy(StrategyCreateRequest request) {
        log.info("建立策略: {}", request.getStrategyName());

        // 驗證條件定義
        validateConditions(request.getConditions());

        // 建立策略實體
        Strategy strategy = strategyConverter.toEntity(request);
        strategy.setStrategyId(generateStrategyId());
        strategy.setCreatedBy("system"); // TODO: 從認證上下文取得

        // 儲存策略
        strategyMapper.insert(strategy);

        // 儲存初始版本
        saveVersion(strategy, "Initial version");

        log.info("策略建立成功: {}", strategy.getStrategyId());
        return strategyConverter.toDetailDTO(strategy);
    }

    /**
     * 更新策略
     */
    @Transactional
    public StrategyDTO updateStrategy(String strategyId, StrategyUpdateRequest request) {
        log.info("更新策略: {}", strategyId);

        Strategy existing = strategyMapper.selectById(strategyId);
        if (existing == null) {
            throw new StrategyNotFoundException(strategyId);
        }

        // 驗證條件定義
        if (request.getConditions() != null) {
            validateConditions(request.getConditions());
            existing.setConditions(request.getConditions());
        }

        // 更新欄位
        if (request.getStrategyName() != null) {
            existing.setStrategyName(request.getStrategyName());
        }
        if (request.getDescription() != null) {
            existing.setDescription(request.getDescription());
        }
        if (request.getParameters() != null) {
            existing.setParameters(request.getParameters());
        }
        if (request.getOutput() != null) {
            existing.setOutputConfig(request.getOutput());
        }

        // 更新版本號
        existing.setCurrentVersion(existing.getCurrentVersion() + 1);
        existing.setUpdatedAt(LocalDateTime.now());

        // 儲存更新
        strategyMapper.update(existing);

        // 儲存新版本
        saveVersion(existing, "Updated strategy");

        log.info("策略更新成功: {} v{}", strategyId, existing.getCurrentVersion());
        return strategyConverter.toDetailDTO(existing);
    }

    /**
     * 更新策略狀態
     */
    @Transactional
    public StrategyDTO updateStatus(String strategyId, String targetStatus) {
        log.info("更新策略狀態: {} -> {}", strategyId, targetStatus);

        Strategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new StrategyNotFoundException(strategyId);
        }

        // 驗證狀態轉換
        StrategyStatus current = strategy.getStatus();
        StrategyStatus target = StrategyStatus.valueOf(targetStatus);

        if (!current.canTransitionTo(target)) {
            throw new InvalidStrategyDefinitionException(
                    String.format("無法從 %s 轉換至 %s", current, target));
        }

        strategyMapper.updateStatus(strategyId, targetStatus);
        strategy.setStatus(target);

        log.info("策略狀態更新成功: {} -> {}", strategyId, targetStatus);
        return strategyConverter.toDetailDTO(strategy);
    }

    /**
     * 刪除策略（封存）
     */
    @Transactional
    public void archiveStrategy(String strategyId) {
        log.info("封存策略: {}", strategyId);

        Strategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new StrategyNotFoundException(strategyId);
        }

        strategyMapper.updateStatus(strategyId, StrategyStatus.ARCHIVED.name());
        log.info("策略已封存: {}", strategyId);
    }

    /**
     * 查詢啟用的策略
     */
    public List<Strategy> getActiveStrategies() {
        return strategyMapper.selectActiveStrategies();
    }

    /**
     * 查詢預設策略
     */
    public List<Strategy> getPresetStrategies() {
        return strategyMapper.selectPresetStrategies();
    }

    /**
     * 根據 ID 取得策略（內部使用）
     */
    public Strategy getStrategyEntity(String strategyId) {
        Strategy strategy = strategyMapper.selectById(strategyId);
        if (strategy == null) {
            throw new StrategyNotFoundException(strategyId);
        }
        return strategy;
    }

    // ==================== 私有方法 ====================

    /**
     * 生成策略 ID
     */
    private String generateStrategyId() {
        int counter = idCounter.incrementAndGet();
        if (counter > 999) {
            idCounter.set(0);
        }
        return String.format("STG_CUSTOM_%s%03d",
                LocalDateTime.now().format(ID_FORMATTER).substring(0, 8),
                counter);
    }

    /**
     * 驗證條件定義
     */
    private void validateConditions(java.util.Map<String, Object> conditions) {
        if (conditions == null) {
            throw InvalidStrategyDefinitionException.missingField("conditions");
        }

        // 檢查 logic 欄位
        if (!conditions.containsKey("logic") && !conditions.containsKey("factor_id")) {
            throw InvalidStrategyDefinitionException.invalidCondition(
                    "conditions 需包含 'logic' (AND/OR) 或 'factor_id'");
        }
    }

    /**
     * 儲存策略版本
     */
    private void saveVersion(Strategy strategy, String changeSummary) {
        StrategyVersion version = StrategyVersion.builder()
                .strategyId(strategy.getStrategyId())
                .version(strategy.getCurrentVersion())
                .strategyName(strategy.getStrategyName())
                .description(strategy.getDescription())
                .conditions(strategy.getConditions())
                .parameters(strategy.getParameters())
                .outputConfig(strategy.getOutputConfig())
                .changeSummary(changeSummary)
                .createdBy(strategy.getCreatedBy())
                .createdAt(LocalDateTime.now())
                .build();

        strategyMapper.insertVersion(version);
    }
}
