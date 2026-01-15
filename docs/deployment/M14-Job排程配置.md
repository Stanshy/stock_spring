# M14-選股引擎 Job 排程配置

> **文件編號**: JOB-M14
> **模組名稱**: 選股引擎 (Stock Screening Engine)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. 排程總覽

### 1.1 Job 清單

| # | Job 名稱 | 排程時間 | 說明 | 預估耗時 |
|---|---------|---------|------|---------|
| 1 | PerformanceTrackingJob | 每日 18:30 | 更新選股績效 | 30-60 分鐘 |
| 2 | HistoryCleanupJob | 每日 02:00 | 清理過期歷史 | 10-20 分鐘 |
| 3 | StrategyStatsJob | 每日 06:00 | 更新策略統計 | 5-10 分鐘 |
| 4 | PopularTemplatesJob | 每週一 07:00 | 更新熱門模板 | 2-5 分鐘 |

### 1.2 執行時序圖

```
┌─────────────────────────────────────────────────────────────────────┐
│                    M14 Job 每日執行時序                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  時間軸 (24 小時制)                                                  │
│  ├────┼────┼────┼────┼────┼────┼────┼────┼────┼────┼────┼────┤     │
│  00   02   04   06   08   10   12   14   16   18   20   22   24     │
│                                                                      │
│       ▼                                                              │
│  02:00 HistoryCleanupJob                                            │
│       └─────┐ (10-20 min)                                           │
│             │                                                        │
│             ▼                                                        │
│  06:00      StrategyStatsJob                                        │
│             └─────┐ (5-10 min)                                      │
│                   │                                                  │
│                   │         ┌─── M06 日資料同步完成 (約 17:00)       │
│                   │         │                                        │
│                   ▼         ▼                                        │
│  18:30                      PerformanceTrackingJob                  │
│                             └─────────────────┐ (30-60 min)         │
│                                               │                      │
│                                               ▼                      │
│  19:30                                        完成                   │
│                                                                      │
│  每週一 07:00: PopularTemplatesJob                                  │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Job 詳細配置

### 2.1 PerformanceTrackingJob（績效追蹤）

```java
@Component
@Slf4j
public class PerformanceTrackingJob implements Job {

    @Autowired
    private ScreeningPerformanceMapper performanceMapper;

    @Autowired
    private StockDailyMapper stockDailyMapper;

    private static final int BATCH_SIZE = 1000;
    private static final int MAX_TRACKING_DAYS = 20;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("開始執行績效追蹤 Job");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            LocalDate today = LocalDate.now();
            int totalUpdated = 0;

            // 處理待追蹤記錄
            while (true) {
                List<ScreeningPerformance> pending =
                    performanceMapper.findPendingPerformanceTracking(BATCH_SIZE);

                if (pending.isEmpty()) {
                    break;
                }

                for (ScreeningPerformance perf : pending) {
                    updatePerformance(perf, today);
                    totalUpdated++;
                }

                log.info("已處理 {} 筆績效追蹤記錄", totalUpdated);
            }

            stopWatch.stop();
            log.info("績效追蹤 Job 完成，共更新 {} 筆，耗時 {} 秒",
                totalUpdated, stopWatch.getTotalTimeSeconds());

        } catch (Exception e) {
            log.error("績效追蹤 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }

    private void updatePerformance(ScreeningPerformance perf, LocalDate today) {
        int daysSinceExecution = (int) ChronoUnit.DAYS.between(
            perf.getTradeDate(), today);

        StockDaily priceData = stockDailyMapper.findByStockIdAndDate(
            perf.getStockId(), today);

        if (priceData == null) {
            return; // 無交易資料（假日）
        }

        BigDecimal currentPrice = priceData.getClosePrice();
        BigDecimal basePrice = perf.getPriceAtExecution();
        BigDecimal returnPct = calculateReturn(basePrice, currentPrice);

        // 更新對應天數的績效
        if (daysSinceExecution >= 1 && perf.getReturn1d() == null) {
            performanceMapper.update1DayPerformance(
                perf.getExecutionId(), perf.getStockId(),
                currentPrice, returnPct);
        }

        if (daysSinceExecution >= 5 && perf.getReturn5d() == null) {
            performanceMapper.update5DayPerformance(
                perf.getExecutionId(), perf.getStockId(),
                currentPrice, returnPct);
        }

        if (daysSinceExecution >= 10 && perf.getReturn10d() == null) {
            performanceMapper.update10DayPerformance(
                perf.getExecutionId(), perf.getStockId(),
                currentPrice, returnPct);
        }

        if (daysSinceExecution >= 20 && perf.getReturn20d() == null) {
            performanceMapper.update20DayPerformance(
                perf.getExecutionId(), perf.getStockId(),
                currentPrice, returnPct);
        }

        // 更新最高/最低價
        updateMaxMinPrice(perf, currentPrice, returnPct);
    }

    private BigDecimal calculateReturn(BigDecimal base, BigDecimal current) {
        return current.subtract(base)
            .divide(base, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
screening:
  jobs:
    performance-tracking:
      cron: "0 30 18 * * ?"  # 每日 18:30
      enabled: true
      batch-size: 1000
      max-tracking-days: 20
```

---

### 2.2 HistoryCleanupJob（歷史清理）

```java
@Component
@Slf4j
public class HistoryCleanupJob implements Job {

    @Autowired
    private ScreeningResultMapper resultMapper;

    @Autowired
    private ScreeningPerformanceMapper performanceMapper;

    @Autowired
    private ScreeningExecutionMapper executionMapper;

    @Value("${screening.cleanup.result-retention-days:90}")
    private int resultRetentionDays;

    @Value("${screening.cleanup.execution-retention-days:365}")
    private int executionRetentionDays;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("開始執行歷史清理 Job");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            LocalDate resultCutoff = LocalDate.now().minusDays(resultRetentionDays);
            LocalDate executionCutoff = LocalDate.now().minusDays(executionRetentionDays);

            // 1. 清理選股結果 (90 天)
            int deletedResults = resultMapper.deleteBeforeDate(resultCutoff);
            log.info("已刪除 {} 筆過期選股結果", deletedResults);

            // 2. 清理績效記錄 (90 天)
            int deletedPerformance = performanceMapper.deleteBeforeDate(resultCutoff);
            log.info("已刪除 {} 筆過期績效記錄", deletedPerformance);

            // 3. 歸檔執行記錄 (1 年)
            int archivedExecutions = archiveOldExecutions(executionCutoff);
            log.info("已歸檔 {} 筆過期執行記錄", archivedExecutions);

            stopWatch.stop();
            log.info("歷史清理 Job 完成，耗時 {} 秒", stopWatch.getTotalTimeSeconds());

        } catch (Exception e) {
            log.error("歷史清理 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }

    private int archiveOldExecutions(LocalDate cutoff) {
        // 先複製到歸檔表
        int archived = executionMapper.archiveBeforeDate(cutoff);

        // 再刪除原表
        if (archived > 0) {
            executionMapper.deleteBeforeDate(cutoff);
        }

        return archived;
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
screening:
  jobs:
    history-cleanup:
      cron: "0 0 2 * * ?"  # 每日 02:00
      enabled: true
  cleanup:
    result-retention-days: 90
    execution-retention-days: 365
```

---

### 2.3 StrategyStatsJob（策略統計）

```java
@Component
@Slf4j
public class StrategyStatsJob implements Job {

    @Autowired
    private ScreeningStrategyMapper strategyMapper;

    @Autowired
    private ScreeningPerformanceMapper performanceMapper;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("開始執行策略統計 Job");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 取得所有活躍策略
            List<ScreeningStrategy> activeStrategies =
                strategyMapper.findAllActiveStrategies();

            int updated = 0;
            for (ScreeningStrategy strategy : activeStrategies) {
                // 計算策略績效統計
                StrategyPerformanceSummary summary =
                    performanceMapper.getStrategyPerformanceSummary(
                        strategy.getStrategyId(),
                        LocalDate.now().minusDays(30),
                        LocalDate.now()
                    );

                if (summary != null) {
                    // 更新策略的績效摘要
                    strategyMapper.updatePerformanceStats(
                        strategy.getStrategyId(),
                        summary.getAvgReturn5d(),
                        summary.getWinRate5d(),
                        summary.getTotalExecutions()
                    );
                    updated++;
                }
            }

            stopWatch.stop();
            log.info("策略統計 Job 完成，更新 {} 個策略，耗時 {} 秒",
                updated, stopWatch.getTotalTimeSeconds());

        } catch (Exception e) {
            log.error("策略統計 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
screening:
  jobs:
    strategy-stats:
      cron: "0 0 6 * * ?"  # 每日 06:00
      enabled: true
```

---

### 2.4 PopularTemplatesJob（熱門模板）

```java
@Component
@Slf4j
public class PopularTemplatesJob implements Job {

    @Autowired
    private ScreeningTemplateMapper templateMapper;

    @Autowired
    private ScreeningExecutionMapper executionMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String POPULAR_TEMPLATES_KEY = "screening:popular:templates";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("開始執行熱門模板統計 Job");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 統計過去 7 天各模板的使用次數
            LocalDate startDate = LocalDate.now().minusDays(7);
            List<TemplateUsageStats> usageStats =
                executionMapper.getTemplateUsageStats(startDate, LocalDate.now());

            // 更新模板排序
            for (int i = 0; i < usageStats.size(); i++) {
                TemplateUsageStats stats = usageStats.get(i);
                templateMapper.updateDisplayOrder(
                    stats.getTemplateCode(),
                    i + 1  // 按使用次數排序
                );
            }

            // 快取熱門模板列表
            redisTemplate.opsForValue().set(
                POPULAR_TEMPLATES_KEY,
                usageStats.stream()
                    .limit(10)
                    .map(TemplateUsageStats::getTemplateCode)
                    .collect(Collectors.toList()),
                Duration.ofDays(7)
            );

            stopWatch.stop();
            log.info("熱門模板統計 Job 完成，耗時 {} 秒",
                stopWatch.getTotalTimeSeconds());

        } catch (Exception e) {
            log.error("熱門模板統計 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
screening:
  jobs:
    popular-templates:
      cron: "0 0 7 ? * MON"  # 每週一 07:00
      enabled: true
```

---

## 3. Quartz 整合配置

### 3.1 Job 註冊

```java
@Configuration
public class M14JobConfig {

    @Bean
    public JobDetail performanceTrackingJobDetail() {
        return JobBuilder.newJob(PerformanceTrackingJob.class)
            .withIdentity("performanceTrackingJob", "m14")
            .withDescription("選股績效追蹤")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger performanceTrackingTrigger(
            @Qualifier("performanceTrackingJobDetail") JobDetail jobDetail,
            @Value("${screening.jobs.performance-tracking.cron}") String cron) {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("performanceTrackingTrigger", "m14")
            .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                .withMisfireHandlingInstructionFireAndProceed())
            .build();
    }

    @Bean
    public JobDetail historyCleanupJobDetail() {
        return JobBuilder.newJob(HistoryCleanupJob.class)
            .withIdentity("historyCleanupJob", "m14")
            .withDescription("選股歷史清理")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger historyCleanupTrigger(
            @Qualifier("historyCleanupJobDetail") JobDetail jobDetail,
            @Value("${screening.jobs.history-cleanup.cron}") String cron) {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("historyCleanupTrigger", "m14")
            .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                .withMisfireHandlingInstructionFireAndProceed())
            .build();
    }

    @Bean
    public JobDetail strategyStatsJobDetail() {
        return JobBuilder.newJob(StrategyStatsJob.class)
            .withIdentity("strategyStatsJob", "m14")
            .withDescription("策略績效統計")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger strategyStatsTrigger(
            @Qualifier("strategyStatsJobDetail") JobDetail jobDetail,
            @Value("${screening.jobs.strategy-stats.cron}") String cron) {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("strategyStatsTrigger", "m14")
            .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                .withMisfireHandlingInstructionDoNothing())
            .build();
    }

    @Bean
    public JobDetail popularTemplatesJobDetail() {
        return JobBuilder.newJob(PopularTemplatesJob.class)
            .withIdentity("popularTemplatesJob", "m14")
            .withDescription("熱門模板統計")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger popularTemplatesTrigger(
            @Qualifier("popularTemplatesJobDetail") JobDetail jobDetail,
            @Value("${screening.jobs.popular-templates.cron}") String cron) {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("popularTemplatesTrigger", "m14")
            .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                .withMisfireHandlingInstructionDoNothing())
            .build();
    }
}
```

---

## 4. Job 依賴關係

```
┌─────────────────────────────────────────────────────────────────────┐
│                       M14 Job 依賴關係                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│                      ┌─────────────────────┐                        │
│                      │  M06 日資料同步     │                        │
│                      │  (約 17:00 完成)    │                        │
│                      └──────────┬──────────┘                        │
│                                 │                                    │
│                                 │ 依賴                               │
│                                 ▼                                    │
│                      ┌─────────────────────┐                        │
│                      │ PerformanceTracking │                        │
│                      │ Job (18:30)         │                        │
│                      │ ◄── 需要當日收盤價  │                        │
│                      └──────────┬──────────┘                        │
│                                 │                                    │
│                                 │ 資料產生                           │
│                                 ▼                                    │
│                      ┌─────────────────────┐                        │
│                      │  StrategyStatsJob   │                        │
│                      │  (隔日 06:00)        │                        │
│                      │  ◄── 需要績效數據   │                        │
│                      └─────────────────────┘                        │
│                                                                      │
│  獨立執行:                                                           │
│  ┌─────────────────────┐    ┌─────────────────────┐                 │
│  │ HistoryCleanupJob   │    │ PopularTemplatesJob │                 │
│  │ (02:00)             │    │ (週一 07:00)         │                 │
│  │ ◄── 無外部依賴      │    │ ◄── 無外部依賴      │                 │
│  └─────────────────────┘    └─────────────────────┘                 │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 5. 監控與告警

### 5.1 監控指標

| 指標名稱 | 說明 | 告警閾值 |
|---------|------|---------|
| m14_job_duration_seconds | Job 執行耗時 | > 預估耗時 2 倍 |
| m14_job_success_total | Job 成功次數 | N/A |
| m14_job_failure_total | Job 失敗次數 | > 0 |
| m14_performance_pending_count | 待追蹤績效數 | > 100,000 |
| m14_cleanup_deleted_count | 清理記錄數 | N/A |

### 5.2 告警配置

```yaml
# prometheus-rules.yml
groups:
  - name: m14-job-alerts
    rules:
      - alert: M14PerformanceTrackingJobFailed
        expr: increase(m14_job_failure_total{job_name="performanceTrackingJob"}[1h]) > 0
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "M14 績效追蹤 Job 執行失敗"
          description: "績效追蹤 Job 在過去 1 小時內執行失敗"

      - alert: M14PerformanceTrackingJobSlow
        expr: m14_job_duration_seconds{job_name="performanceTrackingJob"} > 7200
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "M14 績效追蹤 Job 執行過慢"
          description: "績效追蹤 Job 執行時間超過 2 小時"

      - alert: M14PerformancePendingHigh
        expr: m14_performance_pending_count > 100000
        for: 30m
        labels:
          severity: warning
        annotations:
          summary: "M14 待追蹤績效記錄過多"
          description: "待追蹤績效記錄超過 10 萬筆"
```

---

## 6. 運維操作

### 6.1 手動觸發

```bash
# 透過 Actuator 端點手動觸發
curl -X POST http://localhost:8080/actuator/quartz/jobs/m14/performanceTrackingJob/trigger

# 查看 Job 狀態
curl http://localhost:8080/actuator/quartz/jobs/m14
```

### 6.2 暫停與恢復

```bash
# 暫停 Job
curl -X POST http://localhost:8080/actuator/quartz/triggers/m14/performanceTrackingTrigger/pause

# 恢復 Job
curl -X POST http://localhost:8080/actuator/quartz/triggers/m14/performanceTrackingTrigger/resume
```

---

## 7. 相關文檔

- [M14 功能需求](../specs/functional/M14-選股引擎功能需求.md)
- [M14 業務流程](../design/M14-業務流程.md)
- [M14 效能考量](../design/M14-效能考量.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
