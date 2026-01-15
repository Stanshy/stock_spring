# M12-總經與產業分析模組 Job 排程配置

> **文件編號**: JOB-M12
> **模組名稱**: 總經與產業分析模組
> **版本**: v1.0
> **最後更新**: 2026-01-14
> **狀態**: Draft

---

## 📋 Job 排程總覽

本文件定義總經與產業分析模組的所有批次作業與排程配置。

---

## 1. Job 清單

| Job 編號 | Job 名稱 | Job 類型 | 執行頻率 | 執行時間 | 優先級 | 預估時長 |
|---------|---------|---------|---------|---------|-------|---------|
| JOB-M12-001 | CALC_SECTOR_PERFORMANCE | ANALYSIS | 每交易日 | 16:30 | P0 | 3-5 分鐘 |
| JOB-M12-002 | CALC_SECTOR_VALUATION | ANALYSIS | 每交易日 | 17:00 | P0 | 2-3 分鐘 |
| JOB-M12-003 | UPDATE_SECTOR_RANKING | ANALYSIS | 每交易日 | 17:10 | P1 | 1 分鐘 |
| JOB-M12-004 | ANALYZE_ROTATION | ANALYSIS | 每週五 | 17:30 | P0 | 2-3 分鐘 |
| JOB-M12-005 | UPDATE_MACRO_INDICATORS | DATA_SYNC | 每日 | 09:00 | P1 | 1-2 分鐘 |
| JOB-M12-006 | DETERMINE_ECONOMIC_CYCLE | ANALYSIS | 每月 5 日 | 10:00 | P0 | 1 分鐘 |
| JOB-M12-007 | NOTIFY_M13_SIGNALS | EVENT | 每交易日 | 17:20 | P0 | < 1 分鐘 |
| JOB-M12-008 | CLEANUP_OLD_DATA | MAINTENANCE | 每月 1 日 | 03:00 | P2 | 5 分鐘 |

---

## 2. Job 詳細設計

### JOB-M12-001: 每日產業績效計算

**Cron 表達式**: `0 30 16 * * MON-FRI`（週一到週五 16:30）

**執行邏輯**:
1. 檢查當日是否為交易日
2. 確認 M06 股價資料已同步完成
3. 載入所有產業分類與成分股
4. 依產業計算績效指標（市值加權）
5. 計算產業排名
6. 批次儲存結果至 sector_performance

**前置條件**:
- JOB-M06-001 (股價同步) 完成
- 交易日 15:30 後

**參數**:
```json
{
  "job_name": "CALC_SECTOR_PERFORMANCE",
  "trade_date": "2024-12-24",
  "calculation_config": {
    "weighting_method": "MARKET_CAP",
    "return_periods": [1, 5, 20, 60],
    "include_ytd": true,
    "calculate_momentum": true,
    "calculate_breadth": true
  },
  "sector_filter": {
    "classification_type": "TWSE",
    "levels": [2],
    "exclude_inactive": true
  }
}
```

**預期執行時長**: 3-5 分鐘（約 30 個產業）

**冪等性設計**:
- 使用 (sector_code, trade_date) 作為唯一鍵
- 重複執行會覆蓋當日資料

**失敗處理**:
- M06 資料未就緒：等待 5 分鐘後重試（最多 3 次）
- 單一產業計算失敗不影響其他產業
- 失敗產業數 > 30% 觸發告警

---

### JOB-M12-002: 每日產業估值計算

**Cron 表達式**: `0 0 17 * * MON-FRI`（週一到週五 17:00）

**前置條件**:
- JOB-M12-001 完成
- JOB-M08-001 (財務指標計算) 完成

**執行邏輯**:
1. 確認 M08 財務指標已更新
2. 載入各股估值指標（PE, PB, 股利率）
3. 依產業計算加權估值
4. 計算歷史百分位
5. 判斷估值信號
6. 儲存結果至 sector_valuation

**參數**:
```json
{
  "job_name": "CALC_SECTOR_VALUATION",
  "trade_date": "2024-12-24",
  "valuation_config": {
    "metrics": ["PE_RATIO", "PB_RATIO", "DIVIDEND_YIELD"],
    "weighting_method": "MARKET_CAP",
    "history_years": 5,
    "percentile_thresholds": {
      "undervalued": 20,
      "overvalued": 80
    }
  }
}
```

**預期執行時長**: 2-3 分鐘

**失敗處理**:
- M08 資料未就緒：等待 10 分鐘後重試
- 使用前一日估值作為備援

---

### JOB-M12-003: 更新產業排名

**Cron 表達式**: `0 10 17 * * MON-FRI`（週一到週五 17:10）

**前置條件**: JOB-M12-001 完成

**執行邏輯**:
1. 讀取當日 sector_performance
2. 依報酬率、動能等指標排序
3. 更新排名欄位
4. 更新快取

**參數**:
```json
{
  "job_name": "UPDATE_SECTOR_RANKING",
  "trade_date": "2024-12-24",
  "ranking_types": ["RETURN_1D", "RETURN_5D", "RETURN_20D", "MOMENTUM", "RELATIVE_STRENGTH", "BREADTH"]
}
```

**預期執行時長**: < 1 分鐘

---

### JOB-M12-004: 週產業輪動分析

**Cron 表達式**: `0 30 17 * * FRI`（每週五 17:30）

**前置條件**: JOB-M12-001 完成

**執行邏輯**:
1. 載入過去 12 週產業績效資料
2. 計算每週排名變化
3. 識別輪動趨勢（領漲、轉強、轉弱、落後）
4. 與經濟週期交叉驗證
5. 產生輪動信號
6. 儲存分析結果與信號

**參數**:
```json
{
  "job_name": "ANALYZE_ROTATION",
  "analysis_date": "2024-12-27",
  "rotation_config": {
    "analysis_weeks": 12,
    "top_threshold_percent": 33,
    "significant_rank_change": 5,
    "leading_weeks_threshold": 4
  },
  "signal_config": {
    "generate_signals": true,
    "signal_types": ["INDUSTRY_ROTATION", "INDUSTRY_MOMENTUM"]
  }
}
```

**輪動信號產生條件**:

| 信號代碼 | 條件 |
|---------|------|
| IND_SIG_001 | 排名上升 > 5 且進入前 1/3 |
| IND_SIG_002 | 排名下降 > 5 且跌出前 1/3 |
| IND_SIG_003 | 連續 3 週排名上升且進入前 20% |
| IND_SIG_004 | 連續 5 週維持前 20% |

**預期執行時長**: 2-3 分鐘

---

### JOB-M12-005: 更新總經指標

**Cron 表達式**: `0 0 9 * * *`（每日 09:00）

**執行邏輯**:
1. 檢查各資料來源是否有新資料
2. 抓取最新總經指標數值
3. 更新 macro_indicator_values
4. 檢測異常值並告警

**參數**:
```json
{
  "job_name": "UPDATE_MACRO_INDICATORS",
  "update_date": "2024-12-24",
  "data_sources": [
    {
      "source": "TWSE",
      "indicators": ["TW_TAIEX", "TW_VOLUME"]
    },
    {
      "source": "CENTRAL_BANK",
      "indicators": ["TW_M1B_YOY", "TW_M2_YOY", "TW_POLICY_RATE"]
    },
    {
      "source": "NDC",
      "indicators": ["TW_LEADING_INDEX", "TW_COINCIDENT_INDEX", "TW_MONITOR_SCORE"]
    },
    {
      "source": "FRED",
      "indicators": ["US_FED_RATE", "US_10Y_YIELD", "VIX"]
    }
  ],
  "validation": {
    "check_anomaly": true,
    "anomaly_threshold_std": 3
  }
}
```

**預期執行時長**: 1-2 分鐘

**失敗處理**:
- 單一來源失敗不影響其他來源
- 記錄失敗來源並稍後重試

---

### JOB-M12-006: 經濟週期判斷

**Cron 表達式**: `0 0 10 5 * *`（每月 5 日 10:00）

**說明**: 每月 5 日執行，因為國發會景氣指標通常在每月初公布

**前置條件**: JOB-M12-005 已更新當月總經指標

**執行邏輯**:
1. 載入關鍵總經指標最近 6 個月資料
2. 計算領先指標趨勢
3. 計算同時指標歷史位置
4. 判斷經濟週期階段
5. 比較前一週期判斷
6. 若週期變化，產生信號

**參數**:
```json
{
  "job_name": "DETERMINE_ECONOMIC_CYCLE",
  "analysis_date": "2024-12-05",
  "cycle_config": {
    "key_indicators": ["TW_LEADING_INDEX", "TW_COINCIDENT_INDEX", "TW_MONITOR_SCORE"],
    "trend_months": 3,
    "history_years_for_percentile": 5
  },
  "signal_config": {
    "generate_on_change": true
  }
}
```

**預期執行時長**: < 1 分鐘

---

### JOB-M12-007: 通知 M13 新信號

**Cron 表達式**: `0 20 17 * * MON-FRI`（週一到週五 17:20）

**前置條件**: JOB-M12-001, JOB-M12-002, JOB-M12-003 完成

**執行邏輯**:
1. 查詢當日新增的未消費信號數量
2. 若有新信號，發送事件通知 M13
3. 記錄通知結果

**參數**:
```json
{
  "job_name": "NOTIFY_M13_SIGNALS",
  "trade_date": "2024-12-24",
  "notification_config": {
    "channel": "EVENT_BUS",
    "event_type": "INDUSTRY_SIGNALS_READY",
    "include_summary": true
  }
}
```

**事件訊息格式**:
```json
{
  "event_type": "INDUSTRY_SIGNALS_READY",
  "trade_date": "2024-12-24",
  "summary": {
    "total_signals": 5,
    "by_type": {
      "INDUSTRY_ROTATION": 2,
      "INDUSTRY_MOMENTUM": 1,
      "INDUSTRY_VALUATION": 2
    }
  },
  "timestamp": "2024-12-24T17:20:00+08:00"
}
```

**預期執行時長**: < 1 分鐘

---

### JOB-M12-008: 清理舊資料

**Cron 表達式**: `0 0 3 1 * *`（每月 1 日 03:00）

**執行邏輯**:
1. 刪除超過保留期限的績效資料（保留 3 年）
2. 刪除超過保留期限的估值資料（保留 3 年）
3. 刪除已消費且超過 90 天的信號
4. VACUUM 相關資料表

**參數**:
```json
{
  "job_name": "CLEANUP_OLD_DATA",
  "retention_config": {
    "sector_performance_years": 3,
    "sector_valuation_years": 3,
    "signals_consumed_days": 90,
    "vacuum_tables": ["sector_performance", "sector_valuation", "industry_signals"]
  }
}
```

**預期執行時長**: 5 分鐘

---

## 3. Job 依賴關係

```
每交易日:

M06 Jobs                     M08 Jobs                     M12 Jobs
────────────────────────────────────────────────────────────────────────────────
15:30 - SYNC_STOCK_PRICES
        (JOB-M06-001)
          │
          │                  16:30 - CALC_FUNDAMENTAL
          │                          (JOB-M08-001)
          │                            │
          └──────────────────┐         │
                             ▼         ▼
                     ┌─────────────────────────────────┐
                     │                                 │
16:30 ───────────────────── CALC_SECTOR_PERFORMANCE ──┼───────────────────────
                            (JOB-M12-001)             │
                                   │                  │
                                   ▼                  │
17:00 ───────────────────── CALC_SECTOR_VALUATION ────┼───────────────────────
                            (JOB-M12-002)             │
                                   │                  │
                                   ▼                  │
17:10 ───────────────────── UPDATE_SECTOR_RANKING ────┼───────────────────────
                            (JOB-M12-003)             │
                                   │                  │
                                   ▼                  │
17:20 ───────────────────── NOTIFY_M13_SIGNALS ───────┘
                            (JOB-M12-007)
                                   │
                                   ▼
                            ┌─────────────────┐
                            │   M13 消費信號   │
                            │   (下游依賴)     │
                            └─────────────────┘


每週五 (額外執行):
────────────────────────────────────────────────────────────────────────────────
17:30 ───────────────────── ANALYZE_ROTATION
                            (JOB-M12-004)
                                   │
                                   ▼
                            輪動信號也通知 M13


每日 (非交易日也執行):
────────────────────────────────────────────────────────────────────────────────
09:00 ───────────────────── UPDATE_MACRO_INDICATORS
                            (JOB-M12-005)


每月:
────────────────────────────────────────────────────────────────────────────────
每月 5 日 10:00 ─ DETERMINE_ECONOMIC_CYCLE
                  (JOB-M12-006)
                        │
                        ▼
                  週期變化信號通知 M13

每月 1 日 03:00 ─ CLEANUP_OLD_DATA
                  (JOB-M12-008)
```

---

## 4. Job 監控與告警

### 4.1 監控指標

| 指標 | 閾值 | 告警等級 | 處理方式 |
|-----|------|---------|---------|
| Job 執行時長 | > 預估 × 2 | WARNING | 記錄慢執行 |
| 產業計算失敗率 | > 20% | ERROR | 立即告警 |
| M06 資料延遲 | > 30 分鐘 | WARNING | 通知 M06 團隊 |
| M08 資料延遲 | > 30 分鐘 | WARNING | 通知 M08 團隊 |
| Job 連續失敗 | >= 3 次 | CRITICAL | 暫停並告警 |
| 總經資料來源失敗 | >= 2 來源 | WARNING | 檢查資料來源 |

### 4.2 告警通道

透過 M15 告警系統：
- Email
- Slack
- SMS（CRITICAL 等級）

### 4.3 監控儀表板

| 面板 | 指標 |
|-----|------|
| 產業績效總覽 | 當日計算產業數、成功/失敗數 |
| 輪動分析狀態 | 領漲/轉強/轉弱/落後產業數 |
| 總經指標更新 | 各指標最後更新時間 |
| 經濟週期 | 當前週期、持續月數 |

---

## 5. 手動執行指引

### 5.1 執行單一產業績效計算

```bash
curl -X POST http://localhost:8080/api/v1/macro-industry/jobs/calc-sector-performance \
  -H "Content-Type: application/json" \
  -d '{
    "trade_date": "2024-12-24",
    "sector_codes": ["24", "17"]
  }'
```

### 5.2 執行輪動分析

```bash
curl -X POST http://localhost:8080/api/v1/macro-industry/jobs/analyze-rotation \
  -H "Content-Type: application/json" \
  -d '{
    "analysis_date": "2024-12-27",
    "weeks": 12
  }'
```

### 5.3 手動更新總經指標

```bash
curl -X POST http://localhost:8080/api/v1/macro-industry/jobs/update-macro \
  -H "Content-Type: application/json" \
  -d '{
    "indicators": ["TW_LEADING_INDEX", "TW_COINCIDENT_INDEX"],
    "force_update": true
  }'
```

### 5.4 手動觸發週期判斷

```bash
curl -X POST http://localhost:8080/api/v1/macro-industry/jobs/determine-cycle \
  -H "Content-Type: application/json" \
  -d '{
    "analysis_date": "2024-12-05"
  }'
```

### 5.5 補執行特定日期

```bash
curl -X POST http://localhost:8080/api/v1/macro-industry/jobs/backfill \
  -H "Content-Type: application/json" \
  -d '{
    "start_date": "2024-12-20",
    "end_date": "2024-12-24",
    "job_types": ["CALC_SECTOR_PERFORMANCE", "CALC_SECTOR_VALUATION"]
  }'
```

---

## 6. 災難恢復

### 6.1 Job 失敗恢復程序

```
┌─────────────────────────────────────────────────────────────────┐
│                    Job 失敗恢復程序                               │
└─────────────────────────────────────────────────────────────────┘

1. 檢查失敗原因
   - 查看 job_executions 表的 error_message
   - 檢查應用程式日誌

2. 判斷失敗類型
   ├── 上游資料問題（M06/M08 未完成）
   │   → 等待上游 Job 完成，重新執行
   │
   ├── 資料庫連線問題
   │   → 檢查資料庫狀態，重新執行
   │
   ├── 總經資料來源問題
   │   → 檢查外部 API，改用備援來源
   │
   └── 系統資源不足
       → 擴展資源或錯峰執行

3. 恢復執行
   curl -X POST /api/v1/macro-industry/jobs/retry \
     -d '{"job_id": "xxx", "trade_date": "2024-12-24"}'

4. 驗證結果
   - 確認 sector_performance 有新資料
   - 確認 M13 可正常消費信號
```

### 6.2 資料一致性檢查

```sql
-- 檢查產業績效是否完整
SELECT trade_date, COUNT(*) as sector_count
FROM sector_performance
WHERE trade_date >= CURRENT_DATE - INTERVAL '7 days'
GROUP BY trade_date
ORDER BY trade_date DESC;

-- 預期每日約 30 筆（30 個產業）

-- 檢查是否有缺失的產業
SELECT s.sector_code, s.sector_name
FROM sectors s
LEFT JOIN sector_performance sp
    ON s.sector_code = sp.sector_code
    AND sp.trade_date = '2024-12-24'
WHERE s.is_active = true
  AND s.classification_type = 'TWSE'
  AND s.level = 2
  AND sp.id IS NULL;
```

---

## 📚 相關文檔

- [M12 功能需求](../specs/functional/M12-總經產業分析功能需求.md)
- [M12 業務流程](../design/M12-業務流程.md)
- [M06 Job排程配置](./M06-Job排程配置.md)
- [M08 Job排程配置](./M08-Job排程配置.md)
- [全系統 Job 模型](../specs/technical/00-全系統契約.md#45-job-模型)

---

**文件維護者**: DevOps 工程師
**最後更新**: 2026-01-14
**下次審核**: 2026-04-14
