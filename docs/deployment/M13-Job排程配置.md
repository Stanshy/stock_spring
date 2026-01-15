# M13-信號判斷引擎 Job 排程配置

> **文件編號**: JOB-M13
> **模組名稱**: 信號判斷引擎 (Signal Judgment Engine)
> **版本**: v1.0
> **最後更新**: 2026-01-14
> **狀態**: Draft

---

## 1. Job 總覽

| # | Job 名稱 | Cron 表達式 | 執行時間 | 說明 |
|---|---------|-------------|---------|------|
| 1 | DailySignalProcessingJob | `0 0 17 * * MON-FRI` | 17:00 | 每日信號處理主 Job |
| 2 | SignalCollectionJob | `0 5 17 * * MON-FRI` | 17:05 | 信號收集（可單獨執行） |
| 3 | SignalDeduplicationJob | `0 15 17 * * MON-FRI` | 17:15 | 信號去重（可單獨執行） |
| 4 | SignalMergeJob | `0 20 17 * * MON-FRI` | 17:20 | 信號合併（可單獨執行） |
| 5 | SignalScoringJob | `0 25 17 * * MON-FRI` | 17:25 | 信號評分（可單獨執行） |
| 6 | SignalPublishJob | `0 30 17 * * MON-FRI` | 17:30 | 信號發布 |
| 7 | HistoricalAccuracyUpdateJob | `0 0 6 * * SAT` | 週六 06:00 | 更新歷史準確率 |
| 8 | SignalDataCleanupJob | `0 0 3 * * SUN` | 週日 03:00 | 清理過期信號資料 |

---

## 2. Job 執行時序

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                    每日信號處理 Job 時序圖                                    │
│                      (交易日 17:00 - 17:35)                                  │
└─────────────────────────────────────────────────────────────────────────────┘

時間     Job                         說明
─────────────────────────────────────────────────────────────────────────────
17:00 ─→ DailySignalProcessingJob   建立批次，啟動處理流程
    │    ├─ 建立 batch_id
    │    ├─ 設定 status = COLLECTING
    │    └─ 觸發子 Job
    │
17:05 ─→ SignalCollectionJob        從 M07-M12 收集信號
    │    ├─ 查詢 M07 technical_signals (約 2 分鐘)
    │    ├─ 查詢 M08 fundamental_signals (約 1 分鐘)
    │    ├─ 查詢 M09 chip_signals (約 3 分鐘)
    │    ├─ 查詢 M10 pattern_signals (約 1 分鐘)
    │    ├─ 查詢 M11 strategy_signals (約 2 分鐘)
    │    └─ 查詢 M12 industry_signals (約 1 分鐘)
    │
17:15 ─→ SignalDeduplicationJob     去重處理
    │    ├─ 完全重複去除
    │    ├─ 語義重複合併
    │    └─ 時間窗口去重
    │
17:20 ─→ SignalMergeJob             信號合併
    │    ├─ 按股票分組
    │    ├─ 計算方向一致性
    │    └─ 產生統一信號
    │
17:25 ─→ SignalScoringJob           信號評分
    │    ├─ 計算 5 維度分數
    │    ├─ 應用調整因素
    │    └─ 決定評級
    │
17:30 ─→ SignalPublishJob           信號發布
         ├─ 產生推薦清單
         ├─ 檢查訂閱觸發
         └─ 發布就緒事件 → M14/M15/M16/M17/M18

預計完成: 17:35
```

---

## 3. Job 詳細配置

### 3.1 DailySignalProcessingJob

**主控 Job**: 協調整個每日信號處理流程。

```java
@Component
public class DailySignalProcessingJob extends QuartzJobBean {

    @Autowired
    private SignalProcessingOrchestrator orchestrator;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        LocalDate tradeDate = LocalDate.now();

        // 檢查是否為交易日
        if (!isTradingDay(tradeDate)) {
            log.info("Not a trading day, skipping signal processing");
            return;
        }

        // 執行完整處理流程
        ProcessingResult result = orchestrator.executeFullPipeline(tradeDate);

        log.info("Daily signal processing completed: {} unified signals created",
            result.getUnifiedSignalsCreated());
    }
}
```

**Quartz 配置**:
```yaml
jobs:
  daily-signal-processing:
    job-class: com.chris.fin_shark.m13.job.DailySignalProcessingJob
    cron: "0 0 17 * * MON-FRI"
    timezone: Asia/Taipei
    description: "每日信號處理主 Job"
    misfire-instruction: MISFIRE_INSTRUCTION_FIRE_ONCE_NOW
    recovery: true
    durability: true
    parameters:
      timeout-minutes: 60
      retry-count: 3
```

---

### 3.2 SignalCollectionJob

**功能**: 從 6 個上游模組收集原始信號。

```java
@Component
public class SignalCollectionJob extends QuartzJobBean {

    @Autowired
    private SignalCollector collector;

    @Autowired
    private BatchService batchService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String batchId = dataMap.getString("batchId");
        LocalDate tradeDate = (LocalDate) dataMap.get("tradeDate");

        // 如果沒有批次 ID，建立新批次
        if (batchId == null) {
            batchId = batchService.createBatch(tradeDate, BatchType.DAILY_FULL);
        }

        // 更新批次狀態
        batchService.updateStatus(batchId, BatchStatus.COLLECTING);

        try {
            CollectionResult result = collector.collectDailySignals(tradeDate, batchId);

            // 記錄結果
            batchService.recordCollectionResult(batchId, result);

            log.info("Signal collection completed: {} signals from {} modules",
                result.getTotalCollected(), result.getModuleCount());

        } catch (Exception e) {
            batchService.markFailed(batchId, e.getMessage());
            throw e;
        }
    }
}
```

**配置參數**:
| 參數 | 預設值 | 說明 |
|-----|-------|------|
| batch-size | 500 | 每批次收集數量 |
| source-modules | M07,M08,M09,M10,M11,M12 | 收集來源模組 |
| timeout-per-module | 5 分鐘 | 每模組超時時間 |
| retry-on-failure | true | 失敗是否重試 |

---

### 3.3 SignalDeduplicationJob

**功能**: 去除重複與語義相關信號。

```java
@Component
public class SignalDeduplicationJob extends QuartzJobBean {

    @Autowired
    private SignalDeduplicator deduplicator;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String batchId = dataMap.getString("batchId");
        LocalDate tradeDate = (LocalDate) dataMap.get("tradeDate");

        // 更新批次狀態
        batchService.updateStatus(batchId, BatchStatus.DEDUPLICATING);

        DedupResult result = deduplicator.deduplicate(batchId, tradeDate);

        // 記錄結果
        batchService.recordDedupResult(batchId, result);

        log.info("Deduplication completed: {} -> {} (rate: {:.1%})",
            result.getBeforeCount(),
            result.getAfterCount(),
            result.getDedupRate());
    }
}
```

**配置參數**:
| 參數 | 預設值 | 說明 |
|-----|-------|------|
| temporal-window-days | 3 | 時間窗口去重天數 |
| semantic-groups-enabled | true | 是否啟用語義群組 |
| log-removed-signals | true | 是否記錄被移除信號 |

---

### 3.4 SignalMergeJob

**功能**: 合併同股票的多個信號。

```java
@Component
public class SignalMergeJob extends QuartzJobBean {

    @Autowired
    private SignalMerger merger;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String batchId = dataMap.getString("batchId");
        LocalDate tradeDate = (LocalDate) dataMap.get("tradeDate");

        // 更新批次狀態
        batchService.updateStatus(batchId, BatchStatus.MERGING);

        MergeResult result = merger.mergeSignals(batchId, tradeDate);

        // 記錄結果
        batchService.recordMergeResult(batchId, result);

        log.info("Signal merge completed: {} unified signals created",
            result.getUnifiedCount());
    }
}
```

**配置參數**:
| 參數 | 預設值 | 說明 |
|-----|-------|------|
| min-signals-for-strong | 3 | 強方向最少信號數 |
| include-stock-info | true | 是否載入股票名稱 |
| max-key-factors | 3 | 關鍵因素最大數量 |

---

### 3.5 SignalScoringJob

**功能**: 計算統一信號評分。

```java
@Component
public class SignalScoringJob extends QuartzJobBean {

    @Autowired
    private SignalScorer scorer;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String batchId = dataMap.getString("batchId");
        LocalDate tradeDate = (LocalDate) dataMap.get("tradeDate");

        // 更新批次狀態
        batchService.updateStatus(batchId, BatchStatus.SCORING);

        ScoringResult result = scorer.scoreSignals(batchId, tradeDate);

        // 記錄結果
        batchService.recordScoringResult(batchId, result);

        log.info("Signal scoring completed: {} signals scored, avg score: {:.1f}",
            result.getScoredCount(), result.getAvgScore());
    }
}
```

**配置參數**:
| 參數 | 預設值 | 說明 |
|-----|-------|------|
| historical-lookback-days | 90 | 歷史績效回溯天數 |
| enable-market-adjustment | true | 是否啟用市場環境調整 |
| scoring-parallel-threads | 4 | 評分平行執行緒數 |

---

### 3.6 SignalPublishJob

**功能**: 發布統一信號與推薦清單。

```java
@Component
public class SignalPublishJob extends QuartzJobBean {

    @Autowired
    private SignalPublisher publisher;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String batchId = dataMap.getString("batchId");
        LocalDate tradeDate = (LocalDate) dataMap.get("tradeDate");

        // 更新批次狀態
        batchService.updateStatus(batchId, BatchStatus.PUBLISHING);

        PublishResult result = publisher.publishSignals(batchId, tradeDate);

        // 完成批次
        batchService.completeBatch(batchId);

        log.info("Signal publish completed: {} buy / {} sell recommendations, " +
            "{} subscriptions triggered",
            result.getBuyCount(),
            result.getSellCount(),
            result.getSubscriptionsTriggered());
    }
}
```

**配置參數**:
| 參數 | 預設值 | 說明 |
|-----|-------|------|
| recommendation-min-grade | B | 推薦清單最低評級 |
| recommendation-max-count | 50 | 推薦清單最大數量 |
| notification-timeout | 30 秒 | 通知發送超時 |

---

### 3.7 HistoricalAccuracyUpdateJob

**功能**: 更新信號歷史準確率統計。

```java
@Component
public class HistoricalAccuracyUpdateJob extends QuartzJobBean {

    @Autowired
    private HistoricalAccuracyService accuracyService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        // 計算過去 7 天信號的實際表現
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusDays(7);

        // 統計 5 日後的價格變化
        AccuracyUpdateResult result = accuracyService.updateAccuracy(
            startDate, endDate, 5  // 5 日後驗證
        );

        log.info("Historical accuracy updated: {} signals evaluated, " +
            "buy accuracy: {:.1%}, sell accuracy: {:.1%}",
            result.getTotalEvaluated(),
            result.getBuyAccuracy(),
            result.getSellAccuracy());
    }
}
```

**Quartz 配置**:
```yaml
jobs:
  historical-accuracy-update:
    job-class: com.chris.fin_shark.m13.job.HistoricalAccuracyUpdateJob
    cron: "0 0 6 * * SAT"
    timezone: Asia/Taipei
    description: "每週六更新歷史準確率"
    parameters:
      evaluation-days: 5
      lookback-weeks: 1
```

---

### 3.8 SignalDataCleanupJob

**功能**: 清理過期信號資料。

```java
@Component
public class SignalDataCleanupJob extends QuartzJobBean {

    @Autowired
    private SignalCleanupService cleanupService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        LocalDate today = LocalDate.now();

        // 清理原始信號（90 天前）
        int rawDeleted = cleanupService.cleanupRawSignals(
            today.minusDays(90)
        );

        // 清理去重日誌（30 天前）
        int dedupLogsDeleted = cleanupService.cleanupDedupLogs(
            today.minusDays(30)
        );

        // 歸檔舊的統一信號（2 年前）
        int archived = cleanupService.archiveOldUnifiedSignals(
            today.minusYears(2)
        );

        log.info("Signal cleanup completed: " +
            "raw signals deleted: {}, dedup logs deleted: {}, archived: {}",
            rawDeleted, dedupLogsDeleted, archived);
    }
}
```

**Quartz 配置**:
```yaml
jobs:
  signal-data-cleanup:
    job-class: com.chris.fin_shark.m13.job.SignalDataCleanupJob
    cron: "0 0 3 * * SUN"
    timezone: Asia/Taipei
    description: "每週日清理過期信號資料"
    parameters:
      raw-signal-retention-days: 90
      dedup-log-retention-days: 30
      archive-after-years: 2
```

---

## 4. Job 相依性

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                          Job 相依性圖                                        │
└─────────────────────────────────────────────────────────────────────────────┘

                    ┌─────────────────────────────┐
                    │  DailySignalProcessingJob   │
                    │         (主控 Job)          │
                    └─────────────────────────────┘
                                  │
                                  │ 觸發
                                  ▼
                    ┌─────────────────────────────┐
                    │     SignalCollectionJob     │
                    │       (信號收集)            │
                    └─────────────────────────────┘
                                  │
                                  │ 相依
                                  ▼
                    ┌─────────────────────────────┐
                    │   SignalDeduplicationJob    │
                    │       (信號去重)            │
                    └─────────────────────────────┘
                                  │
                                  │ 相依
                                  ▼
                    ┌─────────────────────────────┐
                    │      SignalMergeJob         │
                    │       (信號合併)            │
                    └─────────────────────────────┘
                                  │
                                  │ 相依
                                  ▼
                    ┌─────────────────────────────┐
                    │     SignalScoringJob        │
                    │       (信號評分)            │
                    └─────────────────────────────┘
                                  │
                                  │ 相依
                                  ▼
                    ┌─────────────────────────────┐
                    │     SignalPublishJob        │
                    │       (信號發布)            │
                    └─────────────────────────────┘


上游模組相依:
────────────────────────────────────────────────────────────────────────────
M07 技術分析 Job ──┐
M08 基本面 Job ────┤
M09 籌碼分析 Job ──┼──▶ SignalCollectionJob 需在上游 Job 完成後執行
M10 型態識別 Job ──┤
M11 量化策略 Job ──┤
M12 總經產業 Job ──┘

典型排程:
────────────────────────────────────────────────────────────────────────────
15:00 - 16:30  M07-M12 各模組完成當日分析
17:00 - 17:35  M13 執行信號處理 Pipeline
17:35 - ...    M14-M18 消費統一信號
```

---

## 5. 錯誤處理與重試

### 5.1 重試策略

```java
@Configuration
public class M13JobRetryConfig {

    @Bean
    public RetryPolicy signalProcessingRetryPolicy() {
        return RetryPolicy.builder()
            .maxRetries(3)
            .initialInterval(Duration.ofMinutes(1))
            .multiplier(2.0)
            .maxInterval(Duration.ofMinutes(10))
            .retryableExceptions(
                DatabaseException.class,
                UpstreamModuleException.class,
                TimeoutException.class
            )
            .nonRetryableExceptions(
                ValidationException.class,
                ConfigurationException.class
            )
            .build();
    }
}
```

### 5.2 失敗處理

```java
@Component
public class SignalJobFailureHandler implements JobListener {

    @Autowired
    private AlertService alertService;

    @Autowired
    private BatchService batchService;

    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception) {
        if (exception != null) {
            String jobName = context.getJobDetail().getKey().getName();
            String batchId = context.getMergedJobDataMap().getString("batchId");

            // 標記批次失敗
            if (batchId != null) {
                batchService.markFailed(batchId, exception.getMessage());
            }

            // 發送告警
            alertService.sendAlert(AlertLevel.HIGH,
                "M13 Signal Job Failed",
                String.format("Job: %s, Batch: %s, Error: %s",
                    jobName, batchId, exception.getMessage())
            );

            // 記錄失敗日誌
            log.error("Signal job {} failed for batch {}", jobName, batchId, exception);
        }
    }
}
```

---

## 6. 監控指標

### 6.1 Job 執行監控

| 指標 | 類型 | 說明 |
|-----|------|------|
| `m13.job.execution.count` | Counter | Job 執行次數 |
| `m13.job.execution.duration` | Timer | Job 執行時間 |
| `m13.job.failure.count` | Counter | Job 失敗次數 |
| `m13.batch.completion.rate` | Gauge | 批次完成率 |

### 6.2 處理量監控

| 指標 | 類型 | 說明 |
|-----|------|------|
| `m13.signals.collected` | Counter | 收集信號數 |
| `m13.signals.deduplicated` | Counter | 去重後信號數 |
| `m13.signals.unified` | Counter | 統一信號數 |
| `m13.dedup.rate` | Gauge | 去重率 |
| `m13.scoring.avg` | Gauge | 平均評分 |

### 6.3 SLA 監控

| 指標 | 目標值 | 告警閾值 |
|-----|-------|---------|
| 每日處理完成時間 | 17:35 前 | 17:45 未完成 |
| 收集成功率 | > 99% | < 95% |
| 評分完成率 | 100% | < 98% |
| 發布延遲 | < 5 分鐘 | > 10 分鐘 |

---

## 7. 手動執行指南

### 7.1 手動觸發 Job

```bash
# 手動觸發完整處理流程
POST /api/m13/admin/jobs/trigger/daily-processing
{
  "trade_date": "2024-12-24",
  "force": false
}

# 手動觸發單一階段
POST /api/m13/admin/jobs/trigger/collection
{
  "trade_date": "2024-12-24",
  "batch_id": "BATCH_20241224_001",
  "source_modules": ["M07", "M08"]
}

# 重新執行失敗批次
POST /api/m13/admin/jobs/retry/{batchId}
{
  "from_phase": "SCORING"
}
```

### 7.2 批次狀態查詢

```bash
# 查詢批次狀態
GET /api/m13/admin/batches/{batchId}

# 查詢今日批次列表
GET /api/m13/admin/batches?date=2024-12-24
```

---

## 8. 相關文檔

- [M13 功能需求](../specs/functional/M13-信號引擎功能需求.md)
- [M13 業務流程](../design/M13-業務流程.md)
- [M13 效能考量](../design/M13-效能考量.md)

---

**文件維護者**: DevOps 工程師
**最後更新**: 2026-01-14
**下次審核**: 2026-04-14
