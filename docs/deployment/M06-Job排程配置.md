# M06-資料管理模組 Job 排程配置

> **文件編號**: JOB-M06  
> **模組名稱**: 資料管理模組  
> **版本**: v2.0  
> **最後更新**: 2025-12-31  
> **狀態**: Draft

---

## 📋 Job 排程總覽

本文件定義 資料管理模組的所有批次作業與排程配置。

---

## 6. Job/排程設計

> **重要**: 所有 Job 必須遵守總綱 4.5 Job/排程模型

### 6.1 Job 清單

| Job 編號 | Job 名稱 | Job 類型 | 執行頻率 | 執行時間 | 優先級 | 預估時長 |
|---------|---------|---------|---------|---------|-------|---------|
| JOB-M06-001 | SYNC_STOCK_PRICES | DATA_SYNC | 每交易日 | 15:00 | P0 | 3-5 分鐘 |
| JOB-M06-002 | SYNC_INSTITUTIONAL_TRADING | DATA_SYNC | 每交易日 | 15:30 | P1 | 2-3 分鐘 |
| JOB-M06-003 | SYNC_MARGIN_TRADING | DATA_SYNC | 每交易日 | 15:30 | P1 | 2-3 分鐘 |
| JOB-M06-004 | SYNC_FINANCIAL_STATEMENTS | DATA_SYNC | 每週一次 | 每週一 08:00 | P1 | 10-15 分鐘 |
| JOB-M06-005 | UPDATE_STOCK_LIST | DATA_SYNC | 每月 | 每月1日 08:00 | P2 | 5 分鐘 |
| JOB-M06-006 | CHECK_DATA_QUALITY | MAINTENANCE | 每日 | 01:00 | P0 | 10 分鐘 |
| JOB-M06-007 | CLEANUP_OLD_JOB_LOGS | MAINTENANCE | 每週 | 每週日 03:00 | P2 | 5 分鐘 |
| JOB-M06-008 | ARCHIVE_OLD_PRICES | MAINTENANCE | 每年 | 每年1月1日 04:00 | P2 | 30 分鐘 |

---

### 6.2 Job 詳細設計

#### JOB-M06-001: 股價資料同步

**Cron 表達式**: `0 0 15 * * MON-FRI`（週一到週五 15:00）

**執行邏輯**:
1. 檢查當日是否為交易日（查詢 trading_calendar 表）
2. 若非交易日，記錄 SKIPPED 狀態並結束
3. 若為交易日，執行同步流程（參見 5.2 節）
4. 記錄 Job 執行結果（job_executions 表）

**參數**:
```json
{
  "job_name": "SYNC_STOCK_PRICES",
  "trade_date": "2024-12-24",
  "stock_ids": null,
  "force_update": false,
  "batch_size": 50
}
```

**預期執行時長**: 3-5 分鐘（1800 檔股票）

**冪等性設計**:
- 使用 PostgreSQL `ON CONFLICT (stock_id, trade_date) DO UPDATE`
- 唯一鍵: (stock_id, trade_date)
- 重複執行不會產生重複資料，只會更新現有資料

**失敗處理**:
- 單一股票失敗不影響其他股票（continue on error）
- 記錄失敗股票清單到 job_execution_details
- 失敗率 > 10% 觸發告警（透過 M15 告警系統）
- 支援手動重跑特定日期的 Job

**資料來源優先順序**:
1. Yahoo Finance API（主要）
2. 證交所公開資訊 API（備援）
3. FinMind API（第二備援，若啟用）

---

#### JOB-M06-002: 三大法人買賣超資料同步

**Cron 表達式**: `0 30 15 * * MON-FRI`（週一到週五 15:30）

**執行邏輯**:
1. 檢查當日是否為交易日
2. 從證交所 API 下載三大法人買賣超資料
3. 解析 CSV 或 JSON 格式
4. 驗證資料（買賣超數值合理性）
5. 使用 MyBatis 批次 UPSERT 到 institutional_trading 表
6. 記錄 Job 執行結果

**冪等性設計**:
- 唯一鍵: (stock_id, trade_date)
- 重複執行覆蓋舊資料

**資料驗證規則**:
- foreign_buy, foreign_sell, trust_buy, trust_sell, dealer_buy, dealer_sell >= 0
- 買賣超計算欄位自動由 PostgreSQL GENERATED COLUMN 維護

---

#### JOB-M06-003: 融資融券資料同步

**Cron 表達式**: `0 30 15 * * MON-FRI`（與 JOB-M06-002 同時執行）

**執行邏輯**:
1. 檢查當日是否為交易日
2. 從證交所 API 下載融資融券資料
3. 解析資料
4. 驗證資料（餘額、額度合理性）
5. 使用 MyBatis 批次 UPSERT 到 margin_trading 表
6. 記錄 Job 執行結果

**冪等性設計**:
- 唯一鍵: (stock_id, trade_date)
- 使用率自動由 GENERATED COLUMN 計算

---

#### JOB-M06-004: 財報資料同步

**觸發方式**: 
- 定期檢查：每週一 08:00
- 事件觸發：檢測到新財報公告時立即執行

**Cron 表達式**: `0 0 8 * * MON`

**執行邏輯**:
1. 查詢公開資訊觀測站最新公告清單
2. 比對 financial_statements 表，找出尚未同步的財報
3. 逐筆下載並解析財報（參見 5.3 節）
4. 驗證資料完整性
5. 儲存核心欄位與 JSONB 詳細資料
6. 記錄 Job 執行結果

**資料來源**:
- 公開資訊觀測站（XBRL 格式優先）
- PDF 備援（需 OCR 解析）

**冪等性設計**:
- 唯一鍵: (stock_id, year, quarter, report_type)
- 若財報已存在，更新為最新版本（修正財報）

**特殊處理**:
- 財報資料結構複雜，部分欄位儲存於 JSONB（income_statement, balance_sheet, cash_flow_statement）
- 解析失敗的財報標記為 MANUAL_REVIEW，需人工確認

---

#### JOB-M06-005: 股票清單更新

**Cron 表達式**: `0 0 8 1 * *`（每月 1 日 08:00）

**執行邏輯**:
1. 從證交所 API 下載最新上市櫃公司清單
2. 比對 stocks 表，找出新上市/下市股票
3. 新上市：INSERT 新記錄
4. 下市：更新 is_active = false, delisting_date = 下市日期
5. 發布 StockListUpdated 事件

**冪等性設計**:
- 主鍵: stock_id
- 使用 UPSERT 機制

---

#### JOB-M06-006: 資料品質檢核

**Cron 表達式**: `0 0 1 * * *`（每日 01:00）

**執行邏輯**:
1. 執行預定義的資料品質檢核規則（參照總綱 4.7）
2. 檢查前一交易日資料
3. 記錄檢核結果到 data_quality_issues 表
4. 高嚴重度問題觸發告警

**檢核項目**:

**檢核 1: 股價資料完整性**
```
規則: 檢查所有活躍股票是否都有前一交易日的股價資料

SELECT s.stock_id, s.stock_name
FROM stocks s
WHERE s.is_active = true
  AND NOT EXISTS (
    SELECT 1 FROM stock_prices sp
    WHERE sp.stock_id = s.stock_id
      AND sp.trade_date = :previous_trading_day
  )
```

**檢核 2: 股價四價一致性**
```
規則: 檢查四價關係是否正確

SELECT stock_id, trade_date
FROM stock_prices
WHERE trade_date = :previous_trading_day
  AND (
    low_price > open_price OR
    low_price > close_price OR
    high_price < open_price OR
    high_price < close_price
  )
```

**檢核 3: 成交量合理性**
```
規則: 檢查成交量是否為負數或異常大

SELECT stock_id, trade_date, volume
FROM stock_prices
WHERE trade_date = :previous_trading_day
  AND (volume < 0 OR volume > 1000000000)
```

**檢核 4: 財報資產負債平衡**
```
規則: 檢查 total_assets = total_liabilities + equity

SELECT stock_id, year, quarter
FROM financial_statements
WHERE ABS(total_assets - (total_liabilities + equity)) > 1000
```

---

#### JOB-M06-007: 清理舊 Job 執行記錄

**Cron 表達式**: `0 0 3 * * SUN`（每週日 03:00）

**執行邏輯**:
1. 將 3 個月前的 job_executions 記錄歸檔到 job_executions_archive
2. 刪除已歸檔的記錄
3. 記錄清理統計

**SQL 邏輯**:
```sql
-- 歸檔
INSERT INTO job_executions_archive
SELECT * FROM job_executions
WHERE end_time < CURRENT_TIMESTAMP - INTERVAL '3 months';

-- 刪除
DELETE FROM job_executions
WHERE end_time < CURRENT_TIMESTAMP - INTERVAL '3 months';
```

---

#### JOB-M06-008: 歸檔舊股價資料

**Cron 表達式**: `0 0 4 1 1 *`（每年 1 月 1 日 04:00）

**執行邏輯**:
1. 檢查 stock_prices 表是否有 20 年前的資料
2. 若有，DETACH 該年份的分區
3. 將分區資料匯出到歸檔儲存（如 S3）
4. 刪除已歸檔的分區
5. 記錄歸檔統計

**PostgreSQL 分區操作**:
```sql
-- DETACH 分區（不刪除資料）
ALTER TABLE stock_prices DETACH PARTITION stock_prices_2005;

-- 匯出分區資料
COPY stock_prices_2005 TO '/archive/stock_prices_2005.csv' CSV HEADER;

-- 刪除分區
DROP TABLE stock_prices_2005;
```

---

### 6.3 Job 依賴關係

```
每交易日:
15:00 - SYNC_STOCK_PRICES (JOB-M06-001)
          ↓
15:30 - SYNC_INSTITUTIONAL_TRADING (JOB-M06-002)
        SYNC_MARGIN_TRADING (JOB-M06-003)
        （可並行執行）
          ↓
01:00 - CHECK_DATA_QUALITY (JOB-M06-006)
        （次日檢查前一日資料）

每週:
週一 08:00 - SYNC_FINANCIAL_STATEMENTS (JOB-M06-004)
週日 03:00 - CLEANUP_OLD_JOB_LOGS (JOB-M06-007)

每月:
1日 08:00 - UPDATE_STOCK_LIST (JOB-M06-005)

每年:
1月1日 04:00 - ARCHIVE_OLD_PRICES (JOB-M06-008)
```

---

### 6.4 Job 監控與告警

**監控指標**:
| 指標 | 閾值 | 告警等級 | 處理方式 |
|-----|------|---------|---------|
| Job 執行時長 | > 預估時長 × 2 | WARNING | 記錄慢查詢 |
| Job 失敗率 | > 10% | ERROR | 立即告警 |
| Job 連續失敗 | >= 3 次 | CRITICAL | 立即告警並暫停 |
| 資料品質問題 | HIGH 嚴重度 > 10 筆 | ERROR | 通知資料管理員 |

**告警通道**（透過 M15 告警系統）:
- Email
- Slack
- SMS（CRITICAL 等級）

---


---

## 📚 相關文檔

- [全系統 Job 模型](../specs/technical/00-全系統契約.md#45-job-模型)
- [M06 功能需求](../specs/functional/M06-資料管理功能需求.md)
- [環境配置說明](./環境配置說明.md)

---

**文件維護者**: DevOps 工程師  
**最後更新**: 2025-12-31
