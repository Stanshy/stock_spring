# M06-資料管理模組 API 規格

> **文件編號**: API-M06
> **模組名稱**: 資料管理模組
> **版本**: v2.1
> **最後更新**: 2026-01-01
> **狀態**: Draft

---

## 📋 API 總覽

本文件定義 資料管理模組的所有 REST API 規格（基於已實作的 P0 功能）。

---

## 4. API 設計

> **重要**: 所有 API 必須遵守 [全系統契約 - API 統一規範](../technical/00-全系統契約.md#4-api-統一規範)

### 4.1 API 列表總覽

#### 股票管理 API (StockManagementController)

| API 端點 | HTTP Method | 說明 | 功能編號 | 回應格式 |
|---------|-------------|------|---------|---------|
| GET /api/stocks | GET | 分頁查詢股票清單 | F-M06-001 | 分頁列表 |
| GET /api/stocks/{stockId} | GET | 查詢單一股票資訊 | F-M06-001 | 單一物件 |
| GET /api/stocks/active | GET | 查詢所有活躍股票 | F-M06-001 | 列表 |
| POST /api/stocks | POST | 新增股票 | F-M06-001 | 單一物件 |
| PUT /api/stocks/{stockId} | PUT | 更新股票資訊 | F-M06-001 | 單一物件 |
| DELETE /api/stocks/{stockId} | DELETE | 刪除股票（軟刪除） | F-M06-001 | 空回應 |

#### 市場資料查詢 API (MarketDataQueryController)

| API 端點 | HTTP Method | 說明 | 功能編號 | 回應格式 |
|---------|-------------|------|---------|---------|
| GET /api/market-data/prices/{stockId} | GET | 查詢股票歷史股價 | F-M06-007 | 列表 |
| GET /api/market-data/prices/{stockId}/latest | GET | 查詢最新股價 | F-M06-007 | 單一物件 |
| GET /api/market-data/prices/{stockId}/statistics | GET | 查詢股價統計資訊（含技術指標） | F-M06-007 | 統計物件 |
| GET /api/market-data/institutional/{stockId} | GET | 查詢法人買賣超 | F-M06-007 | 列表 |
| GET /api/market-data/margin/{stockId} | GET | 查詢融資融券 | F-M06-007 | 列表 |

#### 交易日曆 API (TradingCalendarController)

| API 端點 | HTTP Method | 說明 | 功能編號 | 回應格式 |
|---------|-------------|------|---------|---------|
| GET /api/trading-calendar/{date} | GET | 查詢指定日期的交易日曆 | F-M06-005 | 單一物件 |
| GET /api/trading-calendar/is-trading-day/{date} | GET | 檢查是否為交易日 | F-M06-005 | 布林值 |
| GET /api/trading-calendar/range | GET | 查詢日期範圍內的交易日 | F-M06-005 | 列表 |
| GET /api/trading-calendar/next-trading-day | GET | 查詢下一個交易日 | F-M06-005 | 單一物件 |
| GET /api/trading-calendar/previous-trading-day | GET | 查詢上一個交易日 | F-M06-005 | 單一物件 |
| GET /api/trading-calendar/year | GET | 查詢年度所有交易日 | F-M06-005 | 列表 |

#### 資料品質 API (DataQualityController)

| API 端點 | HTTP Method | 說明 | 功能編號 | 回應格式 |
|---------|-------------|------|---------|---------|
| GET /api/data-quality/checks | GET | 查詢所有啟用的檢核規則 | F-M06-006 | 列表 |
| GET /api/data-quality/checks/{targetTable} | GET | 查詢指定表的檢核規則 | F-M06-006 | 列表 |
| GET /api/data-quality/issues | GET | 分頁查詢品質問題 | F-M06-006 | 分頁列表 |
| GET /api/data-quality/issues/open | GET | 查詢未解決的品質問題 | F-M06-006 | 分頁列表 |
| GET /api/data-quality/issues/{issueId} | GET | 查詢品質問題詳情 | F-M06-006 | 單一物件 |
| GET /api/data-quality/summary | GET | 查詢資料品質統計摘要 | F-M06-006 | 統計物件 |
| POST /api/data-quality/run-check | POST | 手動觸發品質檢核 | F-M06-006 | 字串訊息 |

#### Job 管理 API (JobManagementController)

| API 端點 | HTTP Method | 說明 | 功能編號 | 回應格式 |
|---------|-------------|------|---------|---------|
| GET /api/jobs/executions | GET | 分頁查詢 Job 執行記錄 | F-M06-008 | 分頁列表 |
| GET /api/jobs/executions/{executionId} | GET | 查詢單一 Job 執行詳情 | F-M06-008 | 單一物件 |
| GET /api/jobs/status | GET | 查詢 Job 狀態總覽 | F-M06-008 | 統計物件 |
| POST /api/jobs/trigger/stock-price-sync | POST | 手動觸發股價同步 Job | F-M06-008 | Job 執行資訊 |
| POST /api/jobs/trigger/financial-sync | POST | 手動觸發財報同步 Job | F-M06-008 | Job 執行資訊 |
| POST /api/jobs/trigger/data-quality-check | POST | 手動觸發資料品質檢核 Job | F-M06-008 | Job 執行資訊 |

---

### 4.2 API 詳細設計

## 股票管理 API

#### API-M06-001: 分頁查詢股票清單

**Request**:
```
GET /api/stocks?marketType=TWSE&industry=半導體&stockName=台積&activeOnly=true&page=1&size=20
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| marketType | String | N | 市場類別（TWSE/TPEX/EMERGING） | 全部 |
| industry | String | N | 產業分類 | 全部 |
| stockName | String | N | 股票名稱（模糊查詢） | - |
| activeOnly | Boolean | N | 僅查詢活躍股票 | null |
| page | Integer | N | 頁碼（從 1 開始） | 1 |
| size | Integer | N | 每頁筆數 | 20 |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "stock_name_en": "TSMC",
        "market_type": "TWSE",
        "industry": "半導體",
        "sector": "晶圓代工",
        "listing_date": "1994-09-05",
        "is_active": true,
        "par_value": 10,
        "issued_shares": 25930380458,
        "tags": [],
        "extra_info": {

        },
        "created_at": "2025-12-30 19:27:21",
        "updated_at": "2025-12-30 19:27:21"
      }
    ],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total_items": 1,
      "total_pages": 1,
      "has_next": false,
      "has_prev": false
    }
  },
  "timestamp": "2026-01-01T22:36:21.2701051+08:00"
}
```

---

#### API-M06-002: 查詢單一股票資訊

**Request**:
```
GET /api/stocks/2330
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| stockId | String | 股票代碼（如 2330） |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "stock_name_en": "TSMC",
    "market_type": "TWSE",
    "industry": "半導體",
    "sector": "晶圓代工",
    "listing_date": "1994-09-05",
    "is_active": true,
    "par_value": 10,
    "issued_shares": 25930380458,
    "tags": [],
    "extra_info": {

    },
    "created_at": "2025-12-30 19:27:21",
    "updated_at": "2025-12-30 19:27:21"
  },
  "timestamp": "2026-01-01T22:37:10.3426097+08:00"
}
```

**Response** (股票不存在):
```json
{
  "code": 404,
  "message": "Stock with ID '23301' not found",
  "error": {
    "details": "stock_id",
    "field": "Please verify the stock ID and try again",
    "suggestion": "Please check the Please verify the stock ID and try again and try again",
    "error_code": "M06011",
    "error_type": "CLIENT_ERROR"
  },
  "timestamp": "2026-01-01T22:37:56.2481922+08:00",
  "trace_id": "req_569c76c16ecc"
}
```

---

#### API-M06-003: 查詢所有活躍股票

**Request**:
```
GET /api/stocks/active
```

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "stock_id": "2330",
      "stock_name": "台積電",
      "stock_name_en": "TSMC",
      "market_type": "TWSE",
      "industry": "半導體",
      "sector": "晶圓代工",
      "listing_date": "1994-09-05",
      "is_active": true,
      "par_value": 10,
      "issued_shares": 25930380458,
      "tags": [],
      "extra_info": {

      },
      "created_at": "2025-12-30 19:27:21",
      "updated_at": "2025-12-30 19:27:21"
    },
    {
      "stock_id": "2317",
      "stock_name": "鴻海",
      "stock_name_en": "Hon Hai",
      "market_type": "TWSE",
      "industry": "電子",
      "sector": "電子製造",
      "listing_date": "1991-06-15",
      "is_active": true,
      "par_value": 10,
      "issued_shares": 13849042756,
      "tags": [],
      "extra_info": {

      },
      "created_at": "2025-12-30 19:27:21",
      "updated_at": "2025-12-30 19:27:21"
    }
  ],
  "timestamp": "2026-01-01T22:38:32.6428734+08:00"
}
```

---

#### API-M06-004: 新增股票

**Request**:
```
POST /api/stocks
Content-Type: application/json

{
  "stock_id": "23456",
  "stock_name": "測試",
  "stock_name_en": "TEST",
  "market_type": "TWSE",
  "industry": "半導體",
  "sector": "晶圓代工",
  "listing_date": "1994-09-05",
  "par_value": 10.00,
  "issued_shares": 25930380458
}
```

**Request Body**:
| 欄位 | 類型 | 必填 | 說明 |
|-----|------|------|------|
| stock_id | String | Y | 股票代碼 |
| stock_name | String | Y | 股票名稱 |
| stock_name_en | String | N | 英文名稱 |
| market_type | String | Y | 市場類型（TWSE/TPEX/EMERGING） |
| industry | String | N | 產業別 |
| sector | String | N | 產業子分類 |
| listing_date | Date | N | 上市日期 |
| par_value | Decimal | N | 面額 |
| issued_shares | Long | N | 已發行股數 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "stock_id": "23456",
        "stock_name": "測試",
        "stock_name_en": "TEST",
        "market_type": "TWSE",
        "industry": "半導體",
        "sector": "晶圓代工",
        "listing_date": "1994-09-05",
        "is_active": true,
        "par_value": 10.00,
        "issued_shares": 25930380458,
        "created_at": "2026-01-01 22:43:58",
        "updated_at": "2026-01-01 22:43:58"
    },
    "timestamp": "2026-01-01T22:43:58.2858871+08:00"
}
```

---

#### API-M06-005: 更新股票資訊

**Request**:
```
PUT /api/stocks/23456
Content-Type: application/json

{
  "stock_name": "TEST1",
  "industry": "TEST1",
  "issued_shares": 25930380455
}
```

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "stock_id": "23456",
        "stock_name": "TEST1",
        "stock_name_en": "TEST",
        "market_type": "TWSE",
        "industry": "TEST1",
        "sector": "晶圓代工",
        "listing_date": "1994-09-05",
        "is_active": true,
        "par_value": 10.00,
        "issued_shares": 25930380455,
        "created_at": "2026-01-01 22:43:58",
        "updated_at": "2026-01-01 22:43:58"
    },
    "timestamp": "2026-01-01T22:45:48.7765763+08:00"
}
```

---

#### API-M06-006: 刪除股票（軟刪除）

**Request**:
```
DELETE /api/stocks/23456
```

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "timestamp": "2026-01-01T22:46:30.3843935+08:00"
}
```

---

## 市場資料查詢 API

#### API-M06-007: 查詢股票歷史股價

**Request**:
```
GET /api/market-data/prices/2330?startDate=2024-01-01&endDate=2024-12-31&days=30
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| stockId | String | 股票代碼 |

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| startDate | Date | N | 開始日期（yyyy-MM-dd） | - |
| endDate | Date | N | 結束日期（yyyy-MM-dd） | - |
| days | Integer | N | 查詢天數（與日期範圍擇一） | 30 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": [
        {
            "price_id": 1,
            "stock_id": "2330",
            "trade_date": "2024-12-31",
            "open_price": 1040.00,
            "high_price": 1055.00,
            "low_price": 1035.00,
            "close_price": 1050.00,
            "volume": 28500000,
            "turnover": 29842500000.00,
            "transactions": 18500,
            "change_price": 10.00,
            "change_percent": 0.96,
            "created_at": "2026-01-01 14:53:02",
            "updated_at": "2026-01-01 14:53:02"
        },
        {
            "price_id": 2,
            "stock_id": "2330",
            "trade_date": "2024-12-30",
            "open_price": 1025.00,
            "high_price": 1042.00,
            "low_price": 1020.00,
            "close_price": 1040.00,
            "volume": 26800000,
            "turnover": 27777200000.00,
            "transactions": 17200,
            "change_price": 15.00,
            "change_percent": 1.46,
            "created_at": "2026-01-01 14:53:02",
            "updated_at": "2026-01-01 14:53:02"
        }
    ],
    "timestamp": "2026-01-01T22:54:08.5184446+08:00"
}
```

---

#### API-M06-008: 查詢最新股價

**Request**:
```
GET /api/market-data/prices/2330/latest
```

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "price_id": 1,
        "stock_id": "2330",
        "trade_date": "2024-12-31",
        "open_price": 1040.00,
        "high_price": 1055.00,
        "low_price": 1035.00,
        "close_price": 1050.00,
        "volume": 28500000,
        "turnover": 29842500000.00,
        "transactions": 18500,
        "change_price": 10.00,
        "change_percent": 0.96,
        "created_at": "2026-01-01 14:53:02",
        "updated_at": "2026-01-01 14:53:02"
    },
    "timestamp": "2026-01-01T22:53:07.6581609+08:00"
}
```

---

#### API-M06-009: 查詢股價統計資訊（含技術指標）

**Request**:
```
GET /api/market-data/prices/2330/statistics?days=60
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| days | Integer | N | 查詢天數 | 60 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": [
        {
            "stockId": "2330",
            "tradeDate": "2024-12-31",
            "closePrice": 1050.00,
            "volume": 28500000,
            "changePercent": 0.96,
            "ma5": 1027.0000000000000000,
            "ma20": 950.5000000000000000,
            "volumeMa5": 25760000
        },
        {
            "stockId": "2330",
            "tradeDate": "2024-12-30",
            "closePrice": 1040.00,
            "volume": 26800000,
            "changePercent": 1.46,
            "ma5": 1016.0000000000000000,
            "ma20": 940.2500000000000000,
            "volumeMa5": 24640000
        }
    ],
    "timestamp": "2026-01-01T22:56:05.4451553+08:00"
}
```

---

#### API-M06-010: 查詢法人買賣超

**Request**:
```
GET /api/market-data/institutional/2330?startDate=2024-12-01&endDate=2024-12-31&days=30
```

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "trading_id": 1,
      "stock_id": "2330",
      "trade_date": "2025-12-31",
      "foreign_buy": 50000,
      "foreign_sell": 30000,
      "foreign_net": 20000,
      "trust_buy": 10000,
      "trust_sell": 8000,
      "trust_net": 2000,
      "dealer_buy": 5000,
      "dealer_sell": 6000,
      "dealer_net": -1000,
      "total_net": 21000
    }
  ],
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

#### API-M06-011: 查詢融資融券

**Request**:
```
GET /api/market-data/margin/2330?startDate=2025-12-01&endDate=2025-12-31&days=30
```

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": [
    {
      "margin_id": 1,
      "stock_id": "2330",
      "trade_date": "2025-12-31",
      "margin_purchase": 1000,
      "margin_sell": 800,
      "margin_balance": 50000,
      "margin_quota": 200000,
      "margin_usage_rate": 25.50,
      "short_purchase": 600,
      "short_sell": 500,
      "short_balance": 20000,
      "short_quota": 100000,
      "short_usage_rate": 20.00
    }
  ],
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

## 交易日曆 API

#### API-M06-012: 查詢指定日期的交易日曆

**Request**:
```
GET /api/trading-calendar/2025-01-07
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| date | Date | 日期（yyyy-MM-dd） |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "calendar_id": 11,
        "calendar_date": "2025-01-07",
        "is_trading_day": true,
        "day_type": "TRADING",
        "year": 2025,
        "month": 1,
        "day_of_week": 2
    },
    "timestamp": "2026-01-01T22:58:52.988523+08:00"
}
```

---

#### API-M06-013: 檢查是否為交易日

**Request**:
```
GET /api/trading-calendar/is-trading-day/2026-01-05
```

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": false,
    "timestamp": "2026-01-01T22:59:24.0345691+08:00"
}
```

---

#### API-M06-014: 查詢日期範圍內的交易日

**Request**:
```
GET /api/trading-calendar/range?startDate=2025-01-01&endDate=2025-01-31
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 |
|-----|------|------|------|
| startDate | Date | Y | 開始日期（yyyy-MM-dd） |
| endDate | Date | Y | 結束日期（yyyy-MM-dd） |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": [
        {
            "calendar_id": 6,
            "calendar_date": "2025-01-02",
            "is_trading_day": true,
            "day_type": "TRADING",
            "year": 2025,
            "month": 1,
            "day_of_week": 4
        },
        {
            "calendar_id": 7,
            "calendar_date": "2025-01-03",
            "is_trading_day": true,
            "day_type": "TRADING",
            "year": 2025,
            "month": 1,
            "day_of_week": 5
        }
    ],
    "timestamp": "2026-01-01T22:59:59.9382003+08:00"
}
```

---

#### API-M06-015: 查詢下一個交易日

**Request**:
```
GET /api/trading-calendar/next-trading-day?date=2025-01-01
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| date | Date | N | 參考日期（yyyy-MM-dd） | 今天 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "calendar_id": 6,
        "calendar_date": "2025-01-02",
        "is_trading_day": true,
        "day_type": "TRADING",
        "year": 2025,
        "month": 1,
        "day_of_week": 4
    },
    "timestamp": "2026-01-01T23:00:40.1626369+08:00"
}
```

---

#### API-M06-016: 查詢上一個交易日

**Request**:
```
GET /api/trading-calendar/previous-trading-day?date=2025-01-09
```

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": {
        "calendar_id": 13,
        "calendar_date": "2025-01-09",
        "is_trading_day": true,
        "day_type": "TRADING",
        "year": 2025,
        "month": 1,
        "day_of_week": 4
    },
    "timestamp": "2026-01-01T23:01:02.8356883+08:00"
}
```

---

#### API-M06-017: 查詢年度所有交易日

**Request**:
```
GET /api/trading-calendar/year?year=2025
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| year | Integer | N | 年份 | 當年 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": [
        {
            "calendar_id": 6,
            "calendar_date": "2025-01-02",
            "is_trading_day": true,
            "day_type": "TRADING",
            "year": 2025,
            "month": 1,
            "day_of_week": 4
        },
        {
            "calendar_id": 7,
            "calendar_date": "2025-01-03",
            "is_trading_day": true,
            "day_type": "TRADING",
            "year": 2025,
            "month": 1,
            "day_of_week": 5
         }
    ],
    "timestamp": "2026-01-01T23:10:35.9990166+08:00"
}
```

---

## 資料品質 API

#### API-M06-018: 查詢所有啟用的檢核規則

**Request**:
```
GET /api/data-quality/checks
```

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": [
        {
            "check_id": 1,
            "check_name": "股價四價合理性",
            "check_type": "PRICE_VALIDATION",
            "target_table": "stock_prices",
            "check_rule": "high_price >= low_price AND high_price >= open_price AND high_price >= close_price AND low_price <= open_price AND low_price <= close_price",
            "severity": "HIGH",
            "is_active": true
        },
        {
            "check_id": 2,
            "check_name": "股價不可為負",
            "check_type": "RANGE_CHECK",
            "target_table": "stock_prices",
            "check_rule": "open_price > 0 AND high_price > 0 AND low_price > 0 AND close_price > 0",
            "severity": "HIGH",
            "is_active": true
        }
    ],
    "timestamp": "2026-01-01T23:02:17.5201135+08:00"
}
```

---

#### API-M06-019: 查詢指定表的檢核規則

**Request**:
```
GET /api/data-quality/checks/stock_prices
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| targetTable | String | 目標表名稱 |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": [
        {
            "check_id": 1,
            "check_name": "股價四價合理性",
            "check_type": "PRICE_VALIDATION",
            "target_table": "stock_prices",
            "check_rule": "high_price >= low_price AND high_price >= open_price AND high_price >= close_price AND low_price <= open_price AND low_price <= close_price",
            "severity": "HIGH",
            "is_active": true
        },
        {
            "check_id": 2,
            "check_name": "股價不可為負",
            "check_type": "RANGE_CHECK",
            "target_table": "stock_prices",
            "check_rule": "open_price > 0 AND high_price > 0 AND low_price > 0 AND close_price > 0",
            "severity": "HIGH",
            "is_active": true
         }
    ],
    "timestamp": "2026-01-01T23:02:39.2574003+08:00"
}
```

---

#### API-M06-020: 分頁查詢品質問題

**Request**:
```
GET /api/data-quality/issues?status=OPEN&severity=HIGH&page=1&size=20
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| status | String | N | 狀態（OPEN/RESOLVED/IGNORED） | - |
| severity | String | N | 嚴重性（LOW/MEDIUM/HIGH） | - |
| page | Integer | N | 頁碼 | 1 |
| size | Integer | N | 每頁筆數 | 20 |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [
      {
        "issue_id": 123,
        "check_id": 1,
        "check_name": "股價四價合理性",
        "issue_date": "2026-01-01",
        "affected_rows": 1,
        "issue_detail": "股價低價高於收盤價",
        "severity": "HIGH",
        "status": "OPEN",
        "resolved_at": null,
        "resolved_by": null,
        "notes": null,
        "created_at": "2026-01-01 09:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total_items": 5,
      "total_pages": 1,
      "has_next": false,
      "has_prev": false
    }
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}


```

---

#### API-M06-021: 查詢未解決的品質問題

**Request**:
```
GET /api/data-quality/issues/open?page=1&size=20
```

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [
      {
        "issue_id": 123,
        "check_id": 1,
        "check_name": "股價四價合理性",
        "issue_date": "2026-01-01",
        "affected_rows": 1,
        "issue_detail": "股價低價高於收盤價",
        "severity": "HIGH",
        "status": "OPEN",
        "resolved_at": null,
        "resolved_by": null,
        "notes": null,
        "created_at": "2026-01-01 09:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total_items": 5,
      "total_pages": 1,
      "has_next": false,
      "has_prev": false
    }
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

#### API-M06-022: 查詢品質問題詳情

**Request**:
```
GET /api/data-quality/issues/123
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| issueId | Long | 問題 ID |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "issue_id": 123,
    "check_id": 1,
    "check_name": "股價四價合理性",
    "issue_date": "2026-01-01",
    "affected_rows": 1,
    "issue_detail": "股價低價高於收盤價",
    "severity": "HIGH",
    "status": "OPEN",
    "resolved_at": null,
    "resolved_by": null,
    "notes": null,
    "created_at": "2026-01-01 09:00:00"
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}


```

---

#### API-M06-023: 查詢資料品質統計摘要

**Request**:
```
GET /api/data-quality/summary
```

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "total_active_checks": 12,
    "total_open_issues": 5,
    "high_severity_issues": 2,
    "medium_severity_issues": 3,
    "low_severity_issues": 0,
    "today_new_issues": 1,
    "today_resolved_issues": 0,
    "quality_score": 96
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

#### API-M06-024: 手動觸發品質檢核

**Request**:
```
POST /api/data-quality/run-check?targetTable=stock_prices
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 |
|-----|------|------|------|
| targetTable | String | N | 目標表（空則檢核所有表） |

**Response** (成功):
```json
{
    "code": 200,
    "message": "Success",
    "data": "Quality check triggered successfully",
    "timestamp": "2026-01-01T22:59:24.0345691+08:00"
}
```

---

## Job 管理 API

#### API-M06-025: 分頁查詢 Job 執行記錄

**Request**:
```
GET /api/jobs/executions?jobName=SYNC_STOCK_PRICES&jobStatus=SUCCESS&page=1&size=20
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| jobName | String | N | Job 名稱 | - |
| jobStatus | String | N | Job 狀態（RUNNING/SUCCESS/FAILED） | - |
| page | Integer | N | 頁碼 | 1 |
| size | Integer | N | 每頁筆數 | 20 |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "items": [
      {
        "execution_id": 12345,
        "job_name": "SYNC_STOCK_PRICES",
        "job_type": "SCHEDULED",
        "job_status": "SUCCESS",
        "parameters": {
          "trade_date": "2026-01-01"
        },
        "start_time": "2026-01-01 09:00:00",
        "end_time": "2026-01-01 09:05:30",
        "duration_ms": 330000,
        "total_items": 1800,
        "processed_items": 1800,
        "success_items": 1798,
        "failed_items": 2,
        "error_message": null,
        "retry_count": 0,
        "trigger_type": "CRON",
        "triggered_by": "system",
        "created_at": "2026-01-01 09:00:00"
      }
    ],
    "pagination": {
      "page": 1,
      "page_size": 20,
      "total_items": 100,
      "total_pages": 5,
      "has_next": true,
      "has_prev": false
    }
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

#### API-M06-026: 查詢單一 Job 執行詳情

**Request**:
```
GET /api/jobs/executions/12345
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| executionId | Long | 執行 ID |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": 12345,
    "job_name": "SYNC_STOCK_PRICES",
    "job_type": "SCHEDULED",
    "job_status": "SUCCESS",
    "parameters": {
      "trade_date": "2026-01-01",
      "force": false
    },
    "start_time": "2026-01-01 09:00:00",
    "end_time": "2026-01-01 09:05:30",
    "duration_ms": 330000,
    "total_items": 1800,
    "processed_items": 1800,
    "success_items": 1798,
    "failed_items": 2,
    "error_message": null,
    "retry_count": 0,
    "trigger_type": "CRON",
    "triggered_by": "system",
    "created_at": "2026-01-01 09:00:00"
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

#### API-M06-027: 查詢 Job 狀態總覽

**Request**:
```
GET /api/jobs/status
```

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "running_jobs": 1,
    "today_success_jobs": 9,
    "today_failed_jobs": 1,
    "running_job_list": [
      {
        "execution_id": 12346,
        "job_name": "SYNC_STOCK_PRICES",
        "start_time": "2026-01-01 10:30:00",
        "processed_items": 120
      }
    ]
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

#### API-M06-028: 手動觸發股價同步 Job

**Request**:
```
POST /api/jobs/trigger/stock-price-sync?tradeDate=2026-01-01
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| tradeDate | Date | N | 交易日期（yyyy-MM-dd） | 今天 |

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": 12346,
    "job_name": "SYNC_STOCK_PRICES",
    "job_type": "MANUAL",
    "job_status": "RUNNING",
    "parameters": {
      "trade_date": "2026-01-01"
    },
    "start_time": "2026-01-01 10:30:00",
    "end_time": null,
    "duration_ms": null,
    "total_items": null,
    "processed_items": 0,
    "success_items": 0,
    "failed_items": 0,
    "error_message": null,
    "retry_count": 0,
    "trigger_type": "MANUAL",
    "triggered_by": "chris",
    "created_at": "2026-01-01 10:30:00"
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

#### API-M06-029: 手動觸發財報同步 Job

**Request**:
```
POST /api/jobs/trigger/financial-sync
```

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": 12346,
    "job_name": "SYNC_STOCK_PRICES",
    "job_type": "MANUAL",
    "job_status": "RUNNING",
    "parameters": {
      "trade_date": "2026-01-01"
    },
    "start_time": "2026-01-01 10:30:00",
    "end_time": null,
    "duration_ms": null,
    "total_items": null,
    "processed_items": 0,
    "success_items": 0,
    "failed_items": 0,
    "error_message": null,
    "retry_count": 0,
    "trigger_type": "MANUAL",
    "triggered_by": "chris",
    "created_at": "2026-01-01 10:30:00"
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

#### API-M06-030: 手動觸發資料品質檢核 Job

**Request**:
```
POST /api/jobs/trigger/data-quality-check
```

**Response** (成功):
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "execution_id": 12346,
    "job_name": "SYNC_STOCK_PRICES",
    "job_type": "MANUAL",
    "job_status": "RUNNING",
    "parameters": {
      "trade_date": "2026-01-01"
    },
    "start_time": "2026-01-01 10:30:00",
    "end_time": null,
    "duration_ms": null,
    "total_items": null,
    "processed_items": 0,
    "success_items": 0,
    "failed_items": 0,
    "error_message": null,
    "retry_count": 0,
    "trigger_type": "MANUAL",
    "triggered_by": "chris",
    "created_at": "2026-01-01 10:30:00"
  },
  "timestamp": "2026-01-01T10:30:00+08:00"
}

```

---

### 4.3 錯誤碼定義

遵守 [全系統契約 - 錯誤碼規範](../technical/00-全系統契約.md#4-api-統一規範)。

| 錯誤碼 | HTTP Status | 說明 | 處理建議 |
|-------|------------|------|---------|
| M06_STOCK_001 | 404 | 股票不存在 | 檢查股票代碼是否正確 |
| M06_DATA_001 | 404 | 查無資料 | 確認日期範圍或補齊資料 |
| M06_PARAM_001 | 400 | 參數錯誤 | 檢查參數格式與值域 |
| M06_VALIDATION_001 | 422 | 資料驗證失敗 | 檢查資料合理性 |
| M06_SOURCE_001 | 503 | 資料源暫時不可用 | 稍後重試或切換資料源 |
| M06_JOB_001 | 409 | Job 已在執行中 | 等待當前 Job 完成 |
| M06_DB_001 | 500 | 資料庫錯誤 | 聯絡系統管理員 |
| M06_DQ_001 | 422 | 資料品質問題 | 查看問題詳情並修正 |

---

## 📚 相關文檔

- [全系統契約 - API 統一規範](../technical/00-全系統契約.md#4-api-統一規範)
- [M06 功能需求](../functional/M06-資料管理功能需求.md)
- [M06 資料庫設計](../../design/M06-資料庫設計.md)
- [M06 業務流程](../../design/M06-業務流程.md)

---

**文件維護者**: API 設計師
**審核者**: 架構師
**最後更新**: 2026-01-01
**下次審核**: 2026-02-01
