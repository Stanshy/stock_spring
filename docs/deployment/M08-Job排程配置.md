# M08-基本面分析模組 Job 排程配置

> **文件編號**: JOB-M08  
> **模組名稱**: 基本面分析模組  
> **版本**: v2.0  
> **最後更新**: 2025-12-31  
> **狀態**: Draft

---

## 📋 Job 排程總覽

本文件定義 基本面分析模組的所有批次作業與排程配置。

---

## 6. Job/排程設計

### 6.1 Job 清單

| Job 編號 | Job 名稱 | 執行頻率 | 優先級 | 說明 |
|---------|---------|---------|-------|------|
| JOB-M08-001 | 財務指標計算 | 每週一 09:00 | P0 | 計算最新一季財務指標 |
| JOB-M08-002 | 綜合評分計算 | 每週一 09:30 | P0 | 計算 Piotroski、Altman 等評分 |
| JOB-M08-003 | 財務異常偵測 | 每週一 10:00 | P0 | 偵測財務異常並產生警示 |
| JOB-M08-004 | 歷史趨勢更新 | 每週一 11:00 | P1 | 更新指標歷史趨勢資料 |
| JOB-M08-005 | 快取預熱 | 每週一 11:30 | P1 | 熱門股票財務指標快取預熱 |
| JOB-M08-006 | 過期警示清理 | 每週日 02:00 | P2 | 清理已解決的舊警示記錄 |

### 6.2 核心 Job 設計

#### JOB-M08-001: 財務指標計算

**觸發方式**: 
- 定期排程：每週一 09:00
- 事件觸發：收到 M06 FinancialStatementUpdated 事件時立即執行

**Cron 表達式**: `0 0 9 * * MON`

**執行邏輯**:
1. 建立 Job 執行記錄（job_executions 表，status='RUNNING'）
2. 查詢最近一週新增或更新的財報資料
   - SELECT stock_id, year, quarter FROM financial_statements WHERE updated_at > :last_job_time
3. 批次處理（每批 20 檔股票）
4. 對每檔股票執行指標計算：
   - 從 financial_statements 表讀取財報資料
   - 從 stock_prices 表查詢股價（財報公告日）
   - 查詢歷史財報（用於計算成長率）
   - 計算 8 大類指標（估值、獲利、財務結構、償債、效率、現金流、成長、股利）
   - 驗證計算結果
   - 使用 MyBatis 批次 UPSERT 儲存至 fundamental_indicators 表
5. 更新 Redis 快取（熱門股票前 50 檔）
6. 記錄處理統計（成功、失敗、跳過）
7. 更新 Job 執行記錄（status='SUCCESS', 統計資訊）

**資料來源**:
- financial_statements 表（M06）
- stock_prices 表（M06）
- stocks 表（M06）

**冪等性設計**（遵守總綱 4.5 Job 模型）:
- 唯一鍵: (stock_id, year, quarter, report_type)
- 使用 UPSERT 機制（ON CONFLICT DO UPDATE）
- 若財報被修正，重新計算會覆蓋舊值
- 重複執行結果一致

**失敗處理**:
- 單一股票計算失敗不影響其他股票
- 記錄失敗原因至 data_quality_issues 表
- Job 整體失敗後觸發重試（最多 3 次）
- 連續失敗 3 次後發送告警

**效能優化**:
- 批次查詢財報資料（避免 N+1 查詢）
- 批次 UPSERT（每批 20 筆）
- 使用 MyBatis 動態 SQL 優化查詢
- 並行處理（5 個工作執行緒）

**執行時長估算**:
- 單檔股票：約 0.5 秒
- 1000 檔股票：約 8-10 分鐘
- 2000 檔股票：約 15-20 分鐘

---

#### JOB-M08-002: 綜合評分計算

**Cron 表達式**: `0 30 9 * * MON`

**執行邏輯**:
1. 查詢已完成指標計算的股票清單
   - SELECT stock_id, year, quarter FROM fundamental_indicators WHERE calculation_date = CURRENT_DATE
2. 對每檔股票計算綜合評分：
   - Piotroski F-Score（需查詢去年同期資料比較）
   - Altman Z-Score
   - Beneish M-Score（需查詢去年同期資料）
   - Graham Score
   - 自訂綜合評分（加權計算）
3. 使用 JPA 批次儲存至 financial_scores 表
4. 更新 Redis 快取
5. 記錄執行結果

**冪等性設計**:
- 唯一鍵: (stock_id, year, quarter)
- 使用 UPSERT 機制

**資料依賴**:
- JOB-M08-001 必須先執行完成
- 需要當季與去年同季的財務指標

**計算邏輯**:

**Piotroski F-Score**:
```
獲利能力 (4分):
1. ROA > 0 → +1
2. 營運現金流 > 0 → +1
3. ROA 增加（相較去年同季）→ +1
4. 營運現金流 > 淨利 → +1

財務結構/槓桿 (3分):
5. 長期負債減少（相較去年同季）→ +1
6. 流動比率增加（相較去年同季）→ +1
7. 未發行新股稀釋（流通股數不增加）→ +1

經營效率 (2分):
8. 毛利率增加（相較去年同季）→ +1
9. 總資產週轉率增加（相較去年同季）→ +1

總分: 0-9 分
- 8-9 分: 優秀
- 6-7 分: 良好
- 4-5 分: 普通
- 0-3 分: 較差
```

**Altman Z-Score**:
```
Z = 1.2 × X1 + 1.4 × X2 + 3.3 × X3 + 0.6 × X4 + 1.0 × X5

X1 = 營運資金 / 總資產
X2 = 保留盈餘 / 總資產
X3 = 稅前息前盈餘(EBIT) / 總資產
X4 = 股東權益市值 / 總負債
X5 = 營收 / 總資產

判斷:
- Z > 2.99: SAFE（安全區，破產風險低）
- 1.81 < Z < 2.99: GREY（灰色地帶）
- Z < 1.81: DISTRESS（財務困境區，破產風險高）
```

**Beneish M-Score**:
```
M = -4.84 + 0.92×DSRI + 0.528×GMI + 0.404×AQI + 0.892×SGI 
    + 0.115×DEPI - 0.172×SGAI + 4.679×TATA - 0.327×LVGI

其中:
DSRI = 應收帳款比率指數
GMI = 毛利率指數
AQI = 資產品質指數
SGI = 營收成長指數
DEPI = 折舊指數
SGAI = 銷管費用指數
TATA = 總應計項目
LVGI = 槓桿指數

判斷:
- M < -2.22: CLEAN（盈餘品質良好）
- -2.22 < M < -1.78: WARNING（需注意）
- M > -1.78: MANIPULATOR（可能會計操縱）
```

---

#### JOB-M08-003: 財務異常偵測

**Cron 表達式**: `0 0 10 * * MON`

**執行邏輯**:
1. 查詢已完成指標計算的股票清單
2. 對每檔股票執行異常偵測規則：
   - 盈餘品質檢查（應計項目比率、現金流與淨利比較）
   - 負債風險檢查（負債比、負債權益比、利息保障倍數）
   - 流動性風險檢查（流動比、速動比、現金比）
   - 獲利能力惡化檢查（ROE 趨勢、毛利率趨勢）
3. 若偵測到異常：
   - INSERT INTO financial_alerts（記錄警示）
   - 產生信號（遵守總綱 4.2 Signal Contract）
   - 發布 FinancialAlertDetected 事件至 M13
4. 記錄執行結果

**冪等性設計**:
- 同一股票、同一季度、同一異常類型只產生一次警示
- 若異常已存在且狀態為 ACTIVE，跳過重複警示

**異常偵測規則**:

**規則 1: 應計項目比率過高**
```sql
應計項目比率 = (淨利 - 營運現金流) / 總資產

IF 應計項目比率 > 10% THEN
  INSERT INTO financial_alerts (
    alert_type = 'HIGH_ACCRUAL_RATIO',
    alert_category = 'EARNINGS_QUALITY',
    severity = 'HIGH',
    alert_message = '應計項目比率過高，可能存在會計操縱風險'
  )
END IF
```

**規則 2: 負債比率過高**
```sql
IF 負債比 > 70% THEN
  INSERT INTO financial_alerts (
    alert_type = 'HIGH_DEBT_RATIO',
    alert_category = 'DEBT_RISK',
    severity = 'HIGH',
    alert_message = '負債比率過高，財務風險較大'
  )
ELSIF 負債比 > 60% THEN
  INSERT INTO financial_alerts (
    severity = 'MEDIUM'
  )
END IF
```

**規則 3: 流動比率過低**
```sql
IF 流動比 < 1.0 THEN
  INSERT INTO financial_alerts (
    alert_type = 'LOW_CURRENT_RATIO',
    alert_category = 'LIQUIDITY_RISK',
    severity = 'HIGH',
    alert_message = '流動比率小於1，短期償債能力不足'
  )
END IF
```

**規則 4: 自由現金流連續為負**
```sql
-- 查詢最近 2 季 FCF
SELECT fcf FROM fundamental_indicators 
WHERE stock_id = :stock_id 
ORDER BY year DESC, quarter DESC 
LIMIT 2

IF 兩季 FCF 皆 < 0 THEN
  INSERT INTO financial_alerts (
    alert_type = 'NEGATIVE_FCF_TREND',
    alert_category = 'EARNINGS_QUALITY',
    severity = 'MEDIUM',
    alert_message = '自由現金流連續2季為負，公司持續燒錢'
  )
END IF
```

**規則 5: ROE 持續惡化**
```sql
-- 查詢最近 2 季 ROE
SELECT roe FROM fundamental_indicators 
WHERE stock_id = :stock_id 
ORDER BY year DESC, quarter DESC 
LIMIT 2

IF ROE[0] < ROE[1] AND ROE[1] < ROE[2] AND ROE[0] < 10 THEN
  INSERT INTO financial_alerts (
    alert_type = 'ROE_DETERIORATION',
    alert_category = 'PROFITABILITY_DECLINE',
    severity = 'MEDIUM',
    alert_message = 'ROE連續下降且低於10%，獲利能力惡化'
  )
END IF
```

---

#### JOB-M08-004: 歷史趨勢更新

**Cron 表達式**: `0 0 11 * * MON`

**執行邏輯**:
1. 查詢已完成指標計算的股票清單
2. 對每檔股票更新指標歷史趨勢：
   - 查詢最近 20 季財務指標
   - 計算趨勢方向（上升、下降、震盪）
   - 計算波動率（標準差）
   - 計算最大值、最小值、平均值
3. 更新 Redis 快取（趨勢資料）
4. 記錄執行結果

**趨勢計算邏輯**:
```
對於每個指標（如 ROE）:
1. 查詢最近 20 季資料
2. 計算線性迴歸斜率
   - 斜率 > 0.5 → 上升趨勢
   - 斜率 < -0.5 → 下降趨勢
   - -0.5 <= 斜率 <= 0.5 → 震盪
3. 計算標準差（波動率）
4. 計算統計值（min, max, avg, median）
5. 更新 Redis:
   - Key: fund:trend:{stock_id}:{indicator}:20q
   - Value: {trend, volatility, statistics}
   - TTL: 7 天
```

---

#### JOB-M08-005: 快取預熱

**Cron 表達式**: `0 30 11 * * MON`

**執行邏輯**:
1. 查詢熱門股票清單（市值前 50 檔）
2. 對每檔熱門股票：
   - 載入最新一季財務指標
   - 載入最新綜合評分
   - 載入指標歷史趨勢（最近 20 季）
3. 批次寫入 Redis
4. 設定 TTL（24 小時）
5. 記錄快取命中率統計

**快取 Key 設計**:
```
fund:indicators:{stock_id}:{year}:{quarter}        → 單季所有指標
fund:score:{stock_id}:{year}:{quarter}             → 綜合評分
fund:trend:{stock_id}:{indicator}:20q              → 20季趨勢
fund:hot:stocks                                    → 熱門股票清單
```

**預熱策略**:
- 優先載入市值前 50 檔股票
- 包含最新 4 季資料（支援 QoQ、YoY 比較）
- 包含常用指標的 20 季歷史趨勢

---

#### JOB-M08-006: 過期警示清理

**Cron 表達式**: `0 0 2 * * SUN`（每週日 02:00）

**執行邏輯**:
1. 查詢已解決且超過 90 天的警示記錄
2. 批次刪除這些記錄
3. 記錄清理統計

**SQL 邏輯**:
```sql
-- 刪除已解決且超過 90 天的警示
DELETE FROM financial_alerts
WHERE alert_status = 'RESOLVED'
  AND resolved_at < CURRENT_TIMESTAMP - INTERVAL '90 days';

-- 刪除被忽略且超過 180 天的警示
DELETE FROM financial_alerts
WHERE alert_status = 'IGNORED'
  AND created_at < CURRENT_TIMESTAMP - INTERVAL '180 days';
```

---

### 6.3 Job 依賴關係

```
每週一:
09:00 - CALCULATE_FUNDAMENTALS (JOB-M08-001)
          ↓
09:30 - CALCULATE_SCORES (JOB-M08-002)
          ↓
10:00 - DETECT_FINANCIAL_ALERTS (JOB-M08-003)
          ↓
11:00 - UPDATE_TRENDS (JOB-M08-004)
          ↓
11:30 - WARM_UP_CACHE (JOB-M08-005)

每週日:
02:00 - CLEANUP_OLD_ALERTS (JOB-M08-006)
```

**關鍵依賴**:
- JOB-M08-002 依賴 JOB-M08-001（需要指標計算完成）
- JOB-M08-003 依賴 JOB-M08-001（需要指標計算完成）
- JOB-M08-004 依賴 JOB-M08-001（需要指標計算完成）
- JOB-M08-005 依賴 JOB-M08-001, JOB-M08-002（需要指標與評分完成）

---

### 6.4 Job 監控與告警

**監控指標**:
| 指標 | 閾值 | 告警等級 | 處理方式 |
|-----|------|---------|---------|
| Job 執行時長 | > 30 分鐘 | WARNING | 記錄慢查詢，優化批次大小 |
| Job 失敗率 | > 5% | ERROR | 立即告警，檢查資料品質 |
| Job 連續失敗 | >= 3 次 | CRITICAL | 立即告警並暫停，人工介入 |
| 計算失敗股票數 | > 100 檔 | WARNING | 檢查資料來源 |
| 財務異常警示數 | > 50 筆（HIGH以上） | WARNING | 檢視異常股票清單 |

**告警通道**（透過 M15 告警系統）:
- Email
- Slack
- SMS（CRITICAL 等級）

---


---

## 📚 相關文檔

- [全系統 Job 模型](../specs/technical/00-全系統契約.md#45-job-模型)
- [M08 功能需求](../specs/functional/M08-基本面分析功能需求.md)
- [環境配置說明](./環境配置說明.md)

---

**文件維護者**: DevOps 工程師  
**最後更新**: 2025-12-31
