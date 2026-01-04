# M08-基本面分析模組 ERD (Entity-Relationship Diagram)

> **文件編號**: ERD-M08  
> **模組名稱**: 基本面分析模組  
> **版本**: v2.0  
> **最後更新**: 2025-12-31  
> **狀態**: Draft

---

## 📋 ERD 概述

本文件定義 M08 基本面分析模組的實體關聯圖（ERD），展示所有資料表之間的關係。

---

## 🗂️ 核心實體

### 1. fundamental_indicators (基本面指標資料)
**主鍵**: (stock_id, year, quarter, indicator_name)  
**外鍵**:
- stock_id → stocks(stock_id) [M06]
- (stock_id, year, quarter) → financial_statements [M06]

### 2. valuation_metrics (估值指標)
**主鍵**: (stock_id, trade_date)  
**外鍵**:
- stock_id → stocks(stock_id) [M06]
- trade_date → trading_calendar(trade_date) [M06]

### 3. fundamental_scores (基本面評分)
**主鍵**: score_id  
**外鍵**:
- stock_id → stocks(stock_id) [M06]

### 4. dividend_history (股利歷史)
**主鍵**: (stock_id, year)  
**外鍵**:
- stock_id → stocks(stock_id) [M06]

---

## 📊 Mermaid ERD

```mermaid
erDiagram
    stocks ||--o{ fundamental_indicators : "has"
    financial_statements ||--o{ fundamental_indicators : "derives_from"
    
    stocks ||--o{ valuation_metrics : "has"
    trading_calendar ||--o{ valuation_metrics : "validates"
    stock_prices ||--o{ valuation_metrics : "uses"
    
    stocks ||--o{ fundamental_scores : "evaluated_by"
    fundamental_indicators ||--o{ fundamental_scores : "contributes_to"
    
    stocks ||--o{ dividend_history : "has"
    
    stocks {
        varchar stock_id PK "股票代碼 (M06)"
    }
    
    financial_statements {
        bigint statement_id PK "財報ID (M06)"
        varchar stock_id FK "股票代碼"
        integer year "年度"
        integer quarter "季度"
    }
    
    trading_calendar {
        date trade_date PK "交易日期 (M06)"
    }
    
    stock_prices {
        bigint price_id PK "股價ID (M06)"
        varchar stock_id FK "股票代碼"
        date trade_date FK "交易日期"
        numeric close_price "收盤價"
    }
    
    fundamental_indicators {
        varchar stock_id PK_FK "股票代碼"
        integer year PK "年度"
        integer quarter PK "季度"
        varchar indicator_name PK "指標名稱"
        numeric indicator_value "指標數值"
        jsonb metadata "元數據"
        timestamp calculated_at "計算時間"
    }
    
    valuation_metrics {
        varchar stock_id PK_FK "股票代碼"
        date trade_date PK_FK "交易日期"
        numeric pe_ratio "本益比"
        numeric pb_ratio "股價淨值比"
        numeric ps_ratio "股價營收比"
        numeric pcf_ratio "股價現金流比"
        numeric ev_ebitda "企業價值倍數"
        numeric dividend_yield "殖利率"
        jsonb metadata "元數據"
    }
    
    fundamental_scores {
        bigint score_id PK "評分ID"
        varchar stock_id FK "股票代碼"
        integer year "年度"
        integer quarter "季度"
        numeric profitability_score "獲利能力"
        numeric growth_score "成長性"
        numeric safety_score "安全性"
        numeric efficiency_score "效率性"
        numeric total_score "總分"
        jsonb score_details "評分細節"
        timestamp calculated_at "計算時間"
    }
    
    dividend_history {
        varchar stock_id PK_FK "股票代碼"
        integer year PK "年度"
        numeric cash_dividend "現金股利"
        numeric stock_dividend "股票股利"
        numeric total_dividend "總股利"
        date ex_dividend_date "除息日"
        date payment_date "發放日"
        jsonb metadata "元數據"
    }
```

---

## 🔗 關聯說明

### 跨模組關聯

1. **stocks (M06) → fundamental_indicators (M08)**  
   一檔股票有多筆基本面指標記錄
   - 關聯鍵: stock_id
   - 刪除策略: CASCADE

2. **financial_statements (M06) → fundamental_indicators (M08)**  
   財報資料衍生出基本面指標
   - 關聯鍵: (stock_id, year, quarter)
   - 刪除策略: CASCADE

3. **stock_prices (M06) → valuation_metrics (M08)**  
   股價資料用於計算估值指標
   - 關聯鍵: (stock_id, trade_date)
   - 刪除策略: RESTRICT

### 模組內關聯

4. **fundamental_indicators → fundamental_scores**  
   指標資料貢獻於評分計算
   - 間接關聯透過 stock_id, year, quarter

5. **stocks → dividend_history**  
   股票的股利發放歷史
   - 關聯鍵: stock_id
   - 刪除策略: CASCADE

---

## 🎯 設計要點

### 1. 時間維度設計
- **fundamental_indicators**: 以 (year, quarter) 為時間維度（季頻資料）
- **valuation_metrics**: 以 trade_date 為時間維度（日頻資料）
- **dividend_history**: 以 year 為時間維度（年頻資料）

### 2. 複合主鍵設計
fundamental_indicators 使用 (stock_id, year, quarter, indicator_name) 確保：
- 同一股票、同一期間、同一指標只有一筆記錄
- 支援多種指標共存

### 3. 跨模組依賴
M08 強依賴 M06 的：
- stocks: 股票基本資料
- financial_statements: 財報原始資料
- stock_prices: 股價資料（用於估值）
- trading_calendar: 交易日曆

### 4. JSONB 彈性儲存
- metadata: 儲存額外資訊（如資料來源、計算方法）
- score_details: 儲存評分的詳細組成（各項目分數）

---

## 📚 相關文檔

- [M08 資料庫設計](../M08-資料庫設計.md)
- [M06 ERD](./M06-ERD.md)
- [M08 功能需求](../../specs/functional/M08-基本面分析功能需求.md)

---

**文件維護者**: 資料庫設計師  
**最後更新**: 2025-12-31
