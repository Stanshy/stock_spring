# M06 P1 現有檔案修改指南

> **文件編號**: GUIDE-M06-P1-MOD
> **版本**: v1.0
> **最後更新**: 2026-01-10
> **狀態**: Draft

---

## 概述

本文件列出 M06 P1 功能實作時，需要手動修改的現有檔案清單與修改內容說明。

---

## 1. M06ErrorCode.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/enums/M06ErrorCode.java`

### 新增錯誤碼

在現有列舉中新增以下錯誤碼：

```java
// ========== 財報相關 M0603x（擴充）==========

/**
 * 財報資料解析失敗
 */
M06_FINANCIAL_PARSE_ERROR("M06034", 500, "Financial statement parse error"),

/**
 * 無效的財報期間
 */
M06_FINANCIAL_PERIOD_INVALID("M06035", 400, "Invalid financial period"),

/**
 * 財報資料不完整
 */
M06_FINANCIAL_DATA_INCOMPLETE("M06036", 422, "Financial data incomplete"),

// ========== 三大法人相關 M0604x（擴充）==========

/**
 * 法人資料同步失敗
 */
M06_INSTITUTIONAL_SYNC_FAILED("M06043", 500, "Institutional trading sync failed"),

/**
 * 法人資料解析失敗
 */
M06_INSTITUTIONAL_PARSE_ERROR("M06044", 500, "Institutional trading parse error"),

// ========== 融資融券相關 M0605x（擴充）==========

/**
 * 融資融券資料同步失敗
 */
M06_MARGIN_SYNC_FAILED("M06053", 500, "Margin trading sync failed"),

/**
 * 融資融券資料解析失敗
 */
M06_MARGIN_PARSE_ERROR("M06054", 500, "Margin trading parse error"),

// ========== 資料品質相關 M0608x（擴充）==========

/**
 * 品質檢核執行失敗
 */
M06_QUALITY_CHECK_EXECUTION_FAILED("M06084", 500, "Quality check execution failed"),

/**
 * 無效的品質檢核規則
 */
M06_QUALITY_RULE_INVALID("M06085", 400, "Invalid quality check rule"),

/**
 * 品質檢核執行逾時
 */
M06_QUALITY_CHECK_TIMEOUT("M06086", 504, "Quality check timeout"),

// ========== 資料補齊相關 M0610x（新範圍）==========

/**
 * 無效的補齊日期範圍
 */
M06_REPAIR_DATE_RANGE_INVALID("M06101", 400, "Invalid repair date range"),

/**
 * 無效的補齊策略
 */
M06_REPAIR_STRATEGY_INVALID("M06102", 400, "Invalid repair strategy"),

/**
 * 資料補齊執行失敗
 */
M06_REPAIR_EXECUTION_FAILED("M06103", 500, "Data repair execution failed"),

/**
 * 沒有需要補齊的資料
 */
M06_REPAIR_NO_MISSING_DATA("M06104", 200, "No missing data to repair"),

/**
 * 部分資料補齊成功
 */
M06_REPAIR_PARTIAL_SUCCESS("M06105", 207, "Partial data repair success");
```

---

## 2. JobManagementService.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/service/JobManagementService.java`

### 新增方法

```java
/**
 * 觸發法人買賣超同步
 *
 * @param tradeDate 交易日期
 * @return Job 執行記錄
 */
public JobExecution triggerInstitutionalSync(LocalDate tradeDate) {
    log.info("手動觸發法人買賣超同步: tradeDate={}", tradeDate);
    institutionalTradingSyncJob.syncInstitutionalTradingManually(tradeDate);
    // 查詢並返回最新的執行記錄
    return jobExecutionRepository.findLatestByJobName("InstitutionalTradingSync")
            .orElse(null);
}

/**
 * 觸發融資融券同步
 *
 * @param tradeDate 交易日期
 * @return Job 執行記錄
 */
public JobExecution triggerMarginSync(LocalDate tradeDate) {
    log.info("手動觸發融資融券同步: tradeDate={}", tradeDate);
    marginTradingSyncJob.syncMarginTradingManually(tradeDate);
    return jobExecutionRepository.findLatestByJobName("MarginTradingSync")
            .orElse(null);
}

/**
 * 觸發財報同步
 *
 * @param year    年度
 * @param quarter 季度
 * @return Job 執行記錄
 */
public JobExecution triggerFinancialSync(int year, short quarter) {
    log.info("手動觸發財報同步: year={}, quarter={}", year, quarter);
    financialStatementSyncJob.syncFinancialStatementsManually(year, quarter);
    return jobExecutionRepository.findLatestByJobName("FinancialStatementSync")
            .orElse(null);
}

/**
 * 觸發資料品質檢核
 *
 * @return Job 執行記錄
 */
public JobExecution triggerQualityCheck() {
    log.info("手動觸發資料品質檢核");
    dataQualityCheckJob.runQualityCheckManually();
    return jobExecutionRepository.findLatestByJobName("DataQualityCheck")
            .orElse(null);
}
```

### 新增依賴注入

```java
private final InstitutionalTradingSyncJob institutionalTradingSyncJob;
private final MarginTradingSyncJob marginTradingSyncJob;
private final FinancialStatementSyncJob financialStatementSyncJob;
private final DataQualityCheckJob dataQualityCheckJob;
```

---

## 3. JobManagementController.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/controller/JobManagementController.java`

### 新增端點

```java
/**
 * 手動觸發法人買賣超同步
 *
 * @param tradeDate 交易日期
 * @return Job 執行記錄
 */
@PostMapping("/trigger/institutional-sync")
public ApiResponse<JobExecutionDTO> triggerInstitutionalSync(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate) {

    log.info("POST /api/jobs/trigger/institutional-sync?tradeDate={}", tradeDate);

    JobExecution execution = jobManagementService.triggerInstitutionalSync(tradeDate);
    return ApiResponse.success(jobExecutionConverter.toDTO(execution));
}

/**
 * 手動觸發融資融券同步
 *
 * @param tradeDate 交易日期
 * @return Job 執行記錄
 */
@PostMapping("/trigger/margin-sync")
public ApiResponse<JobExecutionDTO> triggerMarginSync(
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate tradeDate) {

    log.info("POST /api/jobs/trigger/margin-sync?tradeDate={}", tradeDate);

    JobExecution execution = jobManagementService.triggerMarginSync(tradeDate);
    return ApiResponse.success(jobExecutionConverter.toDTO(execution));
}

/**
 * 手動觸發財報同步
 *
 * @param year    年度
 * @param quarter 季度
 * @return Job 執行記錄
 */
@PostMapping("/trigger/financial-sync")
public ApiResponse<JobExecutionDTO> triggerFinancialSync(
        @RequestParam Integer year,
        @RequestParam Short quarter) {

    log.info("POST /api/jobs/trigger/financial-sync?year={}&quarter={}", year, quarter);

    JobExecution execution = jobManagementService.triggerFinancialSync(year, quarter);
    return ApiResponse.success(jobExecutionConverter.toDTO(execution));
}

/**
 * 手動觸發資料品質檢核
 *
 * @return Job 執行記錄
 */
@PostMapping("/trigger/quality-check")
public ApiResponse<JobExecutionDTO> triggerQualityCheck() {

    log.info("POST /api/jobs/trigger/quality-check");

    JobExecution execution = jobManagementService.triggerQualityCheck();
    return ApiResponse.success(jobExecutionConverter.toDTO(execution));
}
```

### 移除現有 FeatureNotImplementedException

將現有的 `triggerFinancialSync` 和 `triggerDataQualityCheck` 方法中的 `throw new FeatureNotImplementedException()` 改為實際實作。

---

## 4. MarketDataQueryService.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/service/MarketDataQueryService.java`

### 修改 TODO 方法

將以下方法從 TODO 狀態改為實際實作：

```java
/**
 * 查詢法人買賣超
 */
public List<InstitutionalTradingDTO> queryInstitutionalTrading(
        String stockId, LocalDate startDate, LocalDate endDate, Integer days) {

    List<InstitutionalTrading> data;

    if (startDate != null && endDate != null) {
        data = institutionalTradingRepository.findByStockIdAndDateRange(stockId, startDate, endDate);
    } else {
        data = institutionalTradingRepository
                .findByStockIdOrderByTradeDateDesc(stockId, PageRequest.of(0, days))
                .getContent();
    }

    return institutionalTradingConverter.toDTOList(data);
}

/**
 * 查詢融資融券
 */
public List<MarginTradingDTO> queryMarginTrading(
        String stockId, LocalDate startDate, LocalDate endDate, Integer days) {

    List<MarginTrading> data;

    if (startDate != null && endDate != null) {
        data = marginTradingRepository.findByStockIdAndDateRange(stockId, startDate, endDate);
    } else {
        data = marginTradingRepository
                .findByStockIdOrderByTradeDateDesc(stockId, PageRequest.of(0, days))
                .getContent();
    }

    return marginTradingConverter.toDTOList(data);
}
```

### 新增依賴注入

```java
private final InstitutionalTradingRepository institutionalTradingRepository;
private final MarginTradingRepository marginTradingRepository;
private final InstitutionalTradingConverter institutionalTradingConverter;
private final MarginTradingConverter marginTradingConverter;
```

---

## 5. DataQualityService.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/service/DataQualityService.java`

### 修改 runQualityCheck 方法

將 TODO 狀態改為實際實作：

```java
/**
 * 執行品質檢核
 */
public QualityCheckResultDTO runQualityCheck(QualityCheckExecuteRequest request) {
    return dataQualityExecutionService.executeQualityCheck(request, TriggerType.MANUAL);
}
```

### 新增依賴注入

```java
private final DataQualityExecutionService dataQualityExecutionService;
```

---

## 6. DataQualityController.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/controller/DataQualityController.java`

### 修改 runQualityCheck 端點

將現有的 TODO 實作改為：

```java
/**
 * 執行品質檢核
 *
 * @param request 檢核請求
 * @return 檢核結果
 */
@PostMapping("/run-check")
public ApiResponse<QualityCheckResultDTO> runQualityCheck(
        @Valid @RequestBody QualityCheckExecuteRequest request) {

    log.info("POST /api/data-quality/run-check: {}", request);

    QualityCheckResultDTO result = dataQualityService.runQualityCheck(request);
    return ApiResponse.success(result);
}
```

### 新增 import

```java
import com.chris.fin_shark.m06.dto.QualityCheckResultDTO;
import com.chris.fin_shark.m06.dto.request.QualityCheckExecuteRequest;
```

---

## 7. MarginTradingDTO.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/dto/MarginTradingDTO.java`

### 確認欄位完整性

確保以下欄位存在（與 Entity 對應）：

```java
/** 融資融券 ID */
@JsonProperty("margin_id")
private Long marginId;

/** 融資使用率 */
@JsonProperty("margin_usage_rate")
private BigDecimal marginUsageRate;

/** 融券使用率 */
@JsonProperty("short_usage_rate")
private BigDecimal shortUsageRate;
```

---

## 8. common/enums/JobType.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/common/enums/JobType.java`

### 確認 JobType 列舉值

確保以下值存在：

```java
DATA_SYNC("DATA_SYNC", "資料同步"),
DATA_QUALITY("DATA_QUALITY", "資料品質檢核"),
DATA_REPAIR("DATA_REPAIR", "資料補齊");
```

---

## 修改檢查清單

| 檔案 | 修改類型 | 優先級 | 備註 |
|------|---------|-------|------|
| M06ErrorCode.java | 新增列舉值 | 高 | P1 新增錯誤碼 |
| JobManagementService.java | 新增方法 + 依賴 | 高 | 連接新 Job |
| JobManagementController.java | 新增端點 | 高 | 提供手動觸發 |
| MarketDataQueryService.java | 實作 TODO | 中 | 法人/融資融券查詢 |
| DataQualityService.java | 實作 TODO | 中 | 品質檢核執行 |
| DataQualityController.java | 修改端點 | 中 | 移除 FeatureNotImplemented |
| MarginTradingDTO.java | 確認欄位 | 低 | 欄位可能已存在 |
| common/enums/JobType.java | 確認列舉值 | 低 | 可能已存在 |

---

## 注意事項

1. **依賴順序**：修改時請注意類別間的依賴關係，建議按照以下順序進行：
   - 先修改 Enum（M06ErrorCode, JobType）
   - 再修改 DTO（MarginTradingDTO）
   - 然後修改 Service（MarketDataQueryService, DataQualityService, JobManagementService）
   - 最後修改 Controller（JobManagementController, DataQualityController）

2. **測試**：修改完成後請執行相關單元測試確保功能正常。

3. **編譯檢查**：修改後請執行 `mvnw.cmd clean compile` 確認無編譯錯誤。

---

*文件結束*
