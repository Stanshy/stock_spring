# M17-風險管理模組 Job 排程配置

> **文件編號**: JOB-M17
> **模組名稱**: 風險管理模組 (Risk Management Module)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. Job 清單總覽

| Job 名稱 | 執行頻率 | 執行時間 | 優先級 | 說明 |
|---------|---------|---------|:------:|------|
| DailyRiskCalculationJob | 每日 | 09:30 | P0 | 每日風險指標計算 |
| LimitCheckJob | 每日 | 09:45 | P0 | 風險限額檢查 |
| AlertProcessingJob | 每 15 分鐘 | */15 * * * * | P0 | 預警處理與通知 |
| CorrelationCacheJob | 每日 | 10:00 | P1 | 相關性矩陣快取更新 |
| RiskDataCleanupJob | 每日 | 03:00 | P1 | 過期資料清理 |
| AlertAutoResolveJob | 每小時 | 0 * * * * | P2 | 自動解決已恢復預警 |
| WeeklyStressTestJob | 每週六 | 02:00 | P2 | 週度壓力測試 |

---

## 2. P0 核心 Job

### 2.1 DailyRiskCalculationJob

每日計算所有投資組合的風險指標。

#### 2.1.1 基本配置

```yaml
job:
  name: DailyRiskCalculationJob
  group: M17_RISK
  description: 每日風險指標計算

schedule:
  cron: "0 30 9 * * MON-FRI"  # 週一至週五 09:30
  timezone: Asia/Taipei
  misfire_policy: FIRE_ONCE_NOW

execution:
  max_concurrent: 1
  timeout_minutes: 30
  retry:
    max_attempts: 3
    delay_seconds: 300
    backoff_multiplier: 2
```

#### 2.1.2 執行流程

```
┌─────────────────────────────────────────────────────────────────┐
│               DailyRiskCalculationJob 執行流程                   │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────┐                                                    │
│  │  開始   │ 09:30                                              │
│  └────┬────┘                                                    │
│       │                                                          │
│       ▼                                                          │
│  ┌─────────────────────┐                                        │
│  │ 1. 檢查前置條件      │                                        │
│  │    - M06 資料已更新  │                                        │
│  │    - 今日為交易日    │                                        │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│             ▼                                                    │
│  ┌─────────────────────┐                                        │
│  │ 2. 取得所有活躍投組  │                                        │
│  │    (M18_portfolios) │                                        │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│             ▼                                                    │
│  ┌────────────────────────────────────────┐                     │
│  │ 3. 平行計算各投組風險 (ExecutorService) │                     │
│  │    ┌────────────────────────────────┐  │                     │
│  │    │ FOR EACH portfolio:            │  │                     │
│  │    │   - 載入持倉資料               │  │                     │
│  │    │   - 取得歷史價格 (252 日)      │  │                     │
│  │    │   - 計算 VaR (三種方法)        │  │                     │
│  │    │   - 計算波動度                 │  │                     │
│  │    │   - 計算 Beta                  │  │                     │
│  │    │   - 計算相關性                 │  │                     │
│  │    │   - 計算集中度                 │  │                     │
│  │    │   - 計算風險歸因               │  │                     │
│  │    │   - 評估風險等級               │  │                     │
│  │    │   - 儲存風險快照               │  │                     │
│  │    └────────────────────────────────┘  │                     │
│  └──────────────────────┬─────────────────┘                     │
│                         │                                        │
│                         ▼                                        │
│  ┌─────────────────────┐                                        │
│  │ 4. 記錄執行結果      │                                        │
│  │    - 成功/失敗數量   │                                        │
│  │    - 處理時間        │                                        │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│             ▼                                                    │
│  ┌─────────┐                                                    │
│  │  結束   │                                                    │
│  └─────────┘                                                    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 2.1.3 Java 實現

```java
@Component
@DisallowConcurrentExecution
public class DailyRiskCalculationJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(DailyRiskCalculationJob.class);

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private RiskCalculationService riskCalculationService;

    @Autowired
    private TradingDayService tradingDayService;

    @Autowired
    private JobExecutionLogRepository logRepository;

    @Value("${risk.calculation.thread-pool-size:4}")
    private int threadPoolSize;

    @Value("${risk.calculation.timeout-per-portfolio:300}")
    private int timeoutPerPortfolio;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate today = LocalDate.now();
        String jobId = context.getFireInstanceId();

        log.info("[{}] DailyRiskCalculationJob started for date: {}", jobId, today);
        long startTime = System.currentTimeMillis();

        // 1. 前置條件檢查
        if (!tradingDayService.isTradingDay(today)) {
            log.info("[{}] Not a trading day, skipping", jobId);
            return;
        }

        // 2. 取得所有活躍投組
        List<Portfolio> portfolios = portfolioRepository.findAllActive();
        log.info("[{}] Found {} active portfolios", jobId, portfolios.size());

        // 3. 平行計算
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<CompletableFuture<RiskCalculationResult>> futures = new ArrayList<>();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (Portfolio portfolio : portfolios) {
            CompletableFuture<RiskCalculationResult> future = CompletableFuture
                .supplyAsync(() -> {
                    try {
                        return riskCalculationService.calculateDailyRisk(
                            portfolio.getPortfolioId(), today);
                    } catch (Exception e) {
                        log.error("[{}] Failed to calculate risk for portfolio: {}",
                            jobId, portfolio.getPortfolioId(), e);
                        return RiskCalculationResult.failure(portfolio.getPortfolioId(), e);
                    }
                }, executor)
                .orTimeout(timeoutPerPortfolio, TimeUnit.SECONDS)
                .whenComplete((result, ex) -> {
                    if (ex != null || !result.isSuccess()) {
                        failCount.incrementAndGet();
                    } else {
                        successCount.incrementAndGet();
                    }
                });

            futures.add(future);
        }

        // 等待所有計算完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executor.shutdown();

        // 4. 記錄結果
        long duration = System.currentTimeMillis() - startTime;
        log.info("[{}] DailyRiskCalculationJob completed. Success: {}, Failed: {}, Duration: {}ms",
            jobId, successCount.get(), failCount.get(), duration);

        saveJobExecutionLog(jobId, today, successCount.get(), failCount.get(), duration);

        if (failCount.get() > 0) {
            throw new JobExecutionException(
                String.format("Risk calculation failed for %d portfolios", failCount.get()));
        }
    }

    private void saveJobExecutionLog(String jobId, LocalDate date,
                                      int success, int failed, long duration) {
        JobExecutionLog log = new JobExecutionLog();
        log.setJobName("DailyRiskCalculationJob");
        log.setJobId(jobId);
        log.setExecutionDate(date);
        log.setSuccessCount(success);
        log.setFailedCount(failed);
        log.setDurationMs(duration);
        log.setStatus(failed > 0 ? "PARTIAL_FAILURE" : "SUCCESS");
        logRepository.save(log);
    }
}
```

#### 2.1.4 監控指標

| 指標 | 說明 | 警戒閾值 |
|-----|------|---------|
| job.risk_calculation.duration | 執行時間 | > 20 分鐘 |
| job.risk_calculation.success_rate | 成功率 | < 95% |
| job.risk_calculation.portfolio_count | 處理投組數 | 0 |
| job.risk_calculation.var_calculation_time | VaR 計算時間 | > 60 秒/投組 |

---

### 2.2 LimitCheckJob

每日檢查所有風險限額是否違反。

#### 2.2.1 基本配置

```yaml
job:
  name: LimitCheckJob
  group: M17_RISK
  description: 風險限額檢查

schedule:
  cron: "0 45 9 * * MON-FRI"  # 週一至週五 09:45 (風險計算後 15 分鐘)
  timezone: Asia/Taipei
  misfire_policy: FIRE_ONCE_NOW

execution:
  max_concurrent: 1
  timeout_minutes: 15
  retry:
    max_attempts: 2
    delay_seconds: 60

dependencies:
  - DailyRiskCalculationJob  # 需在風險計算完成後執行
```

#### 2.2.2 執行流程

```
┌─────────────────────────────────────────────────────────────────┐
│                   LimitCheckJob 執行流程                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────┐                                                    │
│  │  開始   │ 09:45                                              │
│  └────┬────┘                                                    │
│       │                                                          │
│       ▼                                                          │
│  ┌─────────────────────┐                                        │
│  │ 1. 取得所有啟用限額  │                                        │
│  │    (risk_limits)    │                                        │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│             ▼                                                    │
│  ┌────────────────────────────────────────┐                     │
│  │ 2. 逐一檢查限額                          │                     │
│  │    FOR EACH limit:                      │                     │
│  │    ┌────────────────────────────────┐  │                     │
│  │    │ a. 取得當日風險快照             │  │                     │
│  │    │ b. 取得對應指標值               │  │                     │
│  │    │ c. 計算使用率                   │  │                     │
│  │    │ d. 判定狀態                     │  │                     │
│  │    │ e. 比較前日變化                 │  │                     │
│  │    └────────────────────────────────┘  │                     │
│  └──────────────────────┬─────────────────┘                     │
│                         │                                        │
│                         ▼                                        │
│  ┌─────────────────────┐                                        │
│  │ 3. 批次更新限額狀態  │                                        │
│  │    (risk_limits)    │                                        │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│             ▼                                                    │
│  ┌─────────────────────┐                                        │
│  │ 4. 儲存檢查記錄      │                                        │
│  │  (limit_checks)     │                                        │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│             ▼                                                    │
│        ┌────────────┐                                           │
│        │ 有異常狀態? │                                           │
│        └─────┬──────┘                                           │
│       Yes    │    No                                            │
│    ┌─────────┤                                                  │
│    ▼         ▼                                                  │
│  ┌──────────────┐  ┌──────────────┐                            │
│  │ 5a. 觸發預警  │  │ 5b. 完成     │                            │
│  │ (risk_alerts)│  └──────┬───────┘                            │
│  └──────┬───────┘         │                                     │
│         └─────────────────┤                                     │
│                           ▼                                     │
│  ┌─────────┐                                                    │
│  │  結束   │                                                    │
│  └─────────┘                                                    │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 2.2.3 Java 實現

```java
@Component
@DisallowConcurrentExecution
public class LimitCheckJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(LimitCheckJob.class);

    @Autowired
    private RiskLimitRepository limitRepository;

    @Autowired
    private RiskSnapshotRepository snapshotRepository;

    @Autowired
    private LimitCheckRepository checkRepository;

    @Autowired
    private AlertService alertService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LocalDate today = LocalDate.now();
        String jobId = context.getFireInstanceId();

        log.info("[{}] LimitCheckJob started for date: {}", jobId, today);

        // 1. 取得所有啟用限額
        List<RiskLimit> limits = limitRepository.findAllActive();
        log.info("[{}] Found {} active limits to check", jobId, limits.size());

        // 2. 逐一檢查
        List<LimitCheckResult> results = new ArrayList<>();
        Map<String, RiskSnapshot> snapshotCache = new HashMap<>();

        for (RiskLimit limit : limits) {
            try {
                // 取得風險快照 (快取)
                RiskSnapshot snapshot = snapshotCache.computeIfAbsent(
                    limit.getPortfolioId(),
                    pid -> snapshotRepository.findLatest(pid, today)
                );

                if (snapshot == null) {
                    log.warn("[{}] No risk snapshot found for portfolio: {}",
                        jobId, limit.getPortfolioId());
                    continue;
                }

                // 取得對應指標值
                BigDecimal currentValue = extractMetricValue(snapshot, limit.getLimitType());
                BigDecimal utilization = currentValue.divide(
                    limit.getLimitValue(), 4, RoundingMode.HALF_UP);

                // 判定狀態
                LimitStatus status = determineStatus(utilization, limit);

                // 取得前日記錄
                LimitCheck previousCheck = checkRepository.findPrevious(
                    limit.getLimitId(), today.minusDays(1));

                LimitCheckResult result = LimitCheckResult.builder()
                    .limitId(limit.getLimitId())
                    .portfolioId(limit.getPortfolioId())
                    .limitType(limit.getLimitType())
                    .limitValue(limit.getLimitValue())
                    .currentValue(currentValue)
                    .utilization(utilization)
                    .status(status)
                    .previousValue(previousCheck != null ? previousCheck.getCurrentValue() : null)
                    .build();

                results.add(result);

                // 更新限額狀態
                limit.setCurrentValue(currentValue);
                limit.setCurrentUtilization(utilization);
                limit.setCurrentStatus(status);
                limit.setLastCheckedAt(Instant.now());

            } catch (Exception e) {
                log.error("[{}] Failed to check limit: {}", jobId, limit.getLimitId(), e);
            }
        }

        // 3. 批次更新
        limitRepository.saveAll(limits);

        // 4. 儲存檢查記錄
        List<LimitCheck> checks = results.stream()
            .map(this::toCheckEntity)
            .collect(Collectors.toList());
        checkRepository.saveAll(checks);

        // 5. 觸發預警
        List<LimitCheckResult> abnormalResults = results.stream()
            .filter(r -> r.getStatus() != LimitStatus.NORMAL)
            .collect(Collectors.toList());

        for (LimitCheckResult result : abnormalResults) {
            alertService.triggerLimitAlert(result);
        }

        log.info("[{}] LimitCheckJob completed. Checked: {}, Abnormal: {}",
            jobId, results.size(), abnormalResults.size());
    }

    private BigDecimal extractMetricValue(RiskSnapshot snapshot, String limitType) {
        return switch (limitType) {
            case "VAR_95" -> snapshot.getVar95Daily();
            case "VAR_99" -> snapshot.getVar99Daily();
            case "VAR_PCT" -> snapshot.getVar95Daily()
                .divide(snapshot.getTotalValue(), 6, RoundingMode.HALF_UP);
            case "VOLATILITY" -> snapshot.getVolatilityAnnualized();
            case "MAX_SINGLE_STOCK" -> snapshot.getLargestPositionPct();
            case "MAX_DRAWDOWN" -> snapshot.getMaxDrawdown();
            case "BETA" -> snapshot.getBeta();
            default -> throw new IllegalArgumentException("Unknown limit type: " + limitType);
        };
    }

    private LimitStatus determineStatus(BigDecimal utilization, RiskLimit limit) {
        if (utilization.compareTo(BigDecimal.ONE) >= 0) {
            return LimitStatus.BREACHED;
        } else if (utilization.compareTo(limit.getCriticalThreshold()) >= 0) {
            return LimitStatus.CRITICAL;
        } else if (utilization.compareTo(limit.getWarningThreshold()) >= 0) {
            return LimitStatus.WARNING;
        } else {
            return LimitStatus.NORMAL;
        }
    }
}
```

---

### 2.3 AlertProcessingJob

定期處理預警通知發送。

#### 2.3.1 基本配置

```yaml
job:
  name: AlertProcessingJob
  group: M17_RISK
  description: 預警處理與通知發送

schedule:
  cron: "0 */15 * * * *"  # 每 15 分鐘執行
  timezone: Asia/Taipei
  misfire_policy: DO_NOTHING

execution:
  max_concurrent: 1
  timeout_minutes: 5
  retry:
    max_attempts: 3
    delay_seconds: 30
```

#### 2.3.2 Java 實現

```java
@Component
@DisallowConcurrentExecution
public class AlertProcessingJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AlertProcessingJob.class);

    @Autowired
    private RiskAlertRepository alertRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String jobId = context.getFireInstanceId();
        log.debug("[{}] AlertProcessingJob started", jobId);

        // 取得待發送通知的預警
        List<RiskAlert> pendingAlerts = alertRepository.findPendingNotifications();

        if (pendingAlerts.isEmpty()) {
            log.debug("[{}] No pending alerts", jobId);
            return;
        }

        log.info("[{}] Processing {} pending alerts", jobId, pendingAlerts.size());

        // 按嚴重等級分組處理
        Map<AlertSeverity, List<RiskAlert>> groupedAlerts = pendingAlerts.stream()
            .collect(Collectors.groupingBy(RiskAlert::getSeverity));

        // CRITICAL 和 HIGH: 立即發送
        sendImmediateAlerts(groupedAlerts.getOrDefault(AlertSeverity.CRITICAL, List.of()));
        sendImmediateAlerts(groupedAlerts.getOrDefault(AlertSeverity.HIGH, List.of()));

        // MEDIUM: 匯總發送
        sendBatchAlerts(groupedAlerts.getOrDefault(AlertSeverity.MEDIUM, List.of()));

        // LOW: 僅記錄，等待日報
        markAsProcessed(groupedAlerts.getOrDefault(AlertSeverity.LOW, List.of()));

        log.info("[{}] AlertProcessingJob completed", jobId);
    }

    private void sendImmediateAlerts(List<RiskAlert> alerts) {
        for (RiskAlert alert : alerts) {
            try {
                // 取得通知管道
                List<String> channels = getNotificationChannels(alert);

                for (String channel : channels) {
                    switch (channel) {
                        case "EMAIL" -> notificationService.sendEmail(alert);
                        case "PUSH" -> notificationService.sendPush(alert);
                        case "SMS" -> notificationService.sendSms(alert);
                    }
                }

                alert.setNotificationSentAt(Instant.now());
                alertRepository.save(alert);

            } catch (Exception e) {
                log.error("Failed to send alert notification: {}", alert.getAlertId(), e);
            }
        }
    }

    private void sendBatchAlerts(List<RiskAlert> alerts) {
        if (alerts.isEmpty()) return;

        // 按投組分組
        Map<String, List<RiskAlert>> byPortfolio = alerts.stream()
            .collect(Collectors.groupingBy(RiskAlert::getPortfolioId));

        for (Map.Entry<String, List<RiskAlert>> entry : byPortfolio.entrySet()) {
            try {
                notificationService.sendBatchEmail(entry.getKey(), entry.getValue());

                for (RiskAlert alert : entry.getValue()) {
                    alert.setNotificationSentAt(Instant.now());
                }
                alertRepository.saveAll(entry.getValue());

            } catch (Exception e) {
                log.error("Failed to send batch alerts for portfolio: {}", entry.getKey(), e);
            }
        }
    }

    private void markAsProcessed(List<RiskAlert> alerts) {
        for (RiskAlert alert : alerts) {
            alert.setNotificationSentAt(Instant.now());
        }
        alertRepository.saveAll(alerts);
    }
}
```

---

## 3. P1 進階 Job

### 3.1 CorrelationCacheJob

更新相關性矩陣快取。

```yaml
job:
  name: CorrelationCacheJob
  group: M17_RISK
  description: 相關性矩陣快取更新

schedule:
  cron: "0 0 10 * * MON-FRI"  # 週一至週五 10:00
  timezone: Asia/Taipei

execution:
  max_concurrent: 1
  timeout_minutes: 30
```

```java
@Component
public class CorrelationCacheJob implements Job {

    @Autowired
    private PortfolioRepository portfolioRepository;

    @Autowired
    private CorrelationService correlationService;

    @Override
    public void execute(JobExecutionContext context) {
        List<Portfolio> portfolios = portfolioRepository.findAllActive();

        for (Portfolio portfolio : portfolios) {
            // 計算不同期間的相關性矩陣
            for (String period : List.of("1M", "3M", "6M", "1Y")) {
                try {
                    correlationService.calculateAndCache(
                        portfolio.getPortfolioId(),
                        period,
                        "PEARSON"
                    );
                } catch (Exception e) {
                    log.error("Failed to calculate correlation for portfolio: {} period: {}",
                        portfolio.getPortfolioId(), period, e);
                }
            }
        }
    }
}
```

---

### 3.2 RiskDataCleanupJob

清理過期的風險資料。

```yaml
job:
  name: RiskDataCleanupJob
  group: M17_RISK
  description: 過期風險資料清理

schedule:
  cron: "0 0 3 * * *"  # 每日 03:00
  timezone: Asia/Taipei

execution:
  max_concurrent: 1
  timeout_minutes: 60

cleanup_policy:
  risk_snapshots:
    keep_daily: 90        # 保留 90 天每日快照
    keep_monthly: 3_years # 保留 3 年月底快照
  risk_var_results:
    keep_days: 365        # 保留 1 年
  risk_limit_checks:
    keep_days: 365        # 保留 1 年
  risk_alerts:
    resolved_keep_days: 730  # 已解決預警保留 2 年
  risk_correlation_cache:
    auto_expire: true     # 使用 expires_at 自動過期
```

```java
@Component
public class RiskDataCleanupJob implements Job {

    @Value("${cleanup.risk-snapshots.keep-daily:90}")
    private int snapshotKeepDays;

    @Value("${cleanup.var-results.keep-days:365}")
    private int varKeepDays;

    @Autowired
    private RiskSnapshotRepository snapshotRepository;

    @Autowired
    private RiskVarResultRepository varRepository;

    @Autowired
    private LimitCheckRepository checkRepository;

    @Autowired
    private RiskAlertRepository alertRepository;

    @Autowired
    private CorrelationCacheRepository cacheRepository;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) {
        LocalDate today = LocalDate.now();
        String jobId = context.getFireInstanceId();

        log.info("[{}] RiskDataCleanupJob started", jobId);

        // 1. 清理風險快照 (保留月底)
        LocalDate snapshotExpireDate = today.minusDays(snapshotKeepDays);
        int snapshotDeleted = snapshotRepository.deleteOldNonMonthEnd(snapshotExpireDate);
        log.info("[{}] Deleted {} old risk snapshots", jobId, snapshotDeleted);

        // 2. 清理 VaR 結果
        LocalDate varExpireDate = today.minusDays(varKeepDays);
        int varDeleted = varRepository.deleteOlderThan(varExpireDate);
        log.info("[{}] Deleted {} old VaR results", jobId, varDeleted);

        // 3. 清理限額檢查記錄
        int checkDeleted = checkRepository.deleteOlderThan(today.minusYears(1));
        log.info("[{}] Deleted {} old limit checks", jobId, checkDeleted);

        // 4. 清理已解決預警
        int alertDeleted = alertRepository.deleteResolvedOlderThan(today.minusYears(2));
        log.info("[{}] Deleted {} old resolved alerts", jobId, alertDeleted);

        // 5. 清理過期快取
        int cacheDeleted = cacheRepository.deleteExpired();
        log.info("[{}] Deleted {} expired correlation caches", jobId, cacheDeleted);

        log.info("[{}] RiskDataCleanupJob completed", jobId);
    }
}
```

---

## 4. P2 次要 Job

### 4.1 AlertAutoResolveJob

自動解決已恢復正常的預警。

```yaml
job:
  name: AlertAutoResolveJob
  group: M17_RISK
  description: 自動解決已恢復預警

schedule:
  cron: "0 0 * * * *"  # 每小時整點
  timezone: Asia/Taipei

execution:
  max_concurrent: 1
  timeout_minutes: 10
```

---

### 4.2 WeeklyStressTestJob

每週自動執行壓力測試。

```yaml
job:
  name: WeeklyStressTestJob
  group: M17_RISK
  description: 週度自動壓力測試

schedule:
  cron: "0 0 2 * * SAT"  # 每週六 02:00
  timezone: Asia/Taipei

execution:
  max_concurrent: 1
  timeout_minutes: 120

config:
  scenarios:
    - MARKET_CRASH_2008
    - COVID_2020
    - RATE_HIKE
    - BLACK_SWAN
  include_historical_worst: true
```

---

## 5. Job 依賴關係

```
┌─────────────────────────────────────────────────────────────────────────┐
│                      M17 Job 依賴關係圖                                  │
├─────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  每日執行序列 (交易日)：                                                  │
│                                                                          │
│  ┌───────────────────┐                                                  │
│  │ M06 資料同步完成   │ 09:00                                            │
│  └─────────┬─────────┘                                                  │
│            │                                                             │
│            ▼                                                             │
│  ┌───────────────────┐                                                  │
│  │ DailyRiskCalcJob  │ 09:30                                            │
│  │ (風險計算)         │                                                  │
│  └─────────┬─────────┘                                                  │
│            │                                                             │
│            ▼                                                             │
│  ┌───────────────────┐                                                  │
│  │ LimitCheckJob     │ 09:45                                            │
│  │ (限額檢查)         │                                                  │
│  └─────────┬─────────┘                                                  │
│            │                                                             │
│            ▼                                                             │
│  ┌───────────────────┐                                                  │
│  │ CorrelationCache  │ 10:00                                            │
│  │ (相關性快取)       │                                                  │
│  └───────────────────┘                                                  │
│                                                                          │
│  定期執行 (獨立)：                                                        │
│                                                                          │
│  ┌───────────────────┐     ┌───────────────────┐                       │
│  │AlertProcessingJob │     │AlertAutoResolveJob│                       │
│  │ (每 15 分鐘)       │     │ (每小時)          │                       │
│  └───────────────────┘     └───────────────────┘                       │
│                                                                          │
│  ┌───────────────────┐     ┌───────────────────┐                       │
│  │RiskDataCleanupJob │     │WeeklyStressTestJob│                       │
│  │ (每日 03:00)      │     │ (每週六 02:00)    │                       │
│  └───────────────────┘     └───────────────────┘                       │
│                                                                          │
└─────────────────────────────────────────────────────────────────────────┘
```

---

## 6. 監控與告警

### 6.1 Job 監控指標

| 指標名稱 | 類型 | 說明 |
|---------|------|------|
| `job.m17.execution_count` | Counter | 執行次數 |
| `job.m17.success_count` | Counter | 成功次數 |
| `job.m17.failure_count` | Counter | 失敗次數 |
| `job.m17.duration_seconds` | Histogram | 執行時間 |
| `job.m17.last_success_time` | Gauge | 最後成功時間 |

### 6.2 告警規則

```yaml
alerts:
  - name: RiskCalculationJobFailed
    condition: job.m17.daily_risk_calculation.failure_count > 0
    severity: critical
    message: "DailyRiskCalculationJob 執行失敗"

  - name: RiskCalculationJobDelayed
    condition: time_since(job.m17.daily_risk_calculation.last_success_time) > 2h
    severity: warning
    message: "DailyRiskCalculationJob 超過 2 小時未執行"

  - name: LimitCheckJobFailed
    condition: job.m17.limit_check.failure_count > 0
    severity: high
    message: "LimitCheckJob 執行失敗，限額狀態可能未更新"
```

---

## 7. 相關文檔

- [M17 功能需求](../specs/functional/M17-風險管理功能需求.md)
- [M17 業務流程](../design/M17-業務流程.md)
- [M17 效能考量](../design/M17-效能考量.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
