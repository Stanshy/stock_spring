# M16-回測系統 Job 排程配置

> **文件編號**: JOB-M16
> **模組名稱**: 回測系統 (Backtesting System)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. Job 清單總覽

| Job 名稱 | 執行頻率 | 說明 | 優先級 |
|---------|---------|------|:------:|
| BacktestCleanupJob | 每日 02:00 | 清理過期回測資料 | P1 |
| StuckBacktestRecoveryJob | 每 30 分鐘 | 處理卡住的回測任務 | P1 |
| OptimizationCleanupJob | 每日 02:30 | 清理過期最佳化結果 | P2 |
| BacktestStatisticsJob | 每日 06:00 | 統計回測使用情況 | P2 |

---

## 2. Job 詳細規格

### 2.1 BacktestCleanupJob

**功能說明**：清理過期的回測任務及相關資料，維護資料庫空間。

**執行時機**
```
Cron: 0 0 2 * * ?
說明: 每日凌晨 02:00 執行
```

**處理邏輯**

```
┌─────────────────────────────────────────────────────────────────┐
│                   BacktestCleanupJob 流程                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                    清理策略                              │    │
│  │                                                          │    │
│  │  保留期限：                                              │    │
│  │  • COMPLETED/FAILED/CANCELLED 狀態: 90 天               │    │
│  │  • CREATED 但未執行: 7 天                                │    │
│  │  • 回測範本: 永久保留                                    │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 1: 查詢過期回測                                     │    │
│  │                                                          │    │
│  │  SELECT backtest_id FROM backtest_tasks                  │    │
│  │  WHERE (status IN ('COMPLETED','FAILED','CANCELLED')     │    │
│  │         AND created_at < NOW() - INTERVAL '90 days')     │    │
│  │     OR (status = 'CREATED'                               │    │
│  │         AND created_at < NOW() - INTERVAL '7 days')      │    │
│  │                                                          │    │
│  └────────────────────────┬────────────────────────────────┘    │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 2: 批次刪除 (CASCADE)                               │    │
│  │                                                          │    │
│  │  for each batch of 100 backtest_ids:                    │    │
│  │    DELETE FROM backtest_tasks                           │    │
│  │    WHERE backtest_id IN (batch)                         │    │
│  │    -- CASCADE 自動刪除:                                  │    │
│  │    --   backtest_trades                                 │    │
│  │    --   backtest_daily_snapshots                        │    │
│  │    --   backtest_results                                │    │
│  │    --   backtest_drawdowns                              │    │
│  │    --   backtest_optimizations (及其 results)           │    │
│  │                                                          │    │
│  └────────────────────────┬────────────────────────────────┘    │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 3: 記錄清理結果                                     │    │
│  │                                                          │    │
│  │  • 刪除的回測數量                                        │    │
│  │  • 釋放的資料量                                          │    │
│  │  • 執行時間                                              │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**配置參數**

| 參數 | 預設值 | 說明 |
|-----|-------|------|
| retention.completed.days | 90 | 完成回測保留天數 |
| retention.created.days | 7 | 未執行回測保留天數 |
| batch.size | 100 | 每批刪除數量 |
| enabled | true | 是否啟用 |

**Quartz 配置**

```yaml
backtest:
  cleanup:
    cron: "0 0 2 * * ?"
    retention:
      completed-days: 90
      created-days: 7
    batch-size: 100
    enabled: true
```

---

### 2.2 StuckBacktestRecoveryJob

**功能說明**：檢測並處理卡住的回測任務（執行中但長時間無進度更新）。

**執行時機**
```
Cron: 0 */30 * * * ?
說明: 每 30 分鐘執行一次
```

**處理邏輯**

```
┌─────────────────────────────────────────────────────────────────┐
│                 StuckBacktestRecoveryJob 流程                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 1: 查詢卡住的回測                                   │    │
│  │                                                          │    │
│  │  定義「卡住」：                                          │    │
│  │  • status = 'RUNNING'                                    │    │
│  │  • updated_at < NOW() - INTERVAL '30 minutes'           │    │
│  │                                                          │    │
│  │  SELECT backtest_id, started_at, progress_percent       │    │
│  │  FROM backtest_tasks                                    │    │
│  │  WHERE status = 'RUNNING'                               │    │
│  │    AND updated_at < NOW() - INTERVAL '30 minutes'       │    │
│  │                                                          │    │
│  └────────────────────────┬────────────────────────────────┘    │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 2: 評估處理方式                                     │    │
│  │                                                          │    │
│  │  for each stuck backtest:                               │    │
│  │                                                          │    │
│  │    if progress_percent < 10%:                           │    │
│  │      # 剛開始就卡住，可能是資料問題                      │    │
│  │      → 標記為 FAILED                                    │    │
│  │      → error_message = "執行逾時，請檢查資料完整性"     │    │
│  │                                                          │    │
│  │    elif progress_percent >= 90%:                        │    │
│  │      # 快完成了，可能是結算階段問題                      │    │
│  │      → 嘗試重新執行結算                                 │    │
│  │      → 若失敗則標記為 FAILED                            │    │
│  │                                                          │    │
│  │    else:                                                 │    │
│  │      # 中途卡住                                          │    │
│  │      → 標記為 FAILED                                    │    │
│  │      → error_message = "執行中斷，進度: X%"             │    │
│  │                                                          │    │
│  └────────────────────────┬────────────────────────────────┘    │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 3: 發送通知                                         │    │
│  │                                                          │    │
│  │  • 記錄到 application log                               │    │
│  │  • 發送告警給系統管理員（若頻繁發生）                    │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**配置參數**

| 參數 | 預設值 | 說明 |
|-----|-------|------|
| timeout.minutes | 30 | 判定卡住的閒置時間 |
| max.execution.hours | 2 | 最大執行時間（超過強制停止） |
| enabled | true | 是否啟用 |

**Quartz 配置**

```yaml
backtest:
  stuck-recovery:
    cron: "0 */30 * * * ?"
    timeout-minutes: 30
    max-execution-hours: 2
    enabled: true
```

---

### 2.3 OptimizationCleanupJob

**功能說明**：清理過期的參數最佳化任務及結果。

**執行時機**
```
Cron: 0 30 2 * * ?
說明: 每日凌晨 02:30 執行
```

**處理邏輯**

```
┌─────────────────────────────────────────────────────────────────┐
│                 OptimizationCleanupJob 流程                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 1: 查詢過期最佳化任務                               │    │
│  │                                                          │    │
│  │  SELECT optimization_id FROM backtest_optimizations     │    │
│  │  WHERE created_at < NOW() - INTERVAL '30 days'          │    │
│  │                                                          │    │
│  └────────────────────────┬────────────────────────────────┘    │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 2: 刪除相關資料                                     │    │
│  │                                                          │    │
│  │  DELETE FROM backtest_optimization_results              │    │
│  │  WHERE optimization_id IN (expired_ids)                 │    │
│  │                                                          │    │
│  │  DELETE FROM backtest_optimizations                     │    │
│  │  WHERE optimization_id IN (expired_ids)                 │    │
│  │                                                          │    │
│  └────────────────────────┬────────────────────────────────┘    │
│                           │                                     │
│                           ▼                                     │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Step 3: 清理孤兒最佳化（回測已刪除但最佳化還在）         │    │
│  │                                                          │    │
│  │  DELETE FROM backtest_optimizations                     │    │
│  │  WHERE backtest_id NOT IN                               │    │
│  │    (SELECT backtest_id FROM backtest_tasks)             │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**配置參數**

| 參數 | 預設值 | 說明 |
|-----|-------|------|
| retention.days | 30 | 最佳化結果保留天數 |
| enabled | true | 是否啟用 |

---

### 2.4 BacktestStatisticsJob

**功能說明**：統計回測系統使用情況，供監控與分析。

**執行時機**
```
Cron: 0 0 6 * * ?
說明: 每日早上 06:00 執行
```

**統計項目**

```
┌─────────────────────────────────────────────────────────────────┐
│                 BacktestStatisticsJob 統計項目                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  每日統計：                                                       │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ 1. 回測執行統計                                          │    │
│  │    • 總執行次數                                          │    │
│  │    • 成功率 (COMPLETED / total)                         │    │
│  │    • 平均執行時間                                        │    │
│  │    • 失敗原因分布                                        │    │
│  │                                                          │    │
│  │ 2. 資源使用統計                                          │    │
│  │    • 平均回測期間長度                                    │    │
│  │    • 平均股票數量                                        │    │
│  │    • 總交易記錄數                                        │    │
│  │    • 資料庫空間使用                                      │    │
│  │                                                          │    │
│  │ 3. 用戶統計                                              │    │
│  │    • 活躍用戶數                                          │    │
│  │    • 每用戶平均回測數                                    │    │
│  │    • Top 用戶排名                                        │    │
│  │                                                          │    │
│  │ 4. 最佳化統計                                            │    │
│  │    • 最佳化執行次數                                      │    │
│  │    • 平均參數組合數                                      │    │
│  │    • 平均執行時間                                        │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  輸出：寫入監控指標 / 發送報告                                   │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Job 執行順序與依賴

```
┌─────────────────────────────────────────────────────────────────┐
│                    每日 Job 執行時序                             │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  時間軸 (每日)                                                   │
│  ═══════════════════════════════════════════════════════════    │
│                                                                  │
│  00:00                                                           │
│    │                                                             │
│  02:00 ─── BacktestCleanupJob ───────────────────────────       │
│    │         ↓                                                   │
│  02:30 ─── OptimizationCleanupJob ───────────────────────       │
│    │         (依賴 BacktestCleanupJob 完成)                      │
│    │                                                             │
│  06:00 ─── BacktestStatisticsJob ────────────────────────       │
│    │                                                             │
│    │       StuckBacktestRecoveryJob (每 30 分鐘)                 │
│    │       ┌───┬───┬───┬───┬───┬───┬───┬───┬───┬───┐           │
│    │       00  30  00  30  00  30  00  30  00  30  ...          │
│    │                                                             │
│  24:00                                                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. 異步回測執行機制

回測執行採用異步模式，非 Quartz 排程：

```
┌─────────────────────────────────────────────────────────────────┐
│                    異步回測執行機制                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   執行模式                               │    │
│  │                                                          │    │
│  │  用戶觸發:                                               │    │
│  │    POST /api/v1/backtests/{id}/execute                  │    │
│  │                                                          │    │
│  │  異步執行:                                               │    │
│  │    @Async("backtestExecutor")                           │    │
│  │    public void executeBacktest(String backtestId)       │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   執行緒池配置                           │    │
│  │                                                          │    │
│  │  ThreadPoolTaskExecutor:                                │    │
│  │    corePoolSize: 4                                      │    │
│  │    maxPoolSize: 8                                       │    │
│  │    queueCapacity: 50                                    │    │
│  │    threadNamePrefix: "backtest-"                        │    │
│  │    rejectedHandler: CallerRunsPolicy                    │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                   並行限制                               │    │
│  │                                                          │    │
│  │  • 每用戶同時執行上限: 2 個回測                         │    │
│  │  • 系統總並行上限: 8 個回測                             │    │
│  │  • 超過上限時: 排隊等待                                 │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**執行緒池 Java 配置**

```java
@Configuration
@EnableAsync
public class BacktestAsyncConfig {

    @Bean("backtestExecutor")
    public TaskExecutor backtestExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("backtest-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
```

---

## 5. 監控與告警

### 5.1 監控指標

| 指標名稱 | 類型 | 說明 | 告警閾值 |
|---------|------|------|---------|
| backtest.queue.size | Gauge | 排隊中的回測數 | > 30 |
| backtest.running.count | Gauge | 執行中的回測數 | > 8 |
| backtest.execution.time | Timer | 回測執行時間 | P95 > 5min |
| backtest.failure.rate | Counter | 失敗率 | > 10% |
| backtest.stuck.count | Counter | 卡住的回測數 | > 0 |

### 5.2 告警規則

```yaml
alerts:
  - name: BacktestQueueOverload
    condition: backtest.queue.size > 30
    severity: WARNING
    message: "回測排隊數量過多: ${value}"

  - name: BacktestExecutionSlow
    condition: backtest.execution.time.p95 > 300000
    severity: WARNING
    message: "回測執行時間過長: ${value}ms"

  - name: BacktestStuck
    condition: backtest.stuck.count > 0
    severity: ERROR
    message: "有 ${value} 個回測卡住"

  - name: BacktestHighFailureRate
    condition: backtest.failure.rate > 0.1
    severity: ERROR
    message: "回測失敗率過高: ${value}"
```

---

## 6. 相關文檔

- [M16 功能需求](../specs/functional/M16-回測系統功能需求.md)
- [M16 業務流程](../design/M16-業務流程.md)
- [M16 效能考量](../design/M16-效能考量.md)

---

**文件維護者**: DevOps 工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
