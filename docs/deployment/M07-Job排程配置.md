# M07-技術分析模組 Job 排程配置

> **文件編號**: JOB-M07  
> **模組名稱**: 技術分析模組  
> **版本**: v2.0  
> **最後更新**: 2025-12-31  
> **狀態**: Draft

---

## 📋 Job 排程總覽

本文件定義 技術分析模組的所有批次作業與排程配置。

---

## 6. Job/排程設計

### 6.1 Job 列表

| Job編號 | Job名稱 | 執行頻率 | 執行時間 | 優先級 | 說明 |
|--------|---------|---------|---------|-------|------|
| JOB-M07-001 | 基礎指標計算 | 每日 | 15:30 | P0 | 計算 P0 基礎組 11 個指標 |
| JOB-M07-002 | 進階指標計算 | 每日 | 16:00 | P1 | 計算 P1 進階組 20 個指標 |
| JOB-M07-003 | 專業指標計算 | 每週一 | 18:00 | P2 | 計算 P2 專業組 40 個指標 |
| JOB-M07-004 | 交叉信號偵測 | 每日 | 16:30 | P0 | 偵測黃金交叉、死亡交叉 |
| JOB-M07-005 | 超買超賣偵測 | 每日 | 16:30 | P0 | 偵測 RSI、KD 超買超賣 |
| JOB-M07-006 | 快取預熱 | 每日 | 07:00 | P1 | 預載熱門股票指標至 Redis |
| JOB-M07-007 | 歷史指標回填 | 手動觸發 | - | P2 | 回填歷史資料的指標計算 |

---

### 6.2 Job 設計詳細規格

#### JOB-M07-001: 基礎指標計算

**Job 資訊**:
- **Job ID**: JOB-M07-001
- **Job 名稱**: 基礎指標計算
- **執行頻率**: 每日（週一～週五）
- **執行時間**: 15:30（盤後 30 分鐘）
- **預估執行時間**: 15-20 分鐘
- **優先級**: P0（最高）

**觸發條件**:
- 定時觸發：每日 15:30（Cron: `0 30 15 * * MON-FRI`）
- 事件觸發：接收到 M06 `StockPriceUpdated` 事件（延遲 30 分鐘後觸發）

**Job 參數**:
```json
{
  "calculation_date": "2024-12-24",
  "indicator_priority": "P0",
  "stock_list": null,  // null = 所有活躍股票
  "force_recalculate": false,
  "batch_size": 50
}
```

**執行流程**:
```
1. 建立 Job 執行記錄
   INSERT INTO indicator_calculation_jobs (
     job_type, calculation_date, indicator_priority, status
   ) VALUES ('CALCULATE_INDICATORS', '2024-12-24', 'P0', 'RUNNING')
   
2. 查詢需計算的股票清單
   SELECT stock_id FROM stocks WHERE is_active = true
   若 stock_list 參數非 NULL，則只計算指定股票
   
3. 載入 P0 指標定義
   SELECT * FROM indicator_definitions 
   WHERE priority = 'P0' AND is_active = true
   
4. 批次處理（每批 50 檔股票）
   FOR EACH batch IN stock_batches:
     4.1 查詢歷史股價（最近 250 交易日）
         SELECT * FROM stock_prices 
         WHERE stock_id IN (batch) 
           AND trade_date >= CURRENT_DATE - INTERVAL '250 days'
         ORDER BY trade_date
     
     4.2 pandas-ta 批次計算
         df.ta.sma(length=[5, 20, 60])
         df.ta.ema(length=[12, 26, 50])
         df.ta.macd(fast=12, slow=26, signal=9)
         df.ta.rsi(length=14)
         df.ta.stoch(k=9, d=3, smooth_k=3)
         df.ta.bbands(length=20, std=2)
         df.ta.atr(length=14)
         df.ta.obv()
         df.ta.vwap()
         df.ta.adx(length=14)
     
     4.3 驗證計算結果
         - 檢查 NaN（資料不足期間允許）
         - 檢查值域範圍（RSI: 0-100）
         - 檢查異常值
     
     4.4 組裝 JSONB
         trend_indicators = {
           "ma": {"ma5": ..., "ma20": ..., "ma60": ...},
           "ema": {"ema12": ..., "ema26": ..., "ema50": ...},
           "macd": {...},
           "adx": {...}
         }
         
     4.5 批次 UPSERT
         INSERT INTO technical_indicators (...) VALUES (...)
         ON CONFLICT (stock_id, calculation_date) DO UPDATE ...
     
     4.6 回寫 stock_prices
         UPDATE stock_prices 
         SET ma5 = ?, ma20 = ?, volume_ma5 = ?
         WHERE stock_id = ? AND trade_date = ?
   
5. 更新 Redis 快取（熱門股票）
   熱門股票清單（市值前 50）
   FOR EACH hot_stock:
     Redis.set("tech:indicators:{stock_id}:{date}", data, TTL=1h)
   
6. 更新 Job 執行記錄
   UPDATE indicator_calculation_jobs SET
     status = 'SUCCESS',
     end_time = CURRENT_TIMESTAMP,
     duration_seconds = ...,
     statistics = {
       "total_stocks": 1800,
       "success_count": 1785,
       "failed_count": 15,
       "indicators_calculated": ["MA", "EMA", "RSI", ...]
     }
   WHERE job_id = ?
```

**冪等性設計**（遵守總綱 4.5）:
- 使用 PostgreSQL `ON CONFLICT ... DO UPDATE` 確保重複執行不產生重複資料
- Job 參數包含 `calculation_date`，確保可重跑特定日期
- 若 Job 失敗，可安全重試，不會產生副作用

**錯誤處理**:
```
IF 資料庫連線失敗:
  - 回滾交易
  - Job status = 'FAILED'
  - 記錄錯誤訊息
  - 觸發重試（最多 3 次，間隔 5 分鐘）

IF 計算結果異常（NaN、Inf）:
  - 記錄 data_quality_issues 表
  - 跳過該股票
  - 繼續處理其他股票

IF 單一股票處理超時（> 1 分鐘）:
  - 記錄警告
  - 跳過該股票
  - 繼續處理
```

**監控指標**:
- Job 執行時間（目標 < 20 分鐘）
- 成功率（目標 > 99%）
- 平均單股處理時間（目標 < 250ms）
- 快取命中率（目標 > 85%）

---

#### JOB-M07-002: 進階指標計算

**Job 資訊**:
- **Job ID**: JOB-M07-002
- **Job 名稱**: 進階指標計算
- **執行頻率**: 每日（週一～週五）
- **執行時間**: 16:00（基礎指標完成後）
- **預估執行時間**: 20-25 分鐘
- **優先級**: P1

**依賴關係**:
- 前置條件：JOB-M07-001 必須成功完成
- 檢查機制：查詢 `indicator_calculation_jobs` 表確認 JOB-M07-001 狀態為 SUCCESS

**執行流程**:
```
1. 檢查前置條件
   SELECT status FROM indicator_calculation_jobs 
   WHERE job_type = 'CALCULATE_INDICATORS'
     AND calculation_date = CURRENT_DATE
     AND indicator_priority = 'P0'
   
   IF status != 'SUCCESS':
     延遲執行（等待 5 分鐘後重試）
   
2. 載入 P1 指標定義
   SELECT * FROM indicator_definitions 
   WHERE priority = 'P1' AND is_active = true
   
3. 批次計算（流程同 JOB-M07-001）
   計算指標：
   - WMA, HMA
   - Stochastic RSI, Williams %R, CCI, MFI
   - Keltner Channel, Donchian Channel
   - AD Line, CMF
   - Aroon, Parabolic SAR, Supertrend
   - ROC, Momentum
   - Pivot Points, Fibonacci
   - Linear Regression, Slope, R-Squared
   
4. 更新 technical_indicators 表（UPSERT）
   
5. 更新 Job 執行記錄
```

---

#### JOB-M07-003: 專業指標計算

**Job 資訊**:
- **Job ID**: JOB-M07-003
- **Job 名稱**: 專業指標計算
- **執行頻率**: 每週一次
- **執行時間**: 每週一 18:00
- **預估執行時間**: 40-50 分鐘
- **優先級**: P2

**Cron 表達式**: `0 0 18 * * MON`

**執行流程**:
```
1. 載入 P2 指標定義（40 個指標）
   
2. 計算更耗時的專業指標：
   - DEMA, TEMA, VWMA, ZLEMA
   - Ultimate Oscillator, CMO, TSI, KST
   - Standard Deviation, Historical Volatility, Mass Index
   - Hurst Exponent, DPO, Schaff Trend Cycle
   - Correlation Coefficient, Z-Score
   - Elder Ray, Coppock Curve, Qstick, Vortex, BOP
   - Ichimoku Cloud, TRIX
   
3. 更新 technical_indicators 表
   
4. 執行時間較長，需監控超時
```

---

#### JOB-M07-004: 交叉信號偵測

**Job 資訊**:
- **Job ID**: JOB-M07-004
- **Job 名稱**: 交叉信號偵測
- **執行頻率**: 每日
- **執行時間**: 16:30（基礎指標計算完成後）
- **預估執行時間**: 3-5 分鐘
- **優先級**: P0

**執行流程**:
```
1. 檢查前置條件
   確認 JOB-M07-001 已成功完成
   
2. 載入今日與昨日指標資料
   SELECT stock_id, ma5, ma20, ma60, stoch_k, stoch_d
   FROM technical_indicators
   WHERE calculation_date IN (CURRENT_DATE, CURRENT_DATE - INTERVAL '1 day')
   
3. 偵測黃金交叉（MA5 上穿 MA20）
   SELECT t1.stock_id
   FROM technical_indicators t1
   JOIN technical_indicators t2 ON t1.stock_id = t2.stock_id
   WHERE t1.calculation_date = CURRENT_DATE
     AND t2.calculation_date = CURRENT_DATE - INTERVAL '1 day'
     AND t2.ma5 < t2.ma20
     AND t1.ma5 > t1.ma20
   
4. 偵測死亡交叉（MA5 下穿 MA20）
   
5. 偵測 KD 交叉
   - K 線上穿 D 線且 K < 20（低檔黃金交叉）
   - K 線下穿 D 線且 K > 80（高檔死亡交叉）
   
6. 產生信號（遵守總綱 4.1 Signal Contract）
   FOR EACH 交叉信號:
     INSERT INTO signals (
       signal_uuid, stock_id, signal_type, signal_source, signal_name,
       signal_category, trigger_price, confidence_score, evidence_data
     ) VALUES (
       UUID(), '2330', 'BUY', 'M07_TECHNICAL_ANALYSIS', 'MA_GOLDEN_CROSS',
       'TREND_CROSS', 580.0, 70, '{"indicator": "MA", ...}'::jsonb
     )
   
7. 發布事件（TechnicalSignalDetected）
   FOR EACH 信號:
     發布至 M13 信號判斷引擎
   
8. 更新 Job 執行記錄
```

**信號產生邏輯**:
```java
// 黃金交叉信號
if (yesterday.ma5 < yesterday.ma20 && today.ma5 > today.ma20) {
    Signal signal = Signal.builder()
        .signalUuid(UUID.randomUUID())
        .stockId(stockId)
        .signalType(SignalType.BUY)
        .signalSource("M07_TECHNICAL_ANALYSIS")
        .signalName("MA_GOLDEN_CROSS")
        .signalCategory("TREND_CROSS")
        .triggerPrice(currentPrice)
        .confidenceScore(70)
        .evidenceData(buildEvidence(today, yesterday))
        .build();
    
    signalRepository.save(signal);
    eventPublisher.publish(new TechnicalSignalDetectedEvent(signal));
}
```

---

#### JOB-M07-005: 超買超賣偵測

**Job 資訊**:
- **Job ID**: JOB-M07-005
- **Job 名稱**: 超買超賣偵測
- **執行頻率**: 每日
- **執行時間**: 16:30
- **預估執行時間**: 3-5 分鐘
- **優先級**: P0

**執行流程**:
```
1. 檢查前置條件
   
2. 偵測 RSI 超買（RSI > 70）
   SELECT stock_id, rsi_14
   FROM technical_indicators
   WHERE calculation_date = CURRENT_DATE
     AND rsi_14 > 70
   
3. 偵測 RSI 超賣（RSI < 30）
   
4. 計算持續天數（連續超買/超賣）
   SELECT stock_id, COUNT(*) as duration
   FROM technical_indicators
   WHERE calculation_date >= CURRENT_DATE - INTERVAL '7 days'
     AND rsi_14 > 70
   GROUP BY stock_id
   HAVING COUNT(*) >= 3
   
5. 偵測 KD 超買（K > 80 AND D > 80）
   
6. 偵測 KD 超賣（K < 20 AND D < 20）
   
7. 偵測 Williams %R 超買（> -20）
   
8. 偵測 Williams %R 超賣（< -80）
   
9. 產生信號
   - 持續天數越長，confidence_score 越高
   - 持續 1 天：confidence = 60
   - 持續 3 天：confidence = 70
   - 持續 5 天以上：confidence = 75
   
10. 發布事件
```

---

#### JOB-M07-006: 快取預熱

**Job 資訊**:
- **Job ID**: JOB-M07-006
- **Job 名稱**: 快取預熱
- **執行頻率**: 每日
- **執行時間**: 07:00（開盤前）
- **預估執行時間**: 1-2 分鐘
- **優先級**: P1

**執行流程**:
```
1. 查詢熱門股票清單（市值前 50）
   SELECT stock_id FROM stocks 
   WHERE is_active = true 
   ORDER BY market_cap DESC 
   LIMIT 50
   
2. 取得最新交易日
   SELECT MAX(calculation_date) FROM technical_indicators
   
3. 對每檔熱門股票：
   FOR EACH hot_stock:
     3.1 查詢最新指標
         SELECT * FROM technical_indicators 
         WHERE stock_id = hot_stock 
           AND calculation_date = latest_date
     
     3.2 序列化為 JSON
     
     3.3 寫入 Redis
         Key: tech:indicators:{stock_id}:{date}
         Value: JSON
         TTL: 1 小時
   
4. 預熱指標定義
   SELECT * FROM indicator_definitions WHERE is_active = true
   Redis.set("tech:config:indicators", JSON, TTL=永久)
   
5. 記錄預熱統計
```

---

#### JOB-M07-007: 歷史指標回填

**Job 資訊**:
- **Job ID**: JOB-M07-007
- **Job 名稱**: 歷史指標回填
- **執行頻率**: 手動觸發
- **執行時間**: 不定
- **預估執行時間**: 依資料量而定（可能數小時）
- **優先級**: P2

**使用場景**:
- 系統首次上線，需回填歷史資料
- 新股上市，需計算過去的指標
- 指標計算邏輯更新，需重新計算

**執行流程**:
```
1. 接收參數
   {
     "start_date": "2020-01-01",
     "end_date": "2024-12-24",
     "stock_ids": ["2330", "2317"] or null (全部股票),
     "indicator_priority": "P0" or null (全部指標)
   }
   
2. 建立回填 Job 清單
   FOR date IN date_range(start_date, end_date):
     IF 該日為交易日:
       建立子 Job（類似 JOB-M07-001）
   
3. 依序執行子 Job
   - 避免並行執行過多導致資源耗盡
   - 每執行 10 個子 Job 暫停 30 秒
   
4. 監控進度
   - 記錄已完成天數
   - 預估剩餘時間
   
5. 完成後驗證
   - 檢查資料完整性
   - 統計缺失記錄
```

---

### 6.3 Job 調度與依賴管理

**Job 依賴關係圖**:
```
JOB-M07-001 (基礎指標)
    ├─→ JOB-M07-002 (進階指標)
    ├─→ JOB-M07-004 (交叉信號)
    └─→ JOB-M07-005 (超買超賣)

JOB-M07-003 (專業指標) - 獨立執行

JOB-M07-006 (快取預熱) - 獨立執行

JOB-M07-007 (歷史回填) - 手動觸發
```

**調度策略**:
- 使用 Spring Boot `@Scheduled` 註解
- 或使用 Quartz Scheduler（更複雜場景）
- Job 執行狀態記錄至 `indicator_calculation_jobs` 表

**冪等性保證**（總綱 4.5 要求）:
- 所有 Job 使用 `calculation_date` 參數
- 資料庫使用 `ON CONFLICT ... DO UPDATE`
- 重複執行同一 Job 不產生副作用
- 支援安全重試

---


---

## 📚 相關文檔

- [全系統 Job 模型](../specs/technical/00-全系統契約.md#45-job-模型)
- [M07 功能需求](../specs/functional/M07-技術分析功能需求.md)
- [環境配置說明](./環境配置說明.md)

---

**文件維護者**: DevOps 工程師  
**最後更新**: 2025-12-31
