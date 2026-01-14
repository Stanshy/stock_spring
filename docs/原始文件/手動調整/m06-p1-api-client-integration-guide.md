# M06 P1 外部 API Client 整合指南

> **文件編號**: GUIDE-M06-P1-API
> **版本**: v1.1
> **最後更新**: 2026-01-10
> **狀態**: Draft

---

## 概述

本文件說明如何將新建立的外部 API Client 整合到 M06 P1 的 SyncService 中。

**Mapper 使用批次 UPSERT 模式**：`batchInsert(List<Entity>)` + `ON CONFLICT DO UPDATE`

---

## 新增的 API Client

| Client | 檔案路徑 | 用途 |
|--------|---------|------|
| `TwseInstitutionalClient` | `client/twse/TwseInstitutionalClient.java` | 法人買賣超 |
| `TwseMarginClient` | `client/twse/TwseMarginClient.java` | 融資融券 |
| `FinMindClient` | `client/finmind/FinMindClient.java` | 財務報表 |

---

## 1. InstitutionalTradingSyncService.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/service/InstitutionalTradingSyncService.java`

### 1.1 新增 import

```java
import com.chris.fin_shark.client.twse.TwseInstitutionalClient;
import com.chris.fin_shark.common.dto.external.TwseInstitutionalData;
import java.util.Set;
import java.util.stream.Collectors;
```

### 1.2 修改依賴注入

將原本的：
```java
private final TwseApiClient twseApiClient;
```

改為：
```java
private final TwseInstitutionalClient twseInstitutionalClient;
```

### 1.3 修改 syncInstitutionalTradingForDate 方法

**原始程式碼（第 67-104 行）整段替換為：**

```java
// 3. 查詢所有活躍股票
List<Stock> activeStocks = ______________________________;  // 你的查詢活躍股票方法
Set<String> stockIds = activeStocks.stream()
        .map(Stock::getStockId)
        .collect(Collectors.toSet());
execution.setTotalItems(stockIds.size());
log.info("查詢到 {} 檔活躍股票", stockIds.size());

// 4. 批次呼叫 TWSE API（一次取得所有股票資料）
List<TwseInstitutionalData> apiDataList = twseInstitutionalClient.getInstitutionalTrading(tradeDate, stockIds);
log.info("從 TWSE API 取得 {} 筆法人資料", apiDataList.size());

if (apiDataList.isEmpty()) {
    log.warn("TWSE API 未回傳任何資料，可能非交易日或 API 異常");
    execution.setJobStatus(JobStatus.FAILED.getCode());
    execution.setErrorMessage("API 未回傳資料");
    execution.setEndTime(LocalDateTime.now());
    return jobExecutionRepository.save(execution);
}

// 5. 轉換 DTO -> Entity
List<InstitutionalTrading> entities = apiDataList.stream()
        .map(this::convertToEntity)
        .collect(Collectors.toList());

// 6. 批次儲存（UPSERT）
try {
    institutionalTradingMapper.batchInsert(entities);
    execution.setSuccessItems(entities.size());
    execution.setFailedItems(0);
    log.info("法人買賣超批次儲存成功: {} 筆", entities.size());
} catch (Exception e) {
    log.error("法人買賣超批次儲存失敗", e);
    execution.setSuccessItems(0);
    execution.setFailedItems(entities.size());
    throw e;
}

execution.setProcessedItems(entities.size());

// 7. 更新執行結果
long durationMs = java.time.Duration.between(
        execution.getStartTime(), LocalDateTime.now()).toMillis();

execution.setJobStatus(JobStatus.SUCCESS.getCode());
execution.setEndTime(LocalDateTime.now());
execution.setDurationMs(durationMs);

log.info("法人買賣超同步完成: 成功 {}, 耗時 {}ms", entities.size(), durationMs);
```

### 1.4 新增 convertToEntity 方法

**刪除原本的 `syncSingleStock` 方法，新增：**

```java
/**
 * 轉換 API DTO 為 Entity
 */
private InstitutionalTrading convertToEntity(TwseInstitutionalData data) {
    return InstitutionalTrading.builder()
            .stockId(data.getStockId())
            .tradeDate(data.getTradeDate())
            .foreignBuy(data.getForeignBuy())
            .foreignSell(data.getForeignSell())
            .trustBuy(data.getTrustBuy())
            .trustSell(data.getTrustSell())
            .dealerBuy(data.getDealerBuy())
            .dealerSell(data.getDealerSell())
            .build();
}
```

---

## 2. MarginTradingSyncService.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/service/MarginTradingSyncService.java`

### 2.1 新增 import

```java
import com.chris.fin_shark.client.twse.TwseMarginClient;
import com.chris.fin_shark.common.dto.external.TwseMarginData;
import java.util.Set;
import java.util.stream.Collectors;
```

### 2.2 修改依賴注入

將原本的：
```java
private final TwseApiClient twseApiClient;
```

改為：
```java
private final TwseMarginClient twseMarginClient;
```

### 2.3 修改 syncMarginTradingForDate 方法

**原始程式碼（第 67-104 行）整段替換為：**

```java
// 3. 查詢所有活躍股票
List<Stock> activeStocks = ______________________________;  // 你的查詢活躍股票方法
Set<String> stockIds = activeStocks.stream()
        .map(Stock::getStockId)
        .collect(Collectors.toSet());
execution.setTotalItems(stockIds.size());
log.info("查詢到 {} 檔活躍股票", stockIds.size());

// 4. 批次呼叫 TWSE API
List<TwseMarginData> apiDataList = twseMarginClient.getMarginTrading(tradeDate, stockIds);
log.info("從 TWSE API 取得 {} 筆融資融券資料", apiDataList.size());

if (apiDataList.isEmpty()) {
    log.warn("TWSE API 未回傳任何資料，可能非交易日或 API 異常");
    execution.setJobStatus(JobStatus.FAILED.getCode());
    execution.setErrorMessage("API 未回傳資料");
    execution.setEndTime(LocalDateTime.now());
    return jobExecutionRepository.save(execution);
}

// 5. 轉換 DTO -> Entity
List<MarginTrading> entities = apiDataList.stream()
        .map(this::convertToEntity)
        .collect(Collectors.toList());

// 6. 批次儲存（UPSERT）
try {
    marginTradingMapper.batchInsert(entities);
    execution.setSuccessItems(entities.size());
    execution.setFailedItems(0);
    log.info("融資融券批次儲存成功: {} 筆", entities.size());
} catch (Exception e) {
    log.error("融資融券批次儲存失敗", e);
    execution.setSuccessItems(0);
    execution.setFailedItems(entities.size());
    throw e;
}

execution.setProcessedItems(entities.size());

// 7. 更新執行結果
long durationMs = java.time.Duration.between(
        execution.getStartTime(), LocalDateTime.now()).toMillis();

execution.setJobStatus(JobStatus.SUCCESS.getCode());
execution.setEndTime(LocalDateTime.now());
execution.setDurationMs(durationMs);

log.info("融資融券同步完成: 成功 {}, 耗時 {}ms", entities.size(), durationMs);
```

### 2.4 新增 convertToEntity 方法

**刪除原本的 `syncSingleStock` 方法，新增：**

```java
/**
 * 轉換 API DTO 為 Entity
 */
private MarginTrading convertToEntity(TwseMarginData data) {
    return MarginTrading.builder()
            .stockId(data.getStockId())
            .tradeDate(data.getTradeDate())
            .marginPurchase(data.getMarginPurchase())
            .marginSell(data.getMarginSell())
            .marginBalance(data.getMarginBalance())
            .marginQuota(data.getMarginQuota())
            .shortPurchase(data.getShortPurchase())
            .shortSell(data.getShortSell())
            .shortBalance(data.getShortBalance())
            .shortQuota(data.getShortQuota())
            .build();
}
```

---

## 3. FinancialStatementSyncService.java

**檔案路徑**: `src/main/java/com/chris/fin_shark/m06/service/FinancialStatementSyncService.java`

### 3.1 新增 import

```java
import com.chris.fin_shark.client.finmind.FinMindClient;
import com.chris.fin_shark.common.dto.external.FinMindFinancialData;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.stream.Collectors;
```

### 3.2 新增依賴注入

```java
private final FinMindClient finMindClient;
```

### 3.3 修改 syncFinancialStatementsForPeriod 方法

**原始程式碼（第 57-95 行）整段替換為：**

```java
// 查詢所有活躍股票
List<Stock> activeStocks = ______________________________;  // 你的查詢活躍股票方法
List<String> stockIds = activeStocks.stream()
        .map(Stock::getStockId)
        .collect(Collectors.toList());
execution.setTotalItems(stockIds.size());
log.info("查詢到 {} 檔活躍股票", stockIds.size());

// 計算查詢日期範圍
LocalDate startDate = LocalDate.of(year, (quarter - 1) * 3 + 1, 1);
LocalDate endDate = startDate.plusMonths(3).minusDays(1);

List<FinancialStatement> allEntities = new ArrayList<>();
int failCount = 0;

// 逐一查詢每檔股票（FinMind 需要逐檔查詢）
for (String stockId : stockIds) {
    try {
        List<FinMindFinancialData> dataList = finMindClient.getFinancialStatements(
                stockId, startDate, endDate);

        // 找出指定季度的資料
        FinMindFinancialData data = dataList.stream()
                .filter(d -> d.getYear() == year && d.getQuarter() == quarter)
                .findFirst()
                .orElse(null);

        if (data != null) {
            allEntities.add(convertToEntity(data));
            log.debug("取得財報: {} {}Q{}", stockId, year, quarter);
        } else {
            log.warn("未找到財報資料: {} {}Q{}", stockId, year, quarter);
            failCount++;
        }

    } catch (Exception e) {
        failCount++;
        log.error("財報查詢失敗: stockId={}", stockId, e);
    }

    execution.setProcessedItems(allEntities.size() + failCount);

    // 避免 API 限流
    Thread.sleep(300);
}

// 批次儲存（UPSERT）
if (!allEntities.isEmpty()) {
    try {
        financialStatementMapper.batchInsert(allEntities);
        execution.setSuccessItems(allEntities.size());
        log.info("財報批次儲存成功: {} 筆", allEntities.size());
    } catch (Exception e) {
        log.error("財報批次儲存失敗", e);
        execution.setSuccessItems(0);
        failCount += allEntities.size();
        throw e;
    }
}

execution.setFailedItems(failCount);

// 更新執行結果
long durationMs = java.time.Duration.between(
        execution.getStartTime(), LocalDateTime.now()).toMillis();

execution.setJobStatus(JobStatus.SUCCESS.getCode());
execution.setEndTime(LocalDateTime.now());
execution.setDurationMs(durationMs);

log.info("財報同步完成: 成功 {}, 失敗 {}, 耗時 {}ms",
        allEntities.size(), failCount, durationMs);
```

### 3.4 新增 convertToEntity 方法

**刪除原本的 `syncSingleStock` 方法，新增：**

```java
/**
 * 轉換 API DTO 為 Entity
 */
private FinancialStatement convertToEntity(FinMindFinancialData data) {
    return FinancialStatement.builder()
            .stockId(data.getStockId())
            .year(data.getYear())
            .quarter(data.getQuarter())
            .reportType(data.getReportType() != null ? data.getReportType() : "Q")
            // 損益表
            .revenue(data.getRevenue())
            .grossProfit(data.getGrossProfit())
            .operatingExpense(data.getOperatingExpense())
            .operatingIncome(data.getOperatingIncome())
            .netIncome(data.getNetIncome())
            // 資產負債表
            .totalAssets(data.getTotalAssets())
            .totalLiabilities(data.getTotalLiabilities())
            .equity(data.getEquity())
            .currentAssets(data.getCurrentAssets())
            .currentLiabilities(data.getCurrentLiabilities())
            // 現金流量表
            .operatingCashFlow(data.getOperatingCashFlow())
            .investingCashFlow(data.getInvestingCashFlow())
            .financingCashFlow(data.getFinancingCashFlow())
            // 每股指標
            .eps(data.getEps())
            .bps(data.getBps())
            // 來源
            .source("FinMind")
            .publishDate(data.getDate())
            .build();
}
```

---

## 4. application.yml 設定（可選）

如果要使用 FinMind API Token（提高請求限額），請在 `application.yml` 加入：

```yaml
finmind:
  api:
    token: ${FINMIND_API_TOKEN:}  # 可選，從環境變數讀取
```

---

## 5. 修改檢查清單

| 檔案 | 修改類型 | 說明 |
|------|---------|------|
| `InstitutionalTradingSyncService.java` | 整合 Client | 批次呼叫 + 批次儲存 |
| `MarginTradingSyncService.java` | 整合 Client | 批次呼叫 + 批次儲存 |
| `FinancialStatementSyncService.java` | 整合 Client | 逐檔查詢 + 批次儲存 |

---

## 6. Mapper 方法對照

| Mapper | 方法 | 參數名稱 |
|--------|------|---------|
| `InstitutionalTradingMapper` | `batchInsert` | `tradingList` |
| `MarginTradingMapper` | `batchInsert` | `marginList` |
| `FinancialStatementMapper` | `batchInsert` | `statements` |

---

## 7. API 呼叫效率

| 資料類型 | API 呼叫次數 | 儲存方式 |
|---------|-------------|---------|
| 法人買賣超 | **1 次** | 批次 UPSERT |
| 融資融券 | **1 次** | 批次 UPSERT |
| 財報 | **20 次**（每檔 1 次） | 批次 UPSERT |

你的 20 檔股票，每日同步：**2 次 TWSE API + 20 次 FinMind API**

---

## 8. 注意事項

1. **查詢活躍股票**：請將 `______________________________` 替換為你自己的方法。

2. **Mapper 參數名稱**：
   - 確認你的 Mapper 介面參數有 `@Param` 註解
   - 例如：`void batchInsert(@Param("tradingList") List<InstitutionalTrading> tradingList);`

3. **UPSERT 唯一鍵**：
   - `institutional_trading`: `(stock_id, trade_date)`
   - `margin_trading`: `(stock_id, trade_date)`
   - `financial_statements`: `(stock_id, year, quarter)`

---

*文件結束*
