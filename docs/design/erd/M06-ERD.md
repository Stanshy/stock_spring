# M06-資料管理模組 ERD (Entity-Relationship Diagram)

> **文件編號**: ERD-M06  
> **模組名稱**: 資料管理模組  
> **版本**: v2.0  
> **最後更新**: 2025-12-31  
> **狀態**: Draft

---

## 📋 ERD 概述

本文件定義 M06 資料管理模組的實體關聯圖（ERD），展示所有資料表之間的關係。

---

## 🗂️ 核心實體

### 1. stocks (股票基本資料)
**主鍵**: stock_id  
**關聯**:
- 一對多 → stock_prices (股價資料)
- 一對多 → financial_statements (財務報表)
- 一對多 → institutional_trading (三大法人交易)
- 一對多 → margin_trading (融資融券)

### 2. stock_prices (股價歷史資料)
**主鍵**: (price_id, trade_date) — 分區表  
**外鍵**:
- stock_id → stocks(stock_id)
- trade_date → trading_calendar(trade_date)

### 3. financial_statements (財務報表)
**主鍵**: statement_id  
**外鍵**:
- stock_id → stocks(stock_id)

### 4. institutional_trading (三大法人交易)
**主鍵**: (stock_id, trade_date, investor_type)  
**外鍵**:
- stock_id → stocks(stock_id)
- trade_date → trading_calendar(trade_date)

### 5. margin_trading (融資融券)
**主鍵**: (stock_id, trade_date)  
**外鍵**:
- stock_id → stocks(stock_id)
- trade_date → trading_calendar(trade_date)

### 6. trading_calendar (交易日曆)
**主鍵**: trade_date  
**關聯**: 
- 被 stock_prices, institutional_trading, margin_trading 參照

---

## 📊 Mermaid ERD

```mermaid
erDiagram
    stocks ||--o{ stock_prices : "has"
    stocks ||--o{ financial_statements : "has"
    stocks ||--o{ institutional_trading : "has"
    stocks ||--o{ margin_trading : "has"
    
    trading_calendar ||--o{ stock_prices : "validates"
    trading_calendar ||--o{ institutional_trading : "validates"
    trading_calendar ||--o{ margin_trading : "validates"
    
    stocks {
        varchar stock_id PK "股票代碼"
        varchar stock_name "股票名稱"
        varchar market_type "市場類型"
        varchar industry "產業"
        date listing_date "上市日期"
        boolean is_active "是否活躍"
        text[] tags "標籤陣列"
        jsonb extra_info "額外資訊"
    }
    
    stock_prices {
        bigint price_id PK "ID"
        varchar stock_id FK "股票代碼"
        date trade_date PK "交易日期"
        numeric open_price "開盤價"
        numeric high_price "最高價"
        numeric low_price "最低價"
        numeric close_price "收盤價"
        bigint volume "成交量"
    }
    
    financial_statements {
        bigint statement_id PK "ID"
        varchar stock_id FK "股票代碼"
        integer year "年度"
        integer quarter "季度"
        numeric revenue "營收"
        numeric net_income "淨利"
        numeric total_assets "總資產"
        jsonb raw_data "原始資料"
    }
    
    institutional_trading {
        varchar stock_id PK_FK "股票代碼"
        date trade_date PK_FK "交易日期"
        varchar investor_type PK "投資人類型"
        bigint buy_volume "買進量"
        bigint sell_volume "賣出量"
        bigint net_volume "淨買賣"
    }
    
    margin_trading {
        varchar stock_id PK_FK "股票代碼"
        date trade_date PK_FK "交易日期"
        bigint margin_balance "融資餘額"
        bigint short_balance "融券餘額"
        numeric margin_ratio "融資維持率"
    }
    
    trading_calendar {
        date trade_date PK "交易日期"
        boolean is_trading_day "是否交易日"
        varchar holiday_name "假日名稱"
    }
```

---

## 🔗 關聯說明

### 一對多關係

1. **stocks → stock_prices**  
   一檔股票有多筆歷史股價記錄
   - 關聯鍵: stock_id
   - 刪除策略: CASCADE (刪除股票時同時刪除歷史股價)

2. **stocks → financial_statements**  
   一檔股票有多筆財報記錄
   - 關聯鍵: stock_id
   - 刪除策略: CASCADE

3. **stocks → institutional_trading**  
   一檔股票有多筆三大法人交易記錄
   - 關聯鍵: stock_id
   - 刪除策略: CASCADE

4. **stocks → margin_trading**  
   一檔股票有多筆融資融券記錄
   - 關聯鍵: stock_id
   - 刪除策略: CASCADE

5. **trading_calendar → stock_prices**  
   交易日曆驗證股價資料的交易日期
   - 關聯鍵: trade_date
   - 刪除策略: RESTRICT (不可刪除已被參照的交易日)

---

## 📚 相關文檔

- [M06 資料庫設計](../M06-資料庫設計.md)
- [全系統資料庫架構](../database-schema.md)
- [M06 功能需求](../../specs/functional/M06-資料管理功能需求.md)

---

**文件維護者**: 資料庫設計師  
**最後更新**: 2025-12-31
