# M15-警報通知系統 Job 排程配置

> **文件編號**: JOB-M15
> **模組名稱**: 警報通知系統 (Alert Notification System)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. 排程總覽

### 1.1 Job 清單

| # | Job 名稱 | 排程時間 | 說明 | 預估耗時 |
|---|---------|---------|------|---------|
| 1 | SignalMonitorJob | 每分鐘 | 監控 M13 新信號 | < 10 秒 |
| 2 | BatchNotificationJob | 每 5 分鐘 | 處理批次通知佇列 | < 30 秒 |
| 3 | NotificationRetryJob | 每 5 分鐘 | 重試失敗通知 | < 1 分鐘 |
| 4 | DailyCountResetJob | 每日 00:00 | 重置每日計數 | < 10 秒 |
| 5 | HistoryCleanupJob | 每日 03:00 | 清理過期歷史 | 5-10 分鐘 |
| 6 | InactiveDeviceCleanupJob | 每週日 04:00 | 清理非活躍裝置 | 1-2 分鐘 |

### 1.2 執行時序圖

```
┌─────────────────────────────────────────────────────────────────────┐
│                    M15 Job 每日執行時序                              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  時間軸 (24 小時制)                                                  │
│  ├────┼────┼────┼────┼────┼────┼────┼────┼────┼────┼────┼────┤     │
│  00   02   04   06   08   10   12   14   16   18   20   22   24     │
│                                                                      │
│  ▼                                                                   │
│  00:00 DailyCountResetJob (每日計數重置)                            │
│                                                                      │
│       ▼                                                              │
│  03:00 HistoryCleanupJob (歷史清理)                                 │
│                                                                      │
│  ────────────────────────────────────────────────────────────       │
│  │ 以下為持續執行的高頻率 Job                                  │       │
│  ────────────────────────────────────────────────────────────       │
│                                                                      │
│  *:00, *:01, *:02 ... (每分鐘)                                      │
│    SignalMonitorJob ◄── 監控新信號                                  │
│                                                                      │
│  *:00, *:05, *:10 ... (每 5 分鐘)                                   │
│    BatchNotificationJob ◄── 處理批次通知                            │
│    NotificationRetryJob ◄── 重試失敗通知                            │
│                                                                      │
│  週日 04:00 InactiveDeviceCleanupJob (每週)                         │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Job 詳細配置

### 2.1 SignalMonitorJob（信號監控）

```java
@Component
@Slf4j
public class SignalMonitorJob implements Job {

    @Autowired
    private UnifiedSignalRepository signalRepository;

    @Autowired
    private AlertTriggerService alertTriggerService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String LAST_CHECK_KEY = "alert:signal:last_check";

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("開始執行信號監控 Job");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 取得上次檢查時間
            OffsetDateTime lastCheck = getLastCheckTime();
            OffsetDateTime now = OffsetDateTime.now();

            // 查詢新信號
            List<UnifiedSignal> newSignals = signalRepository.findNewSignalsSince(
                lastCheck,
                List.of("A+", "A", "B+", "B")
            );

            if (!newSignals.isEmpty()) {
                log.info("發現 {} 則新信號，開始處理警報", newSignals.size());
                alertTriggerService.processSignals(newSignals);
            }

            // 更新檢查時間
            updateLastCheckTime(now);

            stopWatch.stop();
            log.debug("信號監控 Job 完成，處理 {} 則信號，耗時 {} ms",
                newSignals.size(), stopWatch.getTotalTimeMillis());

        } catch (Exception e) {
            log.error("信號監控 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }

    private OffsetDateTime getLastCheckTime() {
        String cached = (String) redisTemplate.opsForValue().get(LAST_CHECK_KEY);
        if (cached != null) {
            return OffsetDateTime.parse(cached);
        }
        return OffsetDateTime.now().minusMinutes(1);
    }

    private void updateLastCheckTime(OffsetDateTime time) {
        redisTemplate.opsForValue().set(LAST_CHECK_KEY, time.toString());
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
alert:
  jobs:
    signal-monitor:
      cron: "0 * * * * ?"  # 每分鐘
      enabled: true
```

---

### 2.2 BatchNotificationJob（批次通知）

```java
@Component
@Slf4j
public class BatchNotificationJob implements Job {

    @Autowired
    private BatchNotificationQueue batchQueue;

    @Autowired
    private NotificationDispatcher dispatcher;

    @Autowired
    private NotificationTemplateService templateService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("開始執行批次通知 Job");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 取得待處理的批次
            Map<String, List<AlertHistory>> pendingBatches = batchQueue.getPendingBatches();

            int batchCount = 0;
            for (Map.Entry<String, List<AlertHistory>> entry : pendingBatches.entrySet()) {
                String userId = entry.getKey();
                List<AlertHistory> alerts = entry.getValue();

                if (alerts.size() >= getBatchThreshold(userId)) {
                    // 發送批次摘要
                    sendBatchSummary(userId, alerts);
                    batchQueue.clearUserBatch(userId);
                    batchCount++;
                } else if (isWindowExpired(alerts)) {
                    // 視窗過期，逐一發送
                    for (AlertHistory alert : alerts) {
                        dispatcher.dispatchSingle(alert);
                    }
                    batchQueue.clearUserBatch(userId);
                }
            }

            stopWatch.stop();
            log.info("批次通知 Job 完成，處理 {} 個批次，耗時 {} ms",
                batchCount, stopWatch.getTotalTimeMillis());

        } catch (Exception e) {
            log.error("批次通知 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }

    private void sendBatchSummary(String userId, List<AlertHistory> alerts) {
        // 渲染批次摘要
        String summary = templateService.renderBatchSummary(alerts);

        UserNotificationSettings settings = settingsRepository.findByUserId(userId)
            .orElse(new UserNotificationSettings());

        // 發送至各管道
        for (NotificationChannel channel : getEnabledChannels(settings)) {
            dispatcher.sendBatchNotification(userId, alerts, channel, summary);
        }
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
alert:
  jobs:
    batch-notification:
      cron: "0 */5 * * * ?"  # 每 5 分鐘
      enabled: true
  batch:
    window-minutes: 5
    threshold: 3
```

---

### 2.3 NotificationRetryJob（通知重試）

```java
@Component
@Slf4j
public class NotificationRetryJob implements Job {

    @Autowired
    private NotificationLogRepository logRepository;

    @Autowired
    private NotificationDispatcher dispatcher;

    private static final int MAX_RETRY_COUNT = 3;
    private static final int BATCH_SIZE = 100;

    // 重試延遲（分鐘）
    private static final int[] RETRY_DELAYS = {1, 5, 15};

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.debug("開始執行通知重試 Job");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 查詢待重試的通知
            List<NotificationLog> failedLogs = logRepository.findRetryableLogs(
                NotificationStatus.FAILED,
                MAX_RETRY_COUNT,
                BATCH_SIZE
            );

            int retried = 0;
            int succeeded = 0;

            for (NotificationLog log : failedLogs) {
                if (!isRetryTimeReached(log)) {
                    continue;
                }

                try {
                    // 重試發送
                    dispatcher.retrySend(log);
                    log.setStatus(NotificationStatus.SENT);
                    log.setSentAt(OffsetDateTime.now());
                    succeeded++;
                } catch (Exception e) {
                    log.setRetryCount(log.getRetryCount() + 1);
                    log.setErrorMessage(e.getMessage());

                    if (log.getRetryCount() >= MAX_RETRY_COUNT) {
                        log.setStatus(NotificationStatus.FAILED);  // 永久失敗
                    }
                }

                logRepository.save(log);
                retried++;
            }

            stopWatch.stop();
            log.info("通知重試 Job 完成，重試 {} 則，成功 {} 則，耗時 {} ms",
                retried, succeeded, stopWatch.getTotalTimeMillis());

        } catch (Exception e) {
            log.error("通知重試 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }

    private boolean isRetryTimeReached(NotificationLog log) {
        int retryCount = log.getRetryCount();
        if (retryCount >= RETRY_DELAYS.length) {
            return false;
        }

        int delayMinutes = RETRY_DELAYS[retryCount];
        OffsetDateTime retryTime = log.getUpdatedAt().plusMinutes(delayMinutes);
        return OffsetDateTime.now().isAfter(retryTime);
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
alert:
  jobs:
    notification-retry:
      cron: "0 */5 * * * ?"  # 每 5 分鐘
      enabled: true
      max-retry-count: 3
      batch-size: 100
```

---

### 2.4 DailyCountResetJob（每日計數重置）

```java
@Component
@Slf4j
public class DailyCountResetJob implements Job {

    @Autowired
    private DailyNotificationCountRepository countRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("開始執行每日計數重置 Job");

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            // 刪除 30 天前的計數記錄
            LocalDate cutoffDate = LocalDate.now().minusDays(30);
            int deleted = countRepository.deleteByCountDateBefore(cutoffDate);

            log.info("每日計數重置 Job 完成，刪除 {} 筆過期計數記錄", deleted);

        } catch (Exception e) {
            log.error("每日計數重置 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
alert:
  jobs:
    daily-count-reset:
      cron: "0 0 0 * * ?"  # 每日 00:00
      enabled: true
```

---

### 2.5 HistoryCleanupJob（歷史清理）

```java
@Component
@Slf4j
public class HistoryCleanupJob implements Job {

    @Autowired
    private AlertHistoryRepository historyRepository;

    @Autowired
    private NotificationLogRepository logRepository;

    @Value("${alert.cleanup.history-retention-days:180}")
    private int historyRetentionDays;

    @Value("${alert.cleanup.log-retention-days:90}")
    private int logRetentionDays;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("開始執行歷史清理 Job");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            // 清理警報歷史（180 天）
            OffsetDateTime historyCutoff = OffsetDateTime.now().minusDays(historyRetentionDays);
            int deletedHistory = historyRepository.deleteByTriggeredAtBefore(historyCutoff);
            log.info("已刪除 {} 筆過期警報歷史", deletedHistory);

            // 清理通知日誌（90 天）
            OffsetDateTime logCutoff = OffsetDateTime.now().minusDays(logRetentionDays);
            int deletedLogs = logRepository.deleteByCreatedAtBefore(logCutoff);
            log.info("已刪除 {} 筆過期通知日誌", deletedLogs);

            stopWatch.stop();
            log.info("歷史清理 Job 完成，耗時 {} 秒", stopWatch.getTotalTimeSeconds());

        } catch (Exception e) {
            log.error("歷史清理 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
alert:
  jobs:
    history-cleanup:
      cron: "0 0 3 * * ?"  # 每日 03:00
      enabled: true
  cleanup:
    history-retention-days: 180
    log-retention-days: 90
```

---

### 2.6 InactiveDeviceCleanupJob（非活躍裝置清理）

```java
@Component
@Slf4j
public class InactiveDeviceCleanupJob implements Job {

    @Autowired
    private UserDeviceRepository deviceRepository;

    @Value("${alert.cleanup.device-inactive-days:90}")
    private int deviceInactiveDays;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("開始執行非活躍裝置清理 Job");

        try {
            OffsetDateTime cutoff = OffsetDateTime.now().minusDays(deviceInactiveDays);

            // 標記非活躍
            int deactivated = deviceRepository.deactivateInactiveDevices(cutoff);
            log.info("已標記 {} 個非活躍裝置", deactivated);

            // 刪除已標記超過 30 天的裝置
            OffsetDateTime deleteCutoff = OffsetDateTime.now().minusDays(30);
            int deleted = deviceRepository.deleteInactiveDevicesBefore(deleteCutoff);
            log.info("已刪除 {} 個過期裝置記錄", deleted);

            log.info("非活躍裝置清理 Job 完成");

        } catch (Exception e) {
            log.error("非活躍裝置清理 Job 執行失敗", e);
            throw new JobExecutionException(e);
        }
    }
}
```

**Quartz 配置**:
```yaml
# application.yml
alert:
  jobs:
    inactive-device-cleanup:
      cron: "0 0 4 ? * SUN"  # 每週日 04:00
      enabled: true
  cleanup:
    device-inactive-days: 90
```

---

## 3. Quartz 整合配置

### 3.1 Job 註冊

```java
@Configuration
public class M15JobConfig {

    @Bean
    public JobDetail signalMonitorJobDetail() {
        return JobBuilder.newJob(SignalMonitorJob.class)
            .withIdentity("signalMonitorJob", "m15")
            .withDescription("信號監控")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger signalMonitorTrigger(
            @Qualifier("signalMonitorJobDetail") JobDetail jobDetail,
            @Value("${alert.jobs.signal-monitor.cron}") String cron) {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("signalMonitorTrigger", "m15")
            .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                .withMisfireHandlingInstructionFireAndProceed())
            .build();
    }

    @Bean
    public JobDetail batchNotificationJobDetail() {
        return JobBuilder.newJob(BatchNotificationJob.class)
            .withIdentity("batchNotificationJob", "m15")
            .withDescription("批次通知處理")
            .storeDurably()
            .build();
    }

    @Bean
    public Trigger batchNotificationTrigger(
            @Qualifier("batchNotificationJobDetail") JobDetail jobDetail,
            @Value("${alert.jobs.batch-notification.cron}") String cron) {
        return TriggerBuilder.newTrigger()
            .forJob(jobDetail)
            .withIdentity("batchNotificationTrigger", "m15")
            .withSchedule(CronScheduleBuilder.cronSchedule(cron)
                .withMisfireHandlingInstructionDoNothing())
            .build();
    }

    // ... 其他 Job 配置
}
```

---

## 4. Job 依賴關係

```
┌─────────────────────────────────────────────────────────────────────┐
│                       M15 Job 依賴關係                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    即時處理鏈                                │    │
│  │                                                              │    │
│  │  ┌─────────────────┐                                        │    │
│  │  │ M13 信號引擎    │                                        │    │
│  │  │ 完成            │                                        │    │
│  │  └────────┬────────┘                                        │    │
│  │           │ 觸發                                             │    │
│  │           ▼                                                  │    │
│  │  ┌─────────────────┐                                        │    │
│  │  │ SignalMonitor   │ ◄── 每分鐘執行                         │    │
│  │  │ Job             │                                        │    │
│  │  └────────┬────────┘                                        │    │
│  │           │ 產生警報                                         │    │
│  │           ▼                                                  │    │
│  │  ┌─────────────────┐                                        │    │
│  │  │ BatchNotification│ ◄── 處理批次佇列                      │    │
│  │  │ Job             │                                        │    │
│  │  └────────┬────────┘                                        │    │
│  │           │ 發送失敗                                         │    │
│  │           ▼                                                  │    │
│  │  ┌─────────────────┐                                        │    │
│  │  │ NotificationRetry│ ◄── 重試失敗通知                      │    │
│  │  │ Job             │                                        │    │
│  │  └─────────────────┘                                        │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌─────────────────────────────────────────────────────────────┐    │
│  │                    維護任務（獨立執行）                       │    │
│  │                                                              │    │
│  │  ┌─────────────────┐  ┌─────────────────┐                   │    │
│  │  │ DailyCountReset │  │ HistoryCleanup  │                   │    │
│  │  │ Job (00:00)     │  │ Job (03:00)     │                   │    │
│  │  └─────────────────┘  └─────────────────┘                   │    │
│  │                                                              │    │
│  │  ┌─────────────────┐                                        │    │
│  │  │ InactiveDevice  │                                        │    │
│  │  │ Cleanup (週日)   │                                        │    │
│  │  └─────────────────┘                                        │    │
│  └─────────────────────────────────────────────────────────────┘    │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 5. 監控與告警

### 5.1 監控指標

| 指標名稱 | 說明 | 告警閾值 |
|---------|------|---------|
| m15_signal_monitor_duration_ms | 信號監控耗時 | > 30 秒 |
| m15_signal_monitor_count | 處理信號數 | N/A |
| m15_notification_retry_count | 重試通知數 | > 100/次 |
| m15_notification_retry_success_rate | 重試成功率 | < 50% |
| m15_batch_queue_size | 批次佇列大小 | > 1000 |

### 5.2 告警配置

```yaml
# prometheus-rules.yml
groups:
  - name: m15-job-alerts
    rules:
      - alert: M15SignalMonitorJobSlow
        expr: m15_signal_monitor_duration_ms > 30000
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "M15 信號監控 Job 執行過慢"

      - alert: M15NotificationRetryHighFailure
        expr: m15_notification_retry_success_rate < 0.5
        for: 15m
        labels:
          severity: critical
        annotations:
          summary: "M15 通知重試成功率過低"

      - alert: M15BatchQueueBacklog
        expr: m15_batch_queue_size > 1000
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "M15 批次佇列積壓"
```

---

## 6. 運維操作

### 6.1 手動觸發

```bash
# 手動觸發信號監控
curl -X POST http://localhost:8080/actuator/quartz/jobs/m15/signalMonitorJob/trigger

# 手動觸發歷史清理
curl -X POST http://localhost:8080/actuator/quartz/jobs/m15/historyCleanupJob/trigger
```

### 6.2 暫停與恢復

```bash
# 暫停所有 M15 Job
curl -X POST http://localhost:8080/actuator/quartz/triggers/m15/pause-all

# 恢復所有 M15 Job
curl -X POST http://localhost:8080/actuator/quartz/triggers/m15/resume-all
```

---

## 7. 相關文檔

- [M15 功能需求](../specs/functional/M15-警報通知系統功能需求.md)
- [M15 業務流程](../design/M15-業務流程.md)
- [M15 效能考量](../design/M15-效能考量.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
