# M14-選股引擎 ERD

> **文件編號**: ERD-M14
> **模組名稱**: 選股引擎 (Stock Screening Engine)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. 實體關聯圖

```mermaid
erDiagram
    %% ==================================================
    %% M14 選股引擎 核心實體
    %% ==================================================

    screening_strategies {
        bigserial id PK
        varchar strategy_id UK "策略識別碼"
        varchar strategy_name "策略名稱"
        varchar description "描述"
        varchar owner_id "擁有者ID"
        boolean is_public "是否公開"
        jsonb conditions "篩選條件組合"
        varchar condition_summary "條件摘要"
        varchar market "市場"
        jsonb sectors "產業篩選"
        decimal min_price "最低價格"
        decimal max_price "最高價格"
        integer min_volume "最低成交量"
        jsonb sort_by "排序欄位"
        jsonb sort_direction "排序方向"
        integer default_limit "預設筆數"
        integer execution_count "執行次數"
        integer copy_count "被複製次數"
        timestamp last_executed_at "最後執行時間"
        varchar status "狀態"
        timestamp created_at
        timestamp updated_at
    }

    screening_templates {
        bigserial id PK
        varchar template_code UK "模板代碼"
        varchar template_name "模板名稱"
        varchar description "描述"
        varchar category "分類"
        jsonb conditions "篩選條件"
        varchar condition_summary "條件摘要"
        jsonb sort_by "排序欄位"
        jsonb sort_direction "排序方向"
        integer default_limit "預設筆數"
        integer display_order "顯示順序"
        boolean is_active "是否啟用"
        timestamp created_at
        timestamp updated_at
    }

    screening_executions {
        bigserial id PK
        varchar execution_id UK "執行識別碼"
        varchar execution_type "執行類型"
        varchar template_code FK "模板代碼"
        varchar strategy_id FK "策略ID"
        varchar name "名稱"
        varchar user_id "執行者ID"
        jsonb conditions_snapshot "條件快照"
        varchar condition_summary "條件摘要"
        varchar market "市場"
        jsonb sectors "產業"
        jsonb sort_by "排序欄位"
        jsonb sort_direction "排序方向"
        integer result_limit "結果筆數"
        integer total_candidates "候選股票數"
        integer matched_count "符合條件數"
        integer execution_time_ms "執行耗時"
        timestamp executed_at "執行時間"
        timestamp created_at
    }

    screening_results {
        bigserial id PK
        varchar execution_id FK "執行ID"
        integer rank "排名"
        varchar stock_id "股票代碼"
        varchar stock_name "股票名稱"
        varchar market "市場"
        varchar sector "產業"
        decimal price_at_execution "執行時價格"
        decimal price_change "價格變動"
        decimal price_change_pct "漲跌幅"
        jsonb matched_conditions "符合條件"
        jsonb key_metrics "關鍵指標"
        varchar signal_id FK "信號ID"
        decimal signal_score "信號評分"
        varchar signal_grade "信號評級"
        varchar signal_direction "信號方向"
        timestamp created_at
    }

    screening_performance {
        bigserial id PK
        varchar execution_id FK "執行ID"
        varchar stock_id "股票代碼"
        date trade_date "交易日"
        decimal price_at_execution "執行時價格"
        decimal price_1d "1日後價格"
        decimal return_1d "1日報酬"
        decimal price_5d "5日後價格"
        decimal return_5d "5日報酬"
        decimal price_10d "10日後價格"
        decimal return_10d "10日報酬"
        decimal price_20d "20日後價格"
        decimal return_20d "20日報酬"
        decimal max_price "期間最高價"
        decimal max_return "最大報酬"
        decimal min_price "期間最低價"
        decimal min_return "最小報酬"
        integer tracking_days "已追蹤天數"
        timestamp last_updated_at "最後更新"
        timestamp created_at
    }

    condition_definitions {
        bigserial id PK
        varchar condition_code UK "條件代碼"
        varchar condition_name "條件名稱"
        varchar description "描述"
        varchar category "分類"
        varchar data_source "資料來源"
        varchar data_type "資料類型"
        jsonb operators "支援運算符"
        jsonb allowed_values "允許值"
        jsonb value_range "值範圍"
        varchar unit "單位"
        jsonb example "範例"
        integer display_order "顯示順序"
        boolean is_active "是否啟用"
        timestamp created_at
        timestamp updated_at
    }

    %% ==================================================
    %% 關聯定義
    %% ==================================================

    screening_strategies ||--o{ screening_executions : "has"
    screening_templates ||--o{ screening_executions : "used_by"
    screening_executions ||--o{ screening_results : "produces"
    screening_executions ||--o{ screening_performance : "tracked_in"
```

---

## 2. 表格關係矩陣

| 主表 | 關聯表 | 關係類型 | 外鍵欄位 | 說明 |
|-----|-------|---------|---------|------|
| screening_strategies | screening_executions | 1:N | strategy_id | 策略的執行記錄 |
| screening_templates | screening_executions | 1:N | template_code | 模板的執行記錄 |
| screening_executions | screening_results | 1:N | execution_id | 執行的結果明細 |
| screening_executions | screening_performance | 1:N | execution_id | 執行的績效追蹤 |

---

## 3. 跨模組關聯

```mermaid
erDiagram
    %% ==================================================
    %% M14 與其他模組關聯
    %% ==================================================

    M06_stocks {
        varchar stock_id PK
        varchar stock_name
        varchar market
        varchar sector_code
        varchar status
    }

    M06_stock_daily {
        varchar stock_id FK
        date trade_date
        decimal close_price
        decimal price_change_pct
        bigint volume
    }

    M07_technical_indicators {
        varchar stock_id FK
        date trade_date
        decimal rsi_14
        decimal macd
        decimal ma5
        decimal ma20
    }

    M08_fundamental_indicators {
        varchar stock_id FK
        date report_date
        decimal pe_ratio
        decimal pb_ratio
        decimal roe
        decimal eps
    }

    M09_chip_indicators {
        varchar stock_id FK
        date trade_date
        bigint foreign_net
        integer foreign_cont_days
        bigint trust_net
    }

    M13_unified_signals {
        varchar signal_id PK
        varchar stock_id FK
        date trade_date
        decimal unified_score
        varchar grade
        varchar unified_direction
    }

    M14_screening_executions {
        varchar execution_id PK
        jsonb conditions_snapshot
        timestamp executed_at
    }

    M14_screening_results {
        varchar execution_id FK
        varchar stock_id FK
        varchar signal_id FK
        jsonb key_metrics
    }

    %% 資料來源關聯（查詢時動態 JOIN）
    M06_stocks ||--o{ M14_screening_results : "screened"
    M06_stock_daily ||--o{ M14_screening_results : "price_data"
    M07_technical_indicators ||--o{ M14_screening_results : "technical_data"
    M08_fundamental_indicators ||--o{ M14_screening_results : "fundamental_data"
    M09_chip_indicators ||--o{ M14_screening_results : "chip_data"
    M13_unified_signals ||--o{ M14_screening_results : "signal_data"
```

---

## 4. 跨模組依賴說明

| 來源模組 | 依賴說明 | 用途 |
|---------|---------|------|
| M06 | stocks, stock_daily | 股票基本資料、價量資料 |
| M07 | technical_indicators | 技術指標篩選（RSI, MACD, MA等） |
| M08 | fundamental_indicators | 基本面篩選（PE, ROE, EPS等） |
| M09 | chip_indicators | 籌碼面篩選（外資買賣超、連買天數等） |
| M13 | unified_signals | 信號篩選（評級、評分、方向等） |

---

## 5. 資料流說明

### 5.1 篩選執行流程

```
┌─────────────────────────────────────────────────────────────────────┐
│                        選股執行資料流                                 │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌─────────────────┐                                                │
│  │ condition_      │ ◄── 定義可用篩選條件                            │
│  │ definitions     │                                                │
│  └────────┬────────┘                                                │
│           │ 參照                                                     │
│           ▼                                                         │
│  ┌─────────────────┐     ┌─────────────────┐                       │
│  │ screening_      │     │ screening_      │                       │
│  │ strategies      │────►│ executions      │ ◄── 執行記錄          │
│  │ (自訂策略)       │     │                 │                       │
│  └─────────────────┘     └────────┬────────┘                       │
│                                   │                                  │
│  ┌─────────────────┐              │                                 │
│  │ screening_      │──────────────┤                                 │
│  │ templates       │              │                                 │
│  │ (快速選股)       │              │                                 │
│  └─────────────────┘              │                                 │
│                                   │                                  │
│           ┌───────────────────────┼───────────────────────┐         │
│           │                       │                       │         │
│           ▼                       ▼                       ▼         │
│  ┌─────────────────┐     ┌─────────────────┐     ┌────────────┐    │
│  │ screening_      │     │ screening_      │     │ M06-M13    │    │
│  │ results         │◄────│ 動態查詢         │────►│ 資料表      │    │
│  │ (結果明細)       │     │ (多維度JOIN)     │     │ (資料來源)  │    │
│  └────────┬────────┘     └─────────────────┘     └────────────┘    │
│           │                                                         │
│           │ 績效追蹤                                                 │
│           ▼                                                         │
│  ┌─────────────────┐                                                │
│  │ screening_      │                                                │
│  │ performance     │ ◄── 1d/5d/10d/20d 報酬追蹤                     │
│  └─────────────────┘                                                │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

### 5.2 動態查詢 JOIN 策略

```
┌─────────────────────────────────────────────────────────────────────┐
│                     動態多維度篩選查詢                               │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│                    ┌──────────────────┐                             │
│                    │  stocks (M06)    │ ◄── 基礎表（必定 JOIN）      │
│                    │  stock_daily     │                             │
│                    └────────┬─────────┘                             │
│                             │                                        │
│       ┌─────────────────────┼─────────────────────┐                 │
│       │                     │                     │                  │
│       ▼                     ▼                     ▼                  │
│  ┌─────────┐          ┌─────────┐          ┌─────────┐              │
│  │  M07    │          │  M08    │          │  M09    │              │
│  │ 技術指標 │          │ 基本面  │          │ 籌碼    │              │
│  │         │          │         │          │         │              │
│  │ LEFT    │          │ LEFT    │          │ LEFT    │              │
│  │ JOIN if │          │ JOIN if │          │ JOIN if │              │
│  │條件需要  │          │條件需要  │          │條件需要  │              │
│  └────┬────┘          └────┬────┘          └────┬────┘              │
│       │                    │                    │                    │
│       └────────────────────┼────────────────────┘                   │
│                            │                                         │
│                            ▼                                         │
│                    ┌──────────────────┐                             │
│                    │  M13 統一信號    │                             │
│                    │  LEFT JOIN if    │                             │
│                    │  信號條件存在     │                             │
│                    └────────┬─────────┘                             │
│                             │                                        │
│                             ▼                                        │
│                    ┌──────────────────┐                             │
│                    │  WHERE 動態條件   │                             │
│                    │  AND/OR 組合     │                             │
│                    └────────┬─────────┘                             │
│                             │                                        │
│                             ▼                                        │
│                    ┌──────────────────┐                             │
│                    │  篩選結果         │                             │
│                    └──────────────────┘                             │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 6. 索引設計摘要

| 表格 | 索引名稱 | 欄位 | 用途 |
|-----|---------|------|------|
| screening_strategies | idx_strategies_owner | owner_id, status | 用戶策略列表 |
| screening_strategies | idx_strategies_public | is_public, status | 公開策略查詢 |
| screening_executions | idx_executions_user_date | user_id, executed_at | 執行歷史查詢 |
| screening_executions | idx_executions_strategy | strategy_id, executed_at | 策略執行記錄 |
| screening_results | idx_results_execution | execution_id | 結果查詢 |
| screening_results | idx_results_stock | stock_id, created_at | 股票被選記錄 |
| screening_performance | idx_performance_pending | tracking_days | 待追蹤績效 |

---

## 7. 相關文檔

- [M14 功能需求](../../specs/functional/M14-選股引擎功能需求.md)
- [M14 API 規格](../../specs/api/M14-API規格.md)
- [M14 資料庫設計](../M14-資料庫設計.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
