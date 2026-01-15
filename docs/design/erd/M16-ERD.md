# M16-回測系統 ERD

> **文件編號**: ERD-M16
> **模組名稱**: 回測系統 (Backtesting System)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. 實體關聯圖

```mermaid
erDiagram
    %% ==================================================
    %% M16 回測系統 核心實體
    %% ==================================================

    backtest_tasks {
        bigserial id PK
        varchar backtest_id UK "回測識別碼"
        varchar user_id "用戶ID"
        varchar backtest_name "回測名稱"
        varchar signal_source "信號來源"
        varchar strategy_id FK "策略ID"
        varchar strategy_name "策略名稱"
        varchar target_mode "目標模式"
        text_array target_stock_ids "目標股票"
        date start_date "開始日期"
        date end_date "結束日期"
        integer trading_days "交易日數"
        decimal initial_capital "初始資金"
        jsonb trading_settings "交易設定"
        jsonb exit_rules "出場規則"
        varchar status "執行狀態"
        decimal progress_percent "執行進度"
        timestamp started_at "開始時間"
        timestamp completed_at "完成時間"
        timestamp created_at
        timestamp updated_at
    }

    backtest_trades {
        bigserial id PK
        varchar trade_id UK "交易識別碼"
        varchar backtest_id FK "回測ID"
        varchar stock_id "股票代碼"
        varchar stock_name "股票名稱"
        varchar trade_type "交易類型"
        date trade_date "交易日期"
        decimal trade_price "交易價格"
        integer shares "股數"
        decimal trade_amount "交易金額"
        decimal commission "手續費"
        decimal tax "交易稅"
        decimal slippage "滑價"
        decimal net_amount "淨金額"
        varchar signal_source "信號來源"
        varchar signal_id "信號ID"
        varchar exit_reason "出場原因"
        integer holding_days "持有天數"
        decimal profit_loss "損益"
        decimal return_pct "報酬率"
        timestamp created_at
    }

    backtest_daily_snapshots {
        bigserial id PK
        varchar backtest_id FK "回測ID"
        date snapshot_date "快照日期"
        decimal portfolio_value "投組價值"
        decimal cash "現金"
        decimal position_value "持倉價值"
        decimal daily_return "日報酬"
        decimal cumulative_return "累計報酬"
        decimal high_water_mark "最高水位"
        decimal drawdown "回撤"
        jsonb positions "持倉明細"
        decimal benchmark_value "基準值"
        decimal benchmark_return "基準報酬"
        timestamp created_at
    }

    backtest_results {
        bigserial id PK
        varchar backtest_id FK,UK "回測ID"
        decimal final_value "最終價值"
        decimal total_return "總報酬"
        decimal annualized_return "年化報酬"
        decimal sharpe_ratio "夏普比率"
        decimal sortino_ratio "索提諾比率"
        decimal calmar_ratio "卡瑪比率"
        decimal volatility "波動度"
        decimal max_drawdown "最大回撤"
        integer max_drawdown_duration "最大回撤天數"
        varchar benchmark_code "基準代碼"
        decimal alpha "Alpha"
        decimal beta "Beta"
        integer total_trades "總交易數"
        integer winning_trades "獲利交易數"
        decimal win_rate "勝率"
        decimal profit_factor "獲利因子"
        jsonb monthly_returns "月度報酬"
        jsonb stock_performance "各股績效"
        timestamp created_at
        timestamp updated_at
    }

    backtest_drawdowns {
        bigserial id PK
        varchar backtest_id FK "回測ID"
        integer rank "排名"
        date start_date "開始日期"
        date end_date "結束日期"
        date recovery_date "恢復日期"
        decimal peak_value "峰值"
        decimal trough_value "谷底值"
        decimal drawdown_depth "回撤深度"
        integer drawdown_duration "回撤天數"
        integer recovery_days "恢復天數"
        timestamp created_at
    }

    backtest_optimizations {
        bigserial id PK
        varchar optimization_id UK "最佳化識別碼"
        varchar backtest_id FK "回測ID"
        varchar user_id "用戶ID"
        varchar optimization_method "最佳化方法"
        varchar target_metric "目標指標"
        jsonb parameters "待優化參數"
        jsonb constraints "約束條件"
        varchar status "執行狀態"
        integer total_combinations "總組合數"
        integer completed_combinations "完成組合數"
        jsonb best_params "最佳參數"
        decimal best_metric_value "最佳指標值"
        timestamp started_at "開始時間"
        timestamp completed_at "完成時間"
        timestamp created_at
        timestamp updated_at
    }

    backtest_optimization_results {
        bigserial id PK
        varchar optimization_id FK "最佳化ID"
        integer combination_index "組合索引"
        jsonb parameters "參數組合"
        decimal total_return "總報酬"
        decimal sharpe_ratio "夏普比率"
        decimal max_drawdown "最大回撤"
        decimal win_rate "勝率"
        integer total_trades "交易數"
        integer rank_by_target "目標排名"
        boolean meets_constraints "符合約束"
        timestamp created_at
    }

    backtest_templates {
        bigserial id PK
        varchar template_id UK "範本識別碼"
        varchar user_id "用戶ID"
        varchar template_name "範本名稱"
        text description "描述"
        jsonb trading_settings "交易設定"
        jsonb exit_rules "出場規則"
        integer usage_count "使用次數"
        timestamp last_used_at "最後使用"
        timestamp created_at
        timestamp updated_at
    }

    backtest_comparisons {
        bigserial id PK
        varchar comparison_id UK "比較識別碼"
        varchar user_id "用戶ID"
        text_array backtest_ids "回測ID清單"
        varchar benchmark_code "基準代碼"
        jsonb comparison_result "比較結果"
        jsonb correlation_matrix "相關矩陣"
        timestamp created_at
    }

    %% ==================================================
    %% 關聯定義
    %% ==================================================

    backtest_tasks ||--o{ backtest_trades : "generates"
    backtest_tasks ||--o{ backtest_daily_snapshots : "records"
    backtest_tasks ||--|| backtest_results : "produces"
    backtest_tasks ||--o{ backtest_drawdowns : "has"
    backtest_tasks ||--o{ backtest_optimizations : "optimizes"
    backtest_optimizations ||--o{ backtest_optimization_results : "contains"
```

---

## 2. 表格關係矩陣

| 主表 | 關聯表 | 關係類型 | 外鍵欄位 | 說明 |
|-----|-------|---------|---------|------|
| backtest_tasks | backtest_trades | 1:N | backtest_id | 回測的交易記錄 |
| backtest_tasks | backtest_daily_snapshots | 1:N | backtest_id | 回測的每日快照 |
| backtest_tasks | backtest_results | 1:1 | backtest_id | 回測的績效結果 |
| backtest_tasks | backtest_drawdowns | 1:N | backtest_id | 回測的回撤記錄 |
| backtest_tasks | backtest_optimizations | 1:N | backtest_id | 回測的最佳化任務 |
| backtest_optimizations | backtest_optimization_results | 1:N | optimization_id | 最佳化的各組合結果 |

---

## 3. 跨模組關聯

```mermaid
erDiagram
    %% ==================================================
    %% M16 與其他模組關聯
    %% ==================================================

    M06_stocks {
        varchar stock_id PK
        varchar stock_name
        varchar market
    }

    M06_stock_daily {
        varchar stock_id FK
        date trade_date
        decimal open_price
        decimal high_price
        decimal low_price
        decimal close_price
        bigint volume
    }

    M07_technical_indicators {
        varchar stock_id FK
        date trade_date
        decimal ma_5
        decimal ma_20
        decimal rsi_14
        decimal macd
    }

    M11_strategies {
        varchar strategy_id PK
        varchar strategy_name
        jsonb conditions
        boolean is_active
    }

    M13_unified_signals {
        varchar signal_id PK
        varchar stock_id FK
        date trade_date
        varchar unified_direction
        varchar grade
        decimal unified_score
    }

    M16_backtest_tasks {
        varchar backtest_id PK
        varchar strategy_id FK
        date start_date
        date end_date
        varchar status
    }

    M16_backtest_trades {
        varchar trade_id PK
        varchar backtest_id FK
        varchar stock_id FK
        varchar signal_id FK
        date trade_date
        decimal trade_price
    }

    M16_backtest_results {
        varchar backtest_id FK,PK
        decimal total_return
        decimal sharpe_ratio
    }

    %% 跨模組關聯
    M06_stocks ||--o{ M16_backtest_trades : "traded"
    M06_stock_daily ||--o{ M16_backtest_trades : "price_data"
    M11_strategies ||--o{ M16_backtest_tasks : "tested_by"
    M13_unified_signals ||--o{ M16_backtest_trades : "triggers"
    M07_technical_indicators ||--o{ M16_backtest_trades : "indicator_data"
    M16_backtest_tasks ||--|| M16_backtest_results : "produces"
```

---

## 4. 跨模組依賴說明

| 來源模組 | 依賴說明 | 用途 |
|---------|---------|------|
| M06 | stocks, stock_daily | 歷史價格數據、股票基本資料 |
| M07 | technical_indicators | 技術指標數據（回測信號依據） |
| M11 | strategies | 策略定義（回測策略來源） |
| M13 | unified_signals | 統一信號（回測觸發點） |

---

## 5. 資料流說明

### 5.1 回測執行資料流

```
┌─────────────────────────────────────────────────────────────────┐
│                      回測執行資料流                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────┐                                        │
│  │  backtest_tasks     │ ◄── 用戶建立回測任務                   │
│  │  (設定與狀態)        │                                        │
│  └──────────┬──────────┘                                        │
│             │ 執行回測                                           │
│             ▼                                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                    回測引擎執行                            │   │
│  │                                                           │   │
│  │  ┌────────────┐   ┌────────────┐   ┌────────────┐        │   │
│  │  │ M06 價格   │   │ M07 指標   │   │ M13 信號   │        │   │
│  │  │ stock_daily│   │ indicators │   │ unified    │        │   │
│  │  └─────┬──────┘   └─────┬──────┘   └─────┬──────┘        │   │
│  │        │                │                │               │   │
│  │        └────────────────┴────────────────┘               │   │
│  │                         │                                │   │
│  │                         ▼                                │   │
│  │              ┌─────────────────────┐                     │   │
│  │              │   逐日掃描與模擬     │                     │   │
│  │              └─────────┬───────────┘                     │   │
│  │                        │                                 │   │
│  └────────────────────────┼─────────────────────────────────┘   │
│                           │                                     │
│         ┌─────────────────┼─────────────────┐                   │
│         │                 │                 │                   │
│         ▼                 ▼                 ▼                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐          │
│  │ backtest_    │  │ backtest_    │  │ backtest_    │          │
│  │ trades       │  │ daily_       │  │ drawdowns    │          │
│  │ (交易記錄)    │  │ snapshots    │  │ (回撤記錄)    │          │
│  └──────────────┘  │ (每日快照)    │  └──────────────┘          │
│                    └──────────────┘                             │
│                           │                                     │
│                           ▼                                     │
│                    ┌──────────────┐                             │
│                    │ backtest_    │                             │
│                    │ results      │ ◄── 績效計算                │
│                    │ (績效指標)    │                             │
│                    └──────────────┘                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 5.2 參數最佳化資料流

```
┌─────────────────────────────────────────────────────────────────┐
│                    參數最佳化資料流                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─────────────────────┐                                        │
│  │  backtest_tasks     │ ◄── 基準回測任務                       │
│  │  (原始設定)          │                                        │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│             ▼                                                    │
│  ┌─────────────────────┐                                        │
│  │ backtest_           │ ◄── 建立最佳化任務                     │
│  │ optimizations       │                                        │
│  │ • parameters        │ ◄── 待優化參數範圍                     │
│  │ • target_metric     │ ◄── 目標指標                           │
│  │ • constraints       │ ◄── 約束條件                           │
│  └──────────┬──────────┘                                        │
│             │                                                    │
│             ▼                                                    │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │                  網格搜尋 / 隨機搜尋                       │   │
│  │                                                           │   │
│  │   參數組合 1 ──► 回測 ──► 績效                            │   │
│  │   參數組合 2 ──► 回測 ──► 績效                            │   │
│  │   參數組合 3 ──► 回測 ──► 績效                            │   │
│  │        ...                                                │   │
│  │   參數組合 N ──► 回測 ──► 績效                            │   │
│  │                                                           │   │
│  └──────────────────────────┬────────────────────────────────┘   │
│                             │                                    │
│                             ▼                                    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │           backtest_optimization_results                  │    │
│  │                                                          │    │
│  │  ┌─────────────────────────────────────────────────┐    │    │
│  │  │ 組合 | stopLoss | takeProfit | sharpe | return │    │    │
│  │  ├─────────────────────────────────────────────────┤    │    │
│  │  │   1  │   0.07   │    0.20    │  1.45  │  0.35  │    │    │
│  │  │   2  │   0.06   │    0.18    │  1.42  │  0.33  │    │    │
│  │  │  ... │   ...    │    ...     │  ...   │  ...   │    │    │
│  │  └─────────────────────────────────────────────────┘    │    │
│  │                                                          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                             │                                    │
│                             ▼                                    │
│                    ┌──────────────┐                             │
│                    │ 更新最佳結果  │                             │
│                    │ best_params  │                             │
│                    └──────────────┘                             │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 6. 索引設計摘要

| 表格 | 索引名稱 | 欄位 | 用途 |
|-----|---------|------|------|
| backtest_tasks | idx_tasks_user | user_id, created_at DESC | 用戶回測清單 |
| backtest_tasks | idx_tasks_status | status | 狀態篩選 |
| backtest_tasks | idx_tasks_strategy | strategy_id | 策略篩選 |
| backtest_trades | idx_trades_backtest | backtest_id, trade_date | 交易清單查詢 |
| backtest_trades | idx_trades_stock | backtest_id, stock_id | 股票篩選 |
| backtest_daily_snapshots | idx_snapshots_backtest | backtest_id, snapshot_date | 淨值曲線 |
| backtest_drawdowns | idx_drawdowns_backtest | backtest_id, rank | 回撤排名 |
| backtest_optimizations | idx_opt_backtest | backtest_id | 最佳化查詢 |
| backtest_optimization_results | idx_opt_results | optimization_id, rank_by_target | 結果排名 |
| backtest_templates | idx_templates_user | user_id | 用戶範本 |
| backtest_comparisons | idx_comparisons_user | user_id, created_at DESC | 比較歷史 |

---

## 7. 相關文檔

- [M16 功能需求](../../specs/functional/M16-回測系統功能需求.md)
- [M16 API 規格](../../specs/api/M16-API規格.md)
- [M16 資料庫設計](../M16-資料庫設計.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
