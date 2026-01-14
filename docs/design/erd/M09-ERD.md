# M09-籌碼分析模組 ERD

> **文件編號**: ERD-M09
> **模組名稱**: 籌碼分析模組
> **版本**: v1.0
> **最後更新**: 2026-01-11
> **狀態**: Draft

---

## 1. ERD 圖 (Mermaid)

```mermaid
erDiagram
    %% M06 資料來源表（外部依賴）
    stocks {
        varchar stock_id PK "股票代碼"
        varchar stock_name "股票名稱"
        varchar market_type "市場類型"
        varchar industry "產業別"
        bigint issued_shares "發行股數"
        boolean is_active "是否活躍"
    }

    institutional_trading {
        bigint trading_id PK "交易ID"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        bigint foreign_buy "外資買進"
        bigint foreign_sell "外資賣出"
        bigint foreign_net "外資買賣超(計算欄位)"
        bigint trust_buy "投信買進"
        bigint trust_sell "投信賣出"
        bigint trust_net "投信買賣超(計算欄位)"
        bigint dealer_buy "自營商買進"
        bigint dealer_sell "自營商賣出"
        bigint dealer_net "自營商買賣超(計算欄位)"
        bigint total_net "合計買賣超(計算欄位)"
    }

    margin_trading {
        bigint margin_id PK "融資融券ID"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        bigint margin_balance "融資餘額"
        bigint margin_quota "融資限額"
        numeric margin_usage_rate "融資使用率(計算欄位)"
        bigint short_balance "融券餘額"
        bigint short_quota "融券限額"
        numeric short_usage_rate "融券使用率(計算欄位)"
    }

    stock_prices {
        bigint price_id PK "價格ID"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        numeric open_price "開盤價"
        numeric high_price "最高價"
        numeric low_price "最低價"
        numeric close_price "收盤價"
        bigint volume "成交量"
    }

    %% M09 自有資料表
    chip_analysis_results {
        bigint result_id PK "結果ID"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        bigint foreign_net "外資買賣超"
        numeric foreign_net_ma5 "外資MA5"
        numeric foreign_net_ma20 "外資MA20"
        int foreign_continuous_days "外資連續天數"
        bigint trust_net "投信買賣超"
        bigint dealer_net "自營商買賣超"
        bigint total_net "合計買賣超"
        bigint margin_balance "融資餘額"
        numeric margin_usage_rate "融資使用率"
        numeric margin_short_ratio "券資比"
        numeric institutional_ratio "法人持股比"
        varchar concentration_trend "集中度趨勢"
        jsonb institutional_indicators "法人指標JSONB"
        jsonb margin_indicators "融資指標JSONB"
        int chip_score "籌碼評分"
        varchar chip_grade "籌碼等級"
    }

    chip_signals {
        bigint signal_id PK "訊號ID"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        varchar signal_code "訊號代碼"
        varchar signal_name "訊號名稱"
        varchar signal_type "訊號類型"
        varchar severity "嚴重度"
        numeric signal_value "訊號數值"
        numeric threshold_value "門檻值"
        text description "描述"
        boolean is_active "是否有效"
    }

    chip_rankings_cache {
        bigint cache_id PK "快取ID"
        varchar rank_type "排行榜類型"
        date trade_date "交易日期"
        varchar market_type "市場類型"
        jsonb rankings "排行榜內容"
        timestamp expires_at "過期時間"
    }

    chip_cost_estimation {
        bigint estimation_id PK "估算ID"
        varchar stock_id FK "股票代碼"
        date estimation_date "估算日期"
        numeric foreign_avg_cost "外資平均成本"
        numeric foreign_profit_rate "外資報酬率(計算欄位)"
        numeric trust_avg_cost "投信平均成本"
        numeric trust_profit_rate "投信報酬率(計算欄位)"
        numeric current_price "當前價格"
        int lookback_days "回溯天數"
    }

    %% 關聯關係
    stocks ||--o{ institutional_trading : "has"
    stocks ||--o{ margin_trading : "has"
    stocks ||--o{ stock_prices : "has"
    stocks ||--o{ chip_analysis_results : "has"
    stocks ||--o{ chip_signals : "has"
    stocks ||--o{ chip_cost_estimation : "has"

    institutional_trading ||--o{ chip_analysis_results : "計算來源"
    margin_trading ||--o{ chip_analysis_results : "計算來源"
    stock_prices ||--o{ chip_cost_estimation : "成本計算"

    chip_analysis_results ||--o{ chip_signals : "產生"
```

---

## 2. 資料表關聯說明

### 2.1 M06 → M09 依賴關係

| 來源表 (M06) | 目標表 (M09) | 關聯類型 | 說明 |
|-------------|-------------|---------|------|
| institutional_trading | chip_analysis_results | 計算依賴 | 三大法人指標計算來源 |
| margin_trading | chip_analysis_results | 計算依賴 | 融資融券指標計算來源 |
| stock_prices | chip_cost_estimation | 計算依賴 | 成本估算需要價格資料 |
| stocks | 所有 M09 表 | 外鍵關聯 | 股票主表 |

### 2.2 M09 內部關聯

| 來源表 | 目標表 | 關聯類型 | 說明 |
|-------|-------|---------|------|
| chip_analysis_results | chip_signals | 產生關係 | 分析結果觸發異常訊號 |

---

## 3. 實體屬性詳細說明

### 3.1 chip_analysis_results

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| result_id | BIGSERIAL | PK | 自增主鍵 |
| stock_id | VARCHAR(10) | FK, NOT NULL | 股票代碼 |
| trade_date | DATE | NOT NULL | 交易日期（分區鍵） |
| foreign_net | BIGINT | | 外資買賣超股數 |
| foreign_net_ma5 | NUMERIC(15,2) | | 外資買賣超 5 日均線 |
| foreign_net_ma20 | NUMERIC(15,2) | | 外資買賣超 20 日均線 |
| foreign_continuous_days | INTEGER | | 外資連續買超天數 |
| trust_net | BIGINT | | 投信買賣超股數 |
| dealer_net | BIGINT | | 自營商買賣超股數 |
| total_net | BIGINT | | 三大法人合計買賣超 |
| margin_balance | BIGINT | | 融資餘額 |
| margin_usage_rate | NUMERIC(5,2) | | 融資使用率 |
| margin_short_ratio | NUMERIC(5,2) | | 券資比 |
| institutional_ratio | NUMERIC(5,2) | | 法人持股比例估算 |
| concentration_trend | VARCHAR(20) | | 籌碼集中趨勢 |
| institutional_indicators | JSONB | | 法人詳細指標 |
| margin_indicators | JSONB | | 融資融券詳細指標 |
| chip_score | INTEGER | | 籌碼評分 (0-100) |
| chip_grade | VARCHAR(2) | | 籌碼等級 (A/B/C/D/F) |

**唯一約束**: (stock_id, trade_date)

### 3.2 chip_signals

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| signal_id | BIGSERIAL | PK | 自增主鍵 |
| stock_id | VARCHAR(10) | FK, NOT NULL | 股票代碼 |
| trade_date | DATE | NOT NULL | 訊號日期 |
| signal_code | VARCHAR(20) | NOT NULL | 訊號代碼 |
| signal_name | VARCHAR(50) | NOT NULL | 訊號名稱 |
| signal_type | VARCHAR(20) | NOT NULL | 訊號類型 |
| severity | VARCHAR(10) | NOT NULL | 嚴重度 |
| signal_value | NUMERIC(20,2) | | 觸發訊號的數值 |
| threshold_value | NUMERIC(20,2) | | 門檻值 |
| deviation | NUMERIC(10,2) | | 偏離程度（標準差） |
| description | TEXT | | 訊號描述 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否有效 |

### 3.3 chip_rankings_cache

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| cache_id | BIGSERIAL | PK | 自增主鍵 |
| rank_type | VARCHAR(30) | NOT NULL | 排行榜類型 |
| trade_date | DATE | NOT NULL | 交易日期 |
| market_type | VARCHAR(10) | | 市場類型 |
| rankings | JSONB | NOT NULL | 排行榜內容 |
| total_count | INTEGER | | 總筆數 |
| expires_at | TIMESTAMP | | 快取過期時間 |

**唯一約束**: (rank_type, trade_date, market_type)

### 3.4 chip_cost_estimation

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| estimation_id | BIGSERIAL | PK | 自增主鍵 |
| stock_id | VARCHAR(10) | FK, NOT NULL | 股票代碼 |
| estimation_date | DATE | NOT NULL | 估算日期 |
| foreign_avg_cost | NUMERIC(10,2) | | 外資平均成本 |
| foreign_profit_rate | NUMERIC(5,2) | GENERATED | 外資報酬率 |
| trust_avg_cost | NUMERIC(10,2) | | 投信平均成本 |
| trust_profit_rate | NUMERIC(5,2) | GENERATED | 投信報酬率 |
| current_price | NUMERIC(10,2) | NOT NULL | 當前價格 |
| lookback_days | INTEGER | DEFAULT 120 | 回溯天數 |

**唯一約束**: (stock_id, estimation_date)

---

## 4. 索引設計

### 4.1 chip_analysis_results 索引

```sql
-- 主要查詢索引
CREATE INDEX idx_chip_results_stock_id ON chip_analysis_results(stock_id);
CREATE INDEX idx_chip_results_trade_date ON chip_analysis_results(trade_date);

-- 排行榜查詢索引
CREATE INDEX idx_chip_results_foreign_net ON chip_analysis_results(foreign_net);
CREATE INDEX idx_chip_results_total_net ON chip_analysis_results(total_net);
CREATE INDEX idx_chip_results_chip_score ON chip_analysis_results(chip_score);

-- JSONB GIN 索引
CREATE INDEX idx_chip_inst_indicators ON chip_analysis_results USING GIN(institutional_indicators);
CREATE INDEX idx_chip_margin_indicators ON chip_analysis_results USING GIN(margin_indicators);
```

### 4.2 chip_signals 索引

```sql
-- 主要查詢索引
CREATE INDEX idx_chip_signals_stock_id ON chip_signals(stock_id);
CREATE INDEX idx_chip_signals_trade_date ON chip_signals(trade_date);
CREATE INDEX idx_chip_signals_severity ON chip_signals(severity);

-- 複合索引
CREATE INDEX idx_chip_signals_date_severity ON chip_signals(trade_date, severity);
CREATE INDEX idx_chip_signals_stock_date ON chip_signals(stock_id, trade_date);
```

---

## 📚 相關文檔

- [M09 資料庫設計](../M09-資料庫設計.md)
- [M09 功能需求](../../specs/functional/M09-籌碼分析功能需求.md)
- [M06 ERD](./M06-ERD.md)

---

**文件維護者**: 資料庫架構師
**最後更新**: 2026-01-11
**下次審核**: 2026-03-31
