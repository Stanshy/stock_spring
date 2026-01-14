# M09-籌碼分析模組 API 規格

> **文件編號**: API-M09
> **模組名稱**: 籌碼分析模組
> **版本**: v1.0
> **最後更新**: 2026-01-11
> **狀態**: Draft

---

## 📋 API 總覽

### 基礎資訊

| 項目 | 值 |
|-----|-----|
| Base URL | `/api/v1/chip` |
| 認證方式 | JWT Bearer Token |
| 內容類型 | application/json |
| 字元編碼 | UTF-8 |

### API 清單

| 端點 | 方法 | 說明 | 優先級 |
|-----|------|------|-------|
| `/api/v1/chip/{stockId}/institutional` | GET | 查詢三大法人指標 | P0 |
| `/api/v1/chip/{stockId}/margin` | GET | 查詢融資融券指標 | P0 |
| `/api/v1/chip/{stockId}/signals` | GET | 查詢籌碼異常訊號 | P0 |
| `/api/v1/chip/{stockId}/analysis` | GET | 查詢完整籌碼分析 | P0 |
| `/api/v1/chip/{stockId}/analysis` | POST | 執行即時籌碼計算 | P1 |
| `/api/v1/chip/ranking/{rankType}` | GET | 查詢籌碼排行榜 | P1 |
| `/api/v1/chip/scan/signals` | GET | 全市場異常訊號掃描 | P1 |
| `/api/v1/chip/{stockId}/concentration` | GET | 查詢籌碼集中度 | P1 |
| `/api/v1/chip/{stockId}/cost` | GET | 查詢主力成本估算 | P2 |
| `/api/v1/chip/metadata/indicators` | GET | 查詢支援的指標清單 | P2 |

---

## 1. 三大法人指標查詢

### GET `/api/v1/chip/{stockId}/institutional`

查詢指定股票的三大法人買賣超指標。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼（如 2330） |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| start_date | string | N | 60天前 | 開始日期（YYYY-MM-DD） |
| end_date | string | N | 今日 | 結束日期（YYYY-MM-DD） |
| indicators | string | N | all | 指定指標（逗號分隔，如 `foreign_net,trust_net`） |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "latest_date": "2024-12-24",
    "latest_indicators": {
      "foreign_net": 5000000,
      "foreign_buy": 25000000,
      "foreign_sell": 20000000,
      "foreign_net_ma5": 3500000,
      "foreign_net_ma20": 2800000,
      "foreign_continuous_days": 5,
      "trust_net": 800000,
      "trust_buy": 1500000,
      "trust_sell": 700000,
      "trust_net_ma5": 600000,
      "trust_continuous_days": 3,
      "dealer_net": -200000,
      "dealer_buy": 500000,
      "dealer_sell": 700000,
      "total_net": 5600000,
      "institutional_agreement": "BULLISH"
    },
    "history": [
      {
        "trade_date": "2024-12-24",
        "foreign_net": 5000000,
        "trust_net": 800000,
        "dealer_net": -200000,
        "total_net": 5600000
      },
      {
        "trade_date": "2024-12-23",
        "foreign_net": 3500000,
        "trust_net": 500000,
        "dealer_net": 100000,
        "total_net": 4100000
      }
    ],
    "summary": {
      "accumulated_20d_foreign": 45000000,
      "accumulated_20d_trust": 8000000,
      "accumulated_20d_total": 52000000
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_001"
}
```

#### 錯誤回應

| HTTP Status | 錯誤碼 | 說明 |
|-------------|-------|------|
| 404 | M09_CHIP_001 | 股票代碼不存在 |
| 400 | M09_CHIP_002 | 籌碼資料不足 |
| 400 | M09_PARAM_002 | 日期範圍無效 |

---

## 2. 融資融券指標查詢

### GET `/api/v1/chip/{stockId}/margin`

查詢指定股票的融資融券指標。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| start_date | string | N | 60天前 | 開始日期 |
| end_date | string | N | 今日 | 結束日期 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "latest_date": "2024-12-24",
    "latest_indicators": {
      "margin_balance": 125000,
      "margin_change": 5000,
      "margin_quota": 280000,
      "margin_usage_rate": 44.64,
      "margin_continuous_days": 3,
      "short_balance": 8000,
      "short_change": -500,
      "short_quota": 64000,
      "short_usage_rate": 12.50,
      "margin_short_ratio": 6.40,
      "margin_change_ma5": 3200,
      "margin_usage_ma20": 42.50
    },
    "history": [
      {
        "trade_date": "2024-12-24",
        "margin_balance": 125000,
        "margin_change": 5000,
        "margin_usage_rate": 44.64,
        "short_balance": 8000,
        "short_change": -500,
        "margin_short_ratio": 6.40
      }
    ],
    "summary": {
      "margin_20d_high": 130000,
      "margin_20d_low": 110000,
      "usage_rate_avg_20d": 42.50,
      "margin_trend": "INCREASING"
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_002"
}
```

---

## 3. 籌碼異常訊號查詢

### GET `/api/v1/chip/{stockId}/signals`

查詢指定股票的籌碼異常訊號。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| start_date | string | N | 30天前 | 開始日期 |
| end_date | string | N | 今日 | 結束日期 |
| severity | string | N | all | 嚴重度過濾（CRITICAL, HIGH, MEDIUM, LOW） |
| signal_type | string | N | all | 訊號類型（INSTITUTIONAL, MARGIN） |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "total_signals": 3,
    "signals": [
      {
        "signal_id": "CHIP_SIG_001",
        "signal_name": "外資大買",
        "signal_type": "INSTITUTIONAL",
        "severity": "HIGH",
        "trade_date": "2024-12-24",
        "value": 15000000,
        "threshold": 8000000,
        "deviation": 2.5,
        "description": "外資買超 15,000,000 股，超過 20 日平均 2.5 個標準差",
        "recommendation": "留意法人動向，可能有利多消息"
      },
      {
        "signal_id": "CHIP_SIG_003",
        "signal_name": "外資連續買超",
        "signal_type": "INSTITUTIONAL",
        "severity": "MEDIUM",
        "trade_date": "2024-12-24",
        "value": 5,
        "threshold": 5,
        "description": "外資已連續 5 天買超"
      },
      {
        "signal_id": "CHIP_SIG_007",
        "signal_name": "三大法人同買",
        "signal_type": "INSTITUTIONAL",
        "severity": "MEDIUM",
        "trade_date": "2024-12-24",
        "description": "外資、投信、自營商今日皆為買超"
      }
    ],
    "signal_summary": {
      "critical_count": 0,
      "high_count": 1,
      "medium_count": 2,
      "low_count": 0
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_003"
}
```

---

## 4. 完整籌碼分析查詢

### GET `/api/v1/chip/{stockId}/analysis`

一次取得指定股票的完整籌碼分析結果。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| date | string | N | 今日 | 分析日期 |
| include_history | boolean | N | false | 是否包含歷史資料 |
| history_days | integer | N | 20 | 歷史資料天數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "analysis_date": "2024-12-24",
    "institutional_analysis": {
      "foreign_net": 5000000,
      "foreign_continuous_days": 5,
      "foreign_accumulated_20d": 45000000,
      "trust_net": 800000,
      "trust_continuous_days": 3,
      "dealer_net": -200000,
      "total_net": 5600000,
      "trend": "BULLISH",
      "strength": "STRONG"
    },
    "margin_analysis": {
      "margin_balance": 125000,
      "margin_usage_rate": 44.64,
      "margin_trend": "INCREASING",
      "short_balance": 8000,
      "margin_short_ratio": 6.40,
      "retail_sentiment": "BULLISH"
    },
    "concentration_analysis": {
      "institutional_ratio": 75.5,
      "retail_ratio": 24.5,
      "concentration_trend": "CONCENTRATING",
      "concentration_change_20d": 2.5
    },
    "signals": [
      {
        "signal_id": "CHIP_SIG_001",
        "signal_name": "外資大買",
        "severity": "HIGH"
      }
    ],
    "overall_assessment": {
      "chip_score": 85,
      "chip_grade": "A",
      "interpretation": "籌碼面偏多，法人持續買超，散戶融資維持穩定"
    },
    "diagnostics": {
      "calculation_time_ms": 45,
      "data_completeness": 100,
      "last_update": "2024-12-24T15:00:00+08:00"
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_004"
}
```

---

## 5. 即時籌碼計算

### POST `/api/v1/chip/{stockId}/analysis`

執行即時籌碼指標計算（強制重新計算，不使用快取）。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 請求主體

```json
{
  "calculation_plan": {
    "include_institutional": true,
    "include_margin": true,
    "include_concentration": true,
    "include_cost": false,
    "include_signals": true
  },
  "lookback_period": 60,
  "force_recalculate": true
}
```

#### 成功回應 (200)

回應格式同 `GET /api/v1/chip/{stockId}/analysis`。

---

## 6. 籌碼排行榜查詢

### GET `/api/v1/chip/ranking/{rankType}`

查詢籌碼排行榜。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| rankType | string | Y | 排行榜類型（見下表） |

**排行榜類型**:

| rankType | 說明 |
|----------|------|
| foreign_buy | 外資買超排行 |
| foreign_sell | 外資賣超排行 |
| trust_buy | 投信買超排行 |
| trust_sell | 投信賣超排行 |
| foreign_continuous | 外資連續買超天數排行 |
| margin_increase | 融資增加排行 |
| margin_decrease | 融資減少排行 |
| margin_short_ratio | 券資比排行 |
| total_institutional | 三大法人合計買超排行 |
| accumulated_foreign | N日外資累計買超排行 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| trade_date | string | N | 最近交易日 | 交易日期 |
| market_type | string | N | all | 市場類型（TWSE, OTC） |
| limit | integer | N | 50 | 回傳筆數（1-100） |
| min_volume | integer | N | 0 | 最低成交量過濾 |
| days | integer | N | 1 | 累計天數（用於 accumulated 類型） |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "rank_type": "foreign_buy",
    "rank_name": "外資買超排行",
    "trade_date": "2024-12-24",
    "total_count": 50,
    "rankings": [
      {
        "rank": 1,
        "stock_id": "2330",
        "stock_name": "台積電",
        "market_type": "TWSE",
        "industry": "半導體業",
        "value": 15000000,
        "value_unit": "股",
        "change_from_yesterday": 5000000,
        "close_price": 580.00,
        "volume": 35000000
      },
      {
        "rank": 2,
        "stock_id": "2317",
        "stock_name": "鴻海",
        "market_type": "TWSE",
        "industry": "其他電子業",
        "value": 12000000,
        "value_unit": "股",
        "change_from_yesterday": 3000000,
        "close_price": 105.50,
        "volume": 45000000
      }
    ]
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_005"
}
```

---

## 7. 全市場異常訊號掃描

### GET `/api/v1/chip/scan/signals`

掃描全市場籌碼異常訊號。

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| trade_date | string | N | 最近交易日 | 交易日期 |
| market_type | string | N | all | 市場類型 |
| severity | string | N | all | 嚴重度過濾 |
| signal_type | string | N | all | 訊號類型過濾 |
| limit | integer | N | 100 | 回傳筆數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "trade_date": "2024-12-24",
    "scan_time_ms": 3500,
    "total_stocks_scanned": 1800,
    "total_signals_found": 45,
    "signal_summary": {
      "critical": 2,
      "high": 15,
      "medium": 20,
      "low": 8
    },
    "signals": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "signal_id": "CHIP_SIG_001",
        "signal_name": "外資大買",
        "severity": "HIGH",
        "value": 15000000,
        "description": "外資買超 15,000,000 股"
      },
      {
        "stock_id": "2454",
        "stock_name": "聯發科",
        "signal_id": "CHIP_SIG_010",
        "signal_name": "融資斷頭",
        "severity": "CRITICAL",
        "value": -15.5,
        "description": "融資大幅減少 15.5%"
      }
    ]
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_006"
}
```

---

## 8. 籌碼集中度查詢

### GET `/api/v1/chip/{stockId}/concentration`

查詢指定股票的籌碼集中度分析。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| lookback_days | integer | N | 60 | 回溯天數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "analysis_date": "2024-12-24",
    "concentration": {
      "institutional_ratio": 75.5,
      "retail_ratio": 24.5,
      "foreign_ratio": 70.2,
      "trust_ratio": 3.8,
      "dealer_ratio": 1.5
    },
    "trend": {
      "concentration_trend": "CONCENTRATING",
      "change_5d": 0.8,
      "change_20d": 2.5,
      "change_60d": 5.2
    },
    "margin_analysis": {
      "margin_to_capital": 0.48,
      "margin_trend": "STABLE"
    },
    "interpretation": "籌碼持續向法人集中，散戶持股比例降低"
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_007"
}
```

---

## 9. 主力成本估算查詢

### GET `/api/v1/chip/{stockId}/cost`

查詢主力持股成本估算。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| lookback_days | integer | N | 120 | 成本計算回溯天數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "analysis_date": "2024-12-24",
    "current_price": 580.00,
    "cost_estimation": {
      "foreign_avg_cost": 545.50,
      "foreign_profit_rate": 6.32,
      "foreign_holding_days_avg": 45,
      "trust_avg_cost": 560.20,
      "trust_profit_rate": 3.53,
      "trust_holding_days_avg": 30
    },
    "methodology": "加權平均成本法",
    "disclaimer": "此為估算值，僅供參考",
    "calculation_period": {
      "start_date": "2024-08-24",
      "end_date": "2024-12-24",
      "trading_days": 85
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_008"
}
```

---

## 10. 指標元數據查詢

### GET `/api/v1/chip/metadata/indicators`

查詢支援的籌碼指標清單與說明。

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "total_indicators": 28,
    "categories": [
      {
        "category": "INSTITUTIONAL",
        "category_name": "三大法人指標",
        "indicators": [
          {
            "indicator_id": "INST001",
            "name": "foreign_net",
            "display_name": "外資買賣超",
            "description": "外資買進股數減去賣出股數",
            "unit": "股",
            "priority": "P0"
          },
          {
            "indicator_id": "INST005",
            "name": "foreign_net_ma5",
            "display_name": "外資買賣超5日均",
            "description": "外資買賣超的5日簡單移動平均",
            "unit": "股",
            "priority": "P0"
          }
        ]
      },
      {
        "category": "MARGIN",
        "category_name": "融資融券指標",
        "indicators": [
          {
            "indicator_id": "MRGN001",
            "name": "margin_balance",
            "display_name": "融資餘額",
            "description": "當日融資餘額張數",
            "unit": "張",
            "priority": "P0"
          }
        ]
      }
    ],
    "signals": [
      {
        "signal_id": "CHIP_SIG_001",
        "name": "外資大買",
        "type": "INSTITUTIONAL",
        "severity": "HIGH",
        "description": "外資單日買超超過20日平均2個標準差"
      }
    ]
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_009"
}
```

---

## 共用錯誤回應格式

```json
{
  "code": 400,
  "message": "Bad Request",
  "error": {
    "error_code": "M09_CHIP_002",
    "error_message": "籌碼資料不足，無法計算",
    "error_detail": "股票 2330 僅有 10 個交易日資料，至少需要 20 個交易日",
    "suggestion": "請確認資料同步是否完成，或減少回溯天數"
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_chip_err_001"
}
```

---

## 📚 相關文檔

- [M09 功能需求](../functional/M09-籌碼分析功能需求.md)
- [M09 資料庫設計](../../design/M09-資料庫設計.md)
- [API 回應格式總綱](../technical/00-全系統契約.md#44-api-回應格式)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-11
**下次審核**: 2026-03-31
