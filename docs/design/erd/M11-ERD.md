# M11-量化策略模組 ERD

> **文件編號**: ERD-M11
> **模組名稱**: 量化策略模組
> **版本**: v1.0
> **最後更新**: 2026-01-14
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
        varchar security_type "證券類型"
        boolean is_active "是否活躍"
    }

    stock_prices {
        bigint price_id PK "價格ID"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        numeric close_price "收盤價"
        bigint volume "成交量"
        numeric volume_ma20 "20日均量"
    }

    %% M07 技術指標（外部依賴）
    technical_indicators {
        bigint indicator_id PK "指標ID"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        numeric rsi_14 "RSI(14)"
        numeric macd_histogram "MACD柱狀"
        numeric kd_k "K值"
        numeric kd_d "D值"
        numeric ma5 "MA5"
        numeric ma20 "MA20"
        numeric bollinger_upper "布林上軌"
        numeric bollinger_lower "布林下軌"
    }

    %% M08 財務指標（外部依賴）
    fundamental_indicators {
        bigint indicator_id PK "指標ID"
        varchar stock_id FK "股票代碼"
        date report_date "報告日期"
        numeric pe_ratio "本益比"
        numeric pb_ratio "股價淨值比"
        numeric roe "股東權益報酬率"
        numeric eps "每股盈餘"
        numeric dividend_yield "殖利率"
        numeric revenue_growth_yoy "營收年增率"
    }

    %% M09 籌碼分析（外部依賴）
    chip_analysis_results {
        bigint result_id PK "結果ID"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        bigint foreign_net "外資買賣超"
        int foreign_continuous_days "外資連續天數"
        bigint trust_net "投信買賣超"
        bigint dealer_net "自營商買賣超"
        bigint margin_change "融資增減"
        numeric chip_score "籌碼評分"
    }

    %% M11 自有資料表
    strategies {
        varchar strategy_id PK "策略ID"
        varchar strategy_name "策略名稱"
        varchar strategy_type "策略類型"
        text description "策略描述"
        int current_version "當前版本"
        varchar status "狀態"
        boolean is_preset "是否預設"
        jsonb conditions "策略條件JSONB"
        jsonb parameters "策略參數JSONB"
        jsonb output_config "輸出配置JSONB"
        int total_executions "總執行次數"
        int total_signals "總信號數"
        timestamp last_execution_at "最後執行時間"
        varchar created_by "建立者"
    }

    strategy_versions {
        bigint version_id PK "版本ID"
        varchar strategy_id FK "策略ID"
        int version "版本號"
        varchar strategy_name "策略名稱"
        jsonb conditions "策略條件快照"
        jsonb parameters "策略參數快照"
        text change_summary "變更摘要"
        varchar created_by "建立者"
    }

    strategy_executions {
        varchar execution_id PK "執行ID"
        varchar strategy_id FK "策略ID"
        int strategy_version "策略版本"
        date execution_date "執行日期"
        varchar execution_type "執行類型"
        jsonb stock_universe "股票範圍"
        int stocks_evaluated "評估股票數"
        int signals_generated "產生信號數"
        int buy_signals "買進信號數"
        int sell_signals "賣出信號數"
        numeric avg_confidence "平均信心度"
        int execution_time_ms "執行時間ms"
        varchar status "執行狀態"
        jsonb diagnostics "診斷資訊"
    }

    strategy_signals {
        varchar signal_id PK "信號ID"
        varchar execution_id FK "執行ID"
        varchar strategy_id FK "策略ID"
        int strategy_version "策略版本"
        varchar stock_id FK "股票代碼"
        date trade_date "交易日期"
        varchar signal_type "信號類型"
        numeric confidence_score "信心度分數"
        jsonb matched_conditions "匹配條件JSONB"
        jsonb factor_values "因子值JSONB"
        boolean is_consumed "是否已消費"
        varchar consumed_by "消費者"
        timestamp consumed_at "消費時間"
    }

    strategy_optimizations {
        varchar optimization_id PK "優化ID"
        varchar strategy_id FK "策略ID"
        int strategy_version "策略版本"
        varchar optimization_method "優化方法"
        varchar objective_function "目標函數"
        jsonb parameter_config "參數配置"
        jsonb backtest_config "回測配置"
        varchar status "執行狀態"
        int total_combinations "總組合數"
        jsonb best_parameters "最佳參數"
        numeric best_objective_value "最佳目標值"
        jsonb all_results "所有結果"
    }

    factor_metadata {
        varchar factor_id PK "因子ID"
        varchar factor_name "因子名稱"
        varchar display_name "顯示名稱"
        varchar category "因子類別"
        varchar source_module "來源模組"
        varchar data_type "資料類型"
        jsonb value_range "值範圍"
        jsonb typical_thresholds "典型閾值"
        jsonb supported_operators "支援運算子"
        text description "因子說明"
        boolean is_active "是否啟用"
    }

    %% 關聯關係 - M11 內部
    strategies ||--o{ strategy_versions : "has versions"
    strategies ||--o{ strategy_executions : "has executions"
    strategies ||--o{ strategy_optimizations : "has optimizations"
    strategy_executions ||--o{ strategy_signals : "generates"

    %% 關聯關係 - 外部依賴（因子數據讀取）
    stocks ||--o{ stock_prices : "has"
    stocks ||--o{ technical_indicators : "has"
    stocks ||--o{ fundamental_indicators : "has"
    stocks ||--o{ chip_analysis_results : "has"
    stocks ||--o{ strategy_signals : "receives"

    %% 因子數據依賴（讀取關係）
    technical_indicators ||--o{ strategy_signals : "provides factors"
    fundamental_indicators ||--o{ strategy_signals : "provides factors"
    chip_analysis_results ||--o{ strategy_signals : "provides factors"
```

---

## 2. 資料表關聯說明

### 2.1 M11 內部關聯

| 來源表 | 目標表 | 關聯類型 | 說明 |
|-------|-------|---------|------|
| strategies | strategy_versions | 1:N | 策略擁有多個版本歷史 |
| strategies | strategy_executions | 1:N | 策略擁有多次執行記錄 |
| strategies | strategy_optimizations | 1:N | 策略擁有多次優化記錄 |
| strategy_executions | strategy_signals | 1:N | 每次執行產生多個信號 |

### 2.2 上游依賴關係（只讀）

| 來源表 (上游) | 目標用途 (M11) | 關聯類型 | 說明 |
|-------------|---------------|---------|------|
| stocks (M06) | 股票篩選 | 參考 | 取得股票基本資訊 |
| stock_prices (M06) | 價量因子 | 讀取 | 取得價格、成交量數據 |
| technical_indicators (M07) | 技術面因子 | 讀取 | RSI、MACD、MA 等指標 |
| fundamental_indicators (M08) | 基本面因子 | 讀取 | PE、ROE、EPS 等指標 |
| chip_analysis_results (M09) | 籌碼面因子 | 讀取 | 法人買賣超、融資融券等 |

### 2.3 下游消費關係

| 來源表 (M11) | 消費者 (下游) | 說明 |
|-------------|-------------|------|
| strategy_signals | M13 信號引擎 | M13 讀取 strategy_signals 進行信號整合 |

---

## 3. 實體屬性詳細說明

### 3.1 strategies (策略定義表)

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| strategy_id | VARCHAR(20) | PK | 策略唯一識別碼 |
| strategy_name | VARCHAR(100) | NOT NULL | 策略名稱 |
| strategy_type | VARCHAR(20) | NOT NULL | 策略類型（MOMENTUM/VALUE/HYBRID/CUSTOM） |
| description | TEXT | | 策略描述 |
| current_version | INTEGER | DEFAULT 1 | 當前版本號 |
| status | VARCHAR(20) | DEFAULT 'DRAFT' | 狀態（DRAFT/ACTIVE/INACTIVE/ARCHIVED） |
| is_preset | BOOLEAN | DEFAULT FALSE | 是否為系統預設策略 |
| conditions | JSONB | NOT NULL | 策略條件定義 |
| parameters | JSONB | DEFAULT '{}' | 可調整參數 |
| output_config | JSONB | DEFAULT '{}' | 輸出配置（信號類型、信心度公式） |
| total_executions | INTEGER | DEFAULT 0 | 累計執行次數 |
| total_signals | INTEGER | DEFAULT 0 | 累計產生信號數 |
| last_execution_at | TIMESTAMP | | 最後執行時間 |
| created_by | VARCHAR(50) | | 建立者 |

### 3.2 strategy_versions (策略版本歷史表)

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| version_id | BIGSERIAL | PK | 自增主鍵 |
| strategy_id | VARCHAR(20) | FK, NOT NULL | 策略 ID |
| version | INTEGER | NOT NULL | 版本號 |
| strategy_name | VARCHAR(100) | NOT NULL | 版本名稱快照 |
| conditions | JSONB | NOT NULL | 條件快照 |
| parameters | JSONB | DEFAULT '{}' | 參數快照 |
| change_summary | TEXT | | 變更摘要 |
| created_by | VARCHAR(50) | | 建立者 |

**唯一約束**: (strategy_id, version)

### 3.3 strategy_executions (策略執行記錄表)

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| execution_id | VARCHAR(30) | PK | 執行 ID |
| strategy_id | VARCHAR(20) | FK, NOT NULL | 策略 ID |
| strategy_version | INTEGER | NOT NULL | 執行時的策略版本 |
| execution_date | DATE | NOT NULL | 執行日期 |
| execution_type | VARCHAR(20) | DEFAULT 'SCHEDULED' | 執行類型 |
| stock_universe | JSONB | | 股票範圍配置 |
| stocks_evaluated | INTEGER | | 評估的股票數量 |
| signals_generated | INTEGER | DEFAULT 0 | 產生的信號數 |
| buy_signals | INTEGER | DEFAULT 0 | 買進信號數 |
| sell_signals | INTEGER | DEFAULT 0 | 賣出信號數 |
| avg_confidence | NUMERIC(5,2) | | 平均信心度 |
| execution_time_ms | INTEGER | | 執行耗時（毫秒） |
| status | VARCHAR(20) | DEFAULT 'RUNNING' | 執行狀態 |
| diagnostics | JSONB | DEFAULT '{}' | 診斷資訊 |

### 3.4 strategy_signals (策略信號表)

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| signal_id | VARCHAR(30) | PK（複合） | 信號 ID |
| execution_id | VARCHAR(30) | NOT NULL | 執行 ID |
| strategy_id | VARCHAR(20) | NOT NULL | 策略 ID |
| strategy_version | INTEGER | NOT NULL | 策略版本 |
| stock_id | VARCHAR(10) | NOT NULL | 股票代碼 |
| trade_date | DATE | PK（複合） | 交易日期（分區鍵） |
| signal_type | VARCHAR(10) | NOT NULL | 信號類型（BUY/SELL/HOLD） |
| confidence_score | NUMERIC(5,2) | | 信心度分數 |
| matched_conditions | JSONB | NOT NULL | 匹配的條件詳情 |
| factor_values | JSONB | | 因子值快照 |
| is_consumed | BOOLEAN | DEFAULT FALSE | 是否已被 M13 消費 |
| consumed_by | VARCHAR(30) | | 消費者標識 |
| consumed_at | TIMESTAMP | | 消費時間 |

**分區鍵**: trade_date（按月分區）

### 3.5 strategy_optimizations (參數優化記錄表)

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| optimization_id | VARCHAR(30) | PK | 優化 ID |
| strategy_id | VARCHAR(20) | FK, NOT NULL | 策略 ID |
| strategy_version | INTEGER | NOT NULL | 策略版本 |
| optimization_method | VARCHAR(20) | NOT NULL | 優化方法 |
| objective_function | VARCHAR(30) | NOT NULL | 目標函數 |
| parameter_config | JSONB | NOT NULL | 參數搜索空間 |
| backtest_config | JSONB | NOT NULL | 回測配置 |
| status | VARCHAR(20) | DEFAULT 'QUEUED' | 執行狀態 |
| total_combinations | INTEGER | | 總參數組合數 |
| best_parameters | JSONB | | 最佳參數組合 |
| best_objective_value | NUMERIC(10,4) | | 最佳目標值 |
| all_results | JSONB | | 所有結果 |

### 3.6 factor_metadata (因子元數據表)

| 欄位 | 類型 | 約束 | 說明 |
|-----|------|------|------|
| factor_id | VARCHAR(30) | PK | 因子 ID |
| factor_name | VARCHAR(50) | NOT NULL | 因子名稱 |
| display_name | VARCHAR(100) | NOT NULL | 顯示名稱 |
| category | VARCHAR(20) | NOT NULL | 因子類別 |
| source_module | VARCHAR(10) | NOT NULL | 來源模組 |
| data_type | VARCHAR(20) | NOT NULL | 資料類型 |
| value_range | JSONB | | 值範圍 |
| typical_thresholds | JSONB | | 典型閾值 |
| supported_operators | JSONB | NOT NULL | 支援的運算子 |
| description | TEXT | | 因子說明 |
| is_active | BOOLEAN | DEFAULT TRUE | 是否啟用 |

---

## 4. 索引設計

### 4.1 strategies 索引

```sql
CREATE INDEX idx_strategies_type ON strategies(strategy_type);
CREATE INDEX idx_strategies_status ON strategies(status);
CREATE INDEX idx_strategies_is_preset ON strategies(is_preset);
CREATE INDEX idx_strategies_conditions ON strategies USING GIN(conditions);
```

### 4.2 strategy_signals 索引

```sql
-- 主要查詢索引
CREATE INDEX idx_strategy_signals_strategy_id ON strategy_signals(strategy_id);
CREATE INDEX idx_strategy_signals_stock_id ON strategy_signals(stock_id);
CREATE INDEX idx_strategy_signals_trade_date ON strategy_signals(trade_date);
CREATE INDEX idx_strategy_signals_signal_type ON strategy_signals(signal_type);
CREATE INDEX idx_strategy_signals_is_consumed ON strategy_signals(is_consumed);

-- 複合索引
CREATE INDEX idx_strategy_signals_strategy_date ON strategy_signals(strategy_id, trade_date);
CREATE INDEX idx_strategy_signals_stock_date ON strategy_signals(stock_id, trade_date);

-- JSONB GIN 索引
CREATE INDEX idx_strategy_signals_matched ON strategy_signals USING GIN(matched_conditions);
```

### 4.3 strategy_executions 索引

```sql
CREATE INDEX idx_strategy_executions_strategy_id ON strategy_executions(strategy_id);
CREATE INDEX idx_strategy_executions_date ON strategy_executions(execution_date);
CREATE INDEX idx_strategy_executions_status ON strategy_executions(status);
CREATE INDEX idx_strategy_executions_strategy_date ON strategy_executions(strategy_id, execution_date);
```

---

## 5. 資料流向圖

```
                    ┌───────────────────────────────────────────────┐
                    │              M11 策略執行流程                   │
                    └───────────────────────────────────────────────┘

┌─────────────────┐
│   strategies    │ ← 1. 載入策略定義
└────────┬────────┘
         │
         ▼
┌─────────────────┐     ┌──────────────────────────────────────────┐
│factor_metadata  │ ←── │ 2. 確認策略使用的因子                       │
└─────────────────┘     └──────────────────────────────────────────┘
         │
         ▼
┌─────────────────┐     ┌──────────────────────────────────────────┐
│     stocks      │ ←── │ 3. 取得股票清單                            │
│   (M06 依賴)    │     └──────────────────────────────────────────┘
└────────┬────────┘
         │
         ├────────────────────────────────────────┐
         ▼                                        ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│technical_       │  │fundamental_     │  │chip_analysis_   │
│indicators (M07) │  │indicators (M08) │  │results (M09)    │
└────────┬────────┘  └────────┬────────┘  └────────┬────────┘
         │                    │                    │
         └────────────────────┼────────────────────┘
                              │
                              ▼ 4. 載入因子數據
                    ┌─────────────────┐
                    │ StrategyEngine  │ ← 5. 執行策略評估
                    │   (引擎處理)     │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │strategy_        │ ← 6. 記錄執行結果
                    │executions       │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │strategy_signals │ ← 7. 儲存策略信號
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │  M13 信號引擎   │ ← 8. 下游消費
                    │  (is_consumed)  │
                    └─────────────────┘
```

---

## 📚 相關文檔

- [M11 資料庫設計](../M11-資料庫設計.md)
- [M11 功能需求](../../specs/functional/M11-量化策略功能需求.md)
- [M07 ERD](./M07-ERD.md)
- [M08 ERD](./M08-ERD.md)
- [M09 ERD](./M09-ERD.md)

---

**文件維護者**: 資料庫架構師
**最後更新**: 2026-01-14
**下次審核**: 2026-04-14
