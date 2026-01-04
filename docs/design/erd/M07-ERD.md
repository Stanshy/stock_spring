# M07-技術分析模組 ERD (Entity-Relationship Diagram)

> **文件編號**: ERD-M07  
> **模組名稱**: 技術分析模組  
> **版本**: v2.0  
> **最後更新**: 2025-12-31  
> **狀態**: Draft

---

## 📋 ERD 概述

本文件定義 M07 技術分析模組的實體關聯圖（ERD），展示所有資料表之間的關係。

---

## 🗂️ 核心實體

### 1. technical_indicators (技術指標資料)
**主鍵**: (stock_id, trade_date, indicator_name)  
**外鍵**:
- stock_id → stocks(stock_id) [M06]
- trade_date → trading_calendar(trade_date) [M06]

### 2. indicator_metadata (指標元數據)
**主鍵**: indicator_name  
**關聯**:
- 一對多 → technical_indicators

### 3. signal_crossovers (交叉信號)
**主鍵**: signal_id  
**外鍵**:
- stock_id → stocks(stock_id) [M06]
- trade_date → trading_calendar(trade_date) [M06]

### 4. indicator_groups (指標分組)
**主鍵**: group_id  
**關聯**:
- 多對多 → indicator_metadata (透過 indicator_group_mapping)

---

## 📊 Mermaid ERD

```mermaid
erDiagram
    stocks ||--o{ technical_indicators : "has"
    trading_calendar ||--o{ technical_indicators : "validates"
    indicator_metadata ||--o{ technical_indicators : "defines"
    
    stocks ||--o{ signal_crossovers : "generates"
    trading_calendar ||--o{ signal_crossovers : "validates"
    
    indicator_groups ||--o{ indicator_group_mapping : "contains"
    indicator_metadata ||--o{ indicator_group_mapping : "belongs_to"
    
    stocks {
        varchar stock_id PK "股票代碼 (M06)"
    }
    
    trading_calendar {
        date trade_date PK "交易日期 (M06)"
    }
    
    technical_indicators {
        varchar stock_id PK_FK "股票代碼"
        date trade_date PK_FK "交易日期"
        varchar indicator_name PK_FK "指標名稱"
        jsonb indicator_values "指標數值"
        jsonb parameters "計算參數"
        timestamp calculated_at "計算時間"
    }
    
    indicator_metadata {
        varchar indicator_name PK "指標名稱"
        varchar display_name "顯示名稱"
        varchar category "指標類別"
        jsonb default_parameters "預設參數"
        varchar calculation_formula "計算公式"
        text description "說明"
    }
    
    signal_crossovers {
        bigint signal_id PK "信號ID"
        varchar stock_id FK "股票代碼"
        date trade_date FK "交易日期"
        varchar signal_type "信號類型"
        varchar indicator1 "指標1"
        varchar indicator2 "指標2"
        varchar direction "方向"
        numeric confidence "信心度"
    }
    
    indicator_groups {
        varchar group_id PK "分組ID"
        varchar group_name "分組名稱"
        varchar description "說明"
    }
    
    indicator_group_mapping {
        varchar group_id PK_FK "分組ID"
        varchar indicator_name PK_FK "指標名稱"
        integer display_order "顯示順序"
    }
```

---

## 🔗 關聯說明

### 跨模組關聯

1. **stocks (M06) → technical_indicators (M07)**  
   一檔股票有多筆技術指標記錄
   - 關聯鍵: stock_id
   - 刪除策略: CASCADE

2. **trading_calendar (M06) → technical_indicators (M07)**  
   交易日曆驗證指標資料的交易日期
   - 關聯鍵: trade_date
   - 刪除策略: RESTRICT

### 模組內關聯

3. **indicator_metadata → technical_indicators**  
   指標元數據定義指標的計算規則
   - 關聯鍵: indicator_name
   - 刪除策略: RESTRICT

4. **indicator_groups ↔ indicator_metadata**  
   多對多關係：一個分組包含多個指標，一個指標可屬於多個分組
   - 中介表: indicator_group_mapping

---

## 🎯 設計要點

### 1. 複合主鍵設計
technical_indicators 使用 (stock_id, trade_date, indicator_name) 作為主鍵，確保：
- 同一股票、同一日期、同一指標只有一筆記錄
- 支援多種指標共存

### 2. JSONB 彈性儲存
- indicator_values: 儲存多個指標數值（如 MACD 包含 macd, signal, histogram）
- parameters: 儲存計算參數（如 MA 的週期）

### 3. 跨模組依賴
M07 強依賴 M06 的 stocks 和 trading_calendar 表，確保資料一致性

---

## 📚 相關文檔

- [M07 資料庫設計](../M07-資料庫設計.md)
- [M06 ERD](./M06-ERD.md)
- [M07 功能需求](../../specs/functional/M07-技術分析功能需求.md)

---

**文件維護者**: 資料庫設計師  
**最後更新**: 2025-12-31
