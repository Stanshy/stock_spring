# M08-基本面分析模組 API 規格

> **文件編號**: API-M08  
> **模組名稱**: 基本面分析模組  
> **版本**: v2.0  
> **最後更新**: 2025-12-31  
> **狀態**: Draft

---

## 📋 API 總覽

本文件定義 基本面分析模組的所有 REST API 規格。

---

## 4. API 設計

### 4.1 API 列表總覽

| API 編號 | 端點 | 方法 | 說明 | 權限 |
|---------|------|------|------|------|
| API-M08-001 | /api/stocks/{stockId}/fundamentals | GET | 查詢財務指標 | USER |
| API-M08-002 | /api/stocks/{stockId}/scores | GET | 查詢綜合評分 | USER |
| API-M08-003 | /api/stocks/{stockId}/alerts | GET | 查詢財務警示 | USER |
| API-M08-004 | /api/fundamentals/batch | POST | 批次查詢財務指標 | USER |
| API-M08-005 | /api/fundamentals/trends | POST | 查詢指標歷史趨勢 | USER |
| API-M08-006 | /api/jobs/calculate-fundamentals | POST | 手動觸發財務指標計算 | ADMIN |

### 4.2 核心 API 設計

#### API-M08-001: 查詢財務指標

**Request**:
```
GET /api/stocks/2330/fundamentals?year=2024&quarter=3&indicators=pe_ratio,roe,debt_ratio
Authorization: Bearer {jwt_token}
```

**Path Parameters**:
| 參數 | 類型 | 說明 |
|-----|------|------|
| stockId | String | 股票代碼（如 2330） |

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 | 預設值 |
|-----|------|------|------|-------|
| year | Integer | N | 年度 | 最新年度 |
| quarter | Integer | N | 季度（1-4） | 最新季度 |
| indicators | String | N | 指標清單（逗號分隔） | 全部 |

**Response** (遵守總綱 4.4 API 統一規範):
```json
{
  "success": true,
  "timestamp": "2024-12-27T10:30:00+08:00",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "year": 2024,
    "quarter": 3,
    "report_type": "Q",
    "calculation_date": "2024-11-14",
    "stock_price": 580.00,
    "valuation": {
      "pe_ratio": 18.50,
      "pb_ratio": 3.20,
      "ps_ratio": 4.50,
      "peg_ratio": 1.25
    },
    "profitability": {
      "roe": 26.70,
      "roa": 18.70,
      "gross_margin": 53.50,
      "operating_margin": 42.30,
      "net_margin": 41.20,
      "eps": 36.05
    },
    "financial_structure": {
      "debt_to_equity": 0.35,
      "debt_ratio": 26.00,
      "equity_ratio": 74.00
    },
    "solvency": {
      "current_ratio": 2.10,
      "quick_ratio": 1.85,
      "cash_ratio": 1.20
    },
    "cash_flow": {
      "free_cash_flow": 700000000000,
      "fcf_yield": 4.67,
      "operating_cash_flow": 1000000000000
    },
    "growth": {
      "revenue_growth_yoy": 18.50,
      "eps_growth_yoy": 25.30,
      "net_income_growth_yoy": 22.80
    },
    "dividend": {
      "dividend_yield": 1.90,
      "dividend_payout_ratio": 42.50,
      "dividend_per_share": 11.00
    }
  },
  "error": null
}
```

**Response** (股票不存在):
```json
{
  "success": false,
  "timestamp": "2024-12-27T10:30:00+08:00",
  "data": null,
  "error": {
    "error_code": "M08_STOCK_001",
    "error_message": "股票不存在",
    "error_detail": "Stock not found: 9999",
    "trace_id": "req_abc123",
    "path": "/api/stocks/9999/fundamentals"
  }
}
```

---

#### API-M08-002: 查詢綜合評分

**Request**:
```
GET /api/stocks/2330/scores?year=2024&quarter=3
Authorization: Bearer {jwt_token}
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 |
|-----|------|------|------|
| year | Integer | N | 年度（預設最新） |
| quarter | Integer | N | 季度（1-4，預設最新） |

**Response** (成功):
```json
{
  "success": true,
  "timestamp": "2024-12-27T10:30:00+08:00",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "year": 2024,
    "quarter": 3,
    "calculation_date": "2024-11-14",
    "piotroski_f_score": 8,
    "piotroski_interpretation": "優秀（8-9分）",
    "piotroski_details": {
      "profitability": 4,
      "leverage": 3,
      "operating_efficiency": 2
    },
    "altman_z_score": 3.85,
    "altman_status": "SAFE",
    "altman_interpretation": "安全區：破產風險低",
    "beneish_m_score": -2.50,
    "beneish_status": "CLEAN",
    "beneish_interpretation": "盈餘品質良好",
    "graham_score": 9,
    "composite_score": 88.50,
    "composite_grade": "A"
  },
  "error": null
}
```

---

#### API-M08-003: 查詢財務警示

**Request**:
```
GET /api/stocks/2330/alerts?year=2024&quarter=3&severity=HIGH,CRITICAL&status=ACTIVE
Authorization: Bearer {jwt_token}
```

**Query Parameters**:
| 參數 | 類型 | 必填 | 說明 |
|-----|------|------|------|
| year | Integer | N | 年度 |
| quarter | Integer | N | 季度 |
| severity | String | N | 嚴重程度（逗號分隔） |
| status | String | N | 警示狀態 |

**Response** (成功):
```json
{
  "success": true,
  "timestamp": "2024-12-27T10:30:00+08:00",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "alerts": [
      {
        "alert_id": 12345,
        "year": 2024,
        "quarter": 3,
        "alert_type": "HIGH_DEBT_RATIO",
        "alert_category": "DEBT_RISK",
        "severity": "MEDIUM",
        "alert_message": "負債比率偏高",
        "alert_detail": {
          "current_debt_ratio": 68.50,
          "threshold": 70.00,
          "trend": "上升"
        },
        "trigger_indicator": "debt_ratio",
        "trigger_value": 68.50,
        "threshold_value": 70.00,
        "alert_status": "ACTIVE",
        "created_at": "2024-11-14T10:00:00+08:00"
      }
    ],
    "total_count": 1
  },
  "error": null
}
```

---

#### API-M08-004: 批次查詢財務指標

**Request**:
```
POST /api/fundamentals/batch
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "stock_ids": ["2330", "2317", "2454"],
  "year": 2024,
  "quarter": 3,
  "indicators": ["pe_ratio", "roe", "debt_ratio", "eps_growth_yoy"]
}
```

**Request Body**:
| 欄位 | 類型 | 必填 | 說明 |
|-----|------|------|------|
| stock_ids | Array[String] | Y | 股票代碼清單 |
| year | Integer | N | 年度 |
| quarter | Integer | N | 季度 |
| indicators | Array[String] | N | 指標清單 |

**Response** (成功):
```json
{
  "success": true,
  "timestamp": "2024-12-27T10:30:00+08:00",
  "data": {
    "results": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "year": 2024,
        "quarter": 3,
        "indicators": {
          "pe_ratio": 18.50,
          "roe": 26.70,
          "debt_ratio": 26.00,
          "eps_growth_yoy": 25.30
        }
      },
      {
        "stock_id": "2317",
        "stock_name": "鴻海",
        "year": 2024,
        "quarter": 3,
        "indicators": {
          "pe_ratio": 12.30,
          "roe": 18.50,
          "debt_ratio": 45.00,
          "eps_growth_yoy": 15.20
        }
      },
      {
        "stock_id": "2454",
        "stock_name": "聯發科",
        "year": 2024,
        "quarter": 3,
        "indicators": {
          "pe_ratio": 16.80,
          "roe": 22.30,
          "debt_ratio": 32.50,
          "eps_growth_yoy": 30.50
        }
      }
    ],
    "total_count": 3
  },
  "error": null
}
```

---

#### API-M08-005: 查詢指標歷史趨勢

**Request**:
```
POST /api/fundamentals/trends
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "stock_id": "2330",
  "indicator": "roe",
  "start_year": 2020,
  "start_quarter": 1,
  "end_year": 2024,
  "end_quarter": 3
}
```

**Response** (成功):
```json
{
  "success": true,
  "timestamp": "2024-12-27T10:30:00+08:00",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "indicator": "roe",
    "indicator_name": "股東權益報酬率",
    "unit": "%",
    "trend_data": [
      {"year": 2020, "quarter": 1, "value": 22.50},
      {"year": 2020, "quarter": 2, "value": 23.10},
      {"year": 2020, "quarter": 3, "value": 24.20},
      {"year": 2020, "quarter": 4, "value": 24.80},
      {"year": 2021, "quarter": 1, "value": 25.20},
      {"year": 2021, "quarter": 2, "value": 25.50},
      {"year": 2021, "quarter": 3, "value": 26.00},
      {"year": 2021, "quarter": 4, "value": 26.30},
      {"year": 2022, "quarter": 1, "value": 26.50},
      {"year": 2022, "quarter": 2, "value": 26.80},
      {"year": 2022, "quarter": 3, "value": 27.00},
      {"year": 2022, "quarter": 4, "value": 27.20},
      {"year": 2023, "quarter": 1, "value": 26.80},
      {"year": 2023, "quarter": 2, "value": 26.50},
      {"year": 2023, "quarter": 3, "value": 26.30},
      {"year": 2023, "quarter": 4, "value": 26.00},
      {"year": 2024, "quarter": 1, "value": 26.20},
      {"year": 2024, "quarter": 2, "value": 26.50},
      {"year": 2024, "quarter": 3, "value": 26.70}
    ],
    "statistics": {
      "min": 22.50,
      "max": 27.20,
      "avg": 25.87,
      "latest": 26.70,
      "trend": "上升",
      "volatility": 1.52
    }
  },
  "error": null
}
```

---

#### API-M08-006: 手動觸發財務指標計算（管理員）

**Request**:
```
POST /api/jobs/calculate-fundamentals
Authorization: Bearer {admin_jwt_token}
Content-Type: application/json

{
  "stock_ids": ["2330", "2317"],
  "year": 2024,
  "quarter": 3,
  "force": false
}
```

**Request Body**:
| 欄位 | 類型 | 必填 | 說明 |
|-----|------|------|------|
| stock_ids | Array[String] | N | 指定股票代碼（空則全部） |
| year | Integer | N | 指定年度（空則最新） |
| quarter | Integer | N | 指定季度（空則最新） |
| force | Boolean | N | 是否強制重新計算 |

**Response** (成功):
```json
{
  "success": true,
  "timestamp": "2024-12-27T09:05:00+08:00",
  "data": {
    "execution_id": 12345,
    "job_name": "CALCULATE_FUNDAMENTALS",
    "job_status": "RUNNING",
    "start_time": "2024-12-27T09:05:00+08:00",
    "parameters": {
      "stock_ids": ["2330", "2317"],
      "year": 2024,
      "quarter": 3,
      "force": false
    },
    "estimated_duration": "5 minutes"
  },
  "error": null
}
```

---

### 4.3 錯誤碼定義

遵守總綱 4.4 錯誤碼規範。

| 錯誤碼 | HTTP Status | 說明 | 處理建議 |
|-------|------------|------|---------|
| M08_STOCK_001 | 404 | 股票不存在 | 檢查股票代碼 |
| M08_DATA_001 | 404 | 查無財務資料 | 確認年度季度或補齊資料 |
| M08_PARAM_001 | 400 | 參數錯誤 | 檢查參數格式與值域 |
| M08_CALCULATION_001 | 422 | 財務指標計算失敗 | 檢查財報資料完整性 |
| M08_INDICATOR_001 | 400 | 不支援的指標 | 檢查指標名稱 |
| M08_JOB_001 | 409 | Job 已在執行中 | 等待當前 Job 完成 |
| M08_DB_001 | 500 | 資料庫錯誤 | 聯絡系統管理員 |

---


---

## 📚 相關文檔

- [全系統契約](../00-全系統契約.md)
- [M08 功能需求](../../functional/M08-基本面分析功能需求.md)
- [M08 資料庫設計](../../../design/M08-資料庫設計.md)

---

**文件維護者**: API 設計師  
**最後更新**: 2025-12-31
