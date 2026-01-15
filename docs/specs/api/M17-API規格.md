# M17-風險管理模組 API 規格

> **文件編號**: API-M17
> **模組名稱**: 風險管理模組 (Risk Management Module)
> **版本**: v1.0
> **最後更新**: 2026-01-15
> **狀態**: Draft

---

## 1. API 總覽

### 1.1 基礎資訊

| 項目 | 說明 |
|-----|------|
| Base URL | `/api/v1/risk` |
| 認證方式 | Bearer Token (JWT) |
| 資料格式 | JSON |
| 字元編碼 | UTF-8 |

### 1.2 API 清單

| 端點 | 方法 | 說明 | 優先級 |
|-----|------|------|:------:|
| `/portfolios/{portfolioId}/risk` | GET | 取得持倉風險概覽 | P0 |
| `/portfolios/{portfolioId}/var` | GET | 計算 VaR | P0 |
| `/portfolios/{portfolioId}/volatility` | GET | 取得波動度分析 | P0 |
| `/portfolios/{portfolioId}/risk-report` | GET | 取得風險報告 | P0 |
| `/alerts/active` | GET | 取得有效風險預警 | P0 |
| `/stocks/{stockId}/risk` | GET | 取得個股風險指標 | P0 |
| `/limits` | POST | 建立風險限額 | P1 |
| `/limits` | GET | 取得風險限額清單 | P1 |
| `/limits/{limitId}` | PUT | 更新風險限額 | P1 |
| `/limits/{limitId}` | DELETE | 刪除風險限額 | P1 |
| `/alerts` | POST | 建立風險預警 | P1 |
| `/alerts` | GET | 取得預警清單 | P1 |
| `/alerts/{alertId}` | PUT | 更新預警設定 | P1 |
| `/alerts/{alertId}/acknowledge` | POST | 確認預警 | P1 |
| `/portfolios/{portfolioId}/correlation` | GET | 取得相關性矩陣 | P1 |
| `/portfolios/{portfolioId}/concentration` | GET | 取得集中度分析 | P1 |
| `/stress-test` | POST | 執行壓力測試 | P1 |
| `/portfolios/{portfolioId}/beta` | GET | 取得 Beta 分析 | P1 |
| `/portfolios/{portfolioId}/attribution` | GET | 取得風險歸因 | P1 |
| `/portfolios/{portfolioId}/risk/history` | GET | 歷史風險查詢 | P2 |
| `/portfolios/{portfolioId}/risk/export` | GET | 匯出風險報告 | P2 |
| `/scenarios` | POST | 建立自訂情境 | P2 |
| `/scenarios` | GET | 取得情境清單 | P2 |

---

## 2. P0 核心 API

### 2.1 取得持倉風險概覽

取得投資組合的整體風險狀態。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/risk
```

**路徑參數**

| 參數 | 類型 | 說明 |
|-----|------|------|
| portfolioId | string | 投資組合 ID |

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| asOfDate | date | N | today | 計算基準日期 |
| benchmarkCode | string | N | TAIEX | 比較基準代碼 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "portfolioName": "主力持股組合",
    "asOfDate": "2026-01-15",
    "totalValue": 5000000,

    "riskSummary": {
      "riskLevel": "MEDIUM",
      "riskScore": 65,
      "status": "NORMAL",
      "activeAlerts": 2
    },

    "keyMetrics": {
      "var95Daily": {
        "value": 125000,
        "percentage": 0.025,
        "method": "HISTORICAL"
      },
      "var99Daily": {
        "value": 185000,
        "percentage": 0.037,
        "method": "HISTORICAL"
      },
      "volatility": {
        "daily": 0.018,
        "annualized": 0.285
      },
      "beta": 1.15,
      "sharpeRatio": 1.25,
      "maxDrawdown": 0.12,
      "currentDrawdown": 0.03
    },

    "limitStatus": [
      {
        "limitType": "VAR_95",
        "limitValue": 150000,
        "currentValue": 125000,
        "utilization": 0.833,
        "status": "WARNING"
      },
      {
        "limitType": "MAX_SINGLE_STOCK",
        "limitValue": 0.20,
        "currentValue": 0.18,
        "utilization": 0.90,
        "status": "WARNING"
      },
      {
        "limitType": "MAX_SECTOR",
        "limitValue": 0.40,
        "currentValue": 0.35,
        "utilization": 0.875,
        "status": "NORMAL"
      }
    ],

    "topRiskContributors": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "weight": 0.18,
        "riskContribution": 0.32,
        "marginalVar": 45000
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "weight": 0.12,
        "riskContribution": 0.15,
        "marginalVar": 22000
      }
    ],

    "calculatedAt": "2026-01-15T09:30:00+08:00"
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.2 計算 VaR

計算投資組合的 Value at Risk。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/var
```

**路徑參數**

| 參數 | 類型 | 說明 |
|-----|------|------|
| portfolioId | string | 投資組合 ID |

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| method | string | N | HISTORICAL | VaR 計算方法：HISTORICAL/PARAMETRIC/MONTE_CARLO |
| confidenceLevel | number | N | 0.95 | 信賴水準：0.95/0.99 |
| horizon | integer | N | 1 | 持有期間（天數）：1/5/10/21 |
| lookbackDays | integer | N | 252 | 歷史資料天數 |
| simulations | integer | N | 10000 | Monte Carlo 模擬次數 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "asOfDate": "2026-01-15",
    "totalValue": 5000000,

    "varResult": {
      "method": "HISTORICAL",
      "confidenceLevel": 0.95,
      "horizon": 1,
      "lookbackDays": 252,

      "var": {
        "value": 125000,
        "percentage": 0.025
      },
      "cvar": {
        "value": 165000,
        "percentage": 0.033,
        "description": "Conditional VaR (Expected Shortfall)"
      }
    },

    "varBreakdown": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "positionValue": 900000,
        "componentVar": 40000,
        "marginalVar": 0.044,
        "contribution": 0.32
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "positionValue": 600000,
        "componentVar": 18000,
        "marginalVar": 0.030,
        "contribution": 0.144
      }
    ],

    "historicalContext": {
      "worstDayLoss": {
        "date": "2024-08-05",
        "loss": 285000,
        "percentage": 0.057
      },
      "daysExceedingVar": 12,
      "expectedExceedances": 12.6,
      "backtestPValue": 0.52
    },

    "sensitivityAnalysis": {
      "varAt90": 95000,
      "varAt95": 125000,
      "varAt99": 185000,
      "varAt99_5": 220000
    },

    "calculatedAt": "2026-01-15T09:30:00+08:00"
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

**Monte Carlo 方法回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "asOfDate": "2026-01-15",
    "totalValue": 5000000,

    "varResult": {
      "method": "MONTE_CARLO",
      "confidenceLevel": 0.95,
      "horizon": 1,
      "simulations": 10000,

      "var": {
        "value": 128500,
        "percentage": 0.0257
      },
      "cvar": {
        "value": 172000,
        "percentage": 0.0344
      },
      "standardError": 2500
    },

    "simulationDistribution": {
      "mean": 5005000,
      "median": 5012000,
      "stdDev": 78000,
      "skewness": -0.35,
      "kurtosis": 3.8,
      "percentiles": {
        "p1": 4750000,
        "p5": 4875000,
        "p10": 4920000,
        "p25": 4965000,
        "p50": 5012000,
        "p75": 5055000,
        "p90": 5095000,
        "p95": 5125000,
        "p99": 5185000
      }
    },

    "calculatedAt": "2026-01-15T09:35:00+08:00"
  },
  "timestamp": "2026-01-15T09:35:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.3 取得波動度分析

取得投資組合及成分股的波動度分析。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/volatility
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| period | string | N | 1Y | 分析期間：1M/3M/6M/1Y/2Y/5Y |
| includeComponents | boolean | N | true | 是否包含成分股明細 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "asOfDate": "2026-01-15",
    "period": "1Y",

    "portfolioVolatility": {
      "daily": 0.018,
      "weekly": 0.042,
      "monthly": 0.085,
      "annualized": 0.285,
      "trend": "INCREASING"
    },

    "volatilityHistory": {
      "current": 0.285,
      "avg30d": 0.275,
      "avg90d": 0.262,
      "avg1y": 0.248,
      "percentileRank": 72
    },

    "downsideVolatility": {
      "annualized": 0.195,
      "sortinoRatio": 1.82
    },

    "componentVolatility": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "weight": 0.18,
        "volatility": 0.32,
        "volatilityContribution": 0.058,
        "diversificationBenefit": 0.012
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "weight": 0.12,
        "volatility": 0.28,
        "volatilityContribution": 0.034,
        "diversificationBenefit": 0.008
      }
    ],

    "volatilityRegimes": [
      {
        "regime": "LOW",
        "threshold": "<0.20",
        "frequency": 0.35,
        "avgReturn": 0.012
      },
      {
        "regime": "NORMAL",
        "threshold": "0.20-0.35",
        "frequency": 0.48,
        "avgReturn": 0.008
      },
      {
        "regime": "HIGH",
        "threshold": ">0.35",
        "frequency": 0.17,
        "avgReturn": -0.005
      }
    ],

    "impliedVolatility": {
      "available": true,
      "iv30": 0.30,
      "iv60": 0.28,
      "ivRank": 65,
      "ivPercentile": 58
    },

    "calculatedAt": "2026-01-15T09:30:00+08:00"
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.4 取得風險報告

產生完整的投資組合風險報告。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/risk-report
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| reportType | string | N | STANDARD | 報告類型：SUMMARY/STANDARD/DETAILED |
| asOfDate | date | N | today | 報告基準日期 |
| includeSections | array | N | ALL | 包含章節：OVERVIEW/VAR/VOLATILITY/LIMITS/STRESS/ATTRIBUTION |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "reportId": "RPT_RISK_20260115_001",
    "portfolioId": "PF_001",
    "portfolioName": "主力持股組合",
    "reportType": "STANDARD",
    "asOfDate": "2026-01-15",
    "generatedAt": "2026-01-15T09:45:00+08:00",

    "executiveSummary": {
      "riskLevel": "MEDIUM",
      "riskScore": 65,
      "riskTrend": "STABLE",
      "keyFindings": [
        "投資組合 VaR(95%) 為 125,000 元，佔總值 2.5%",
        "波動度處於過去一年 72 百分位",
        "台積電權重接近單一個股上限 (18% / 20%)",
        "電子類股集中度較高 (35%)"
      ],
      "recommendations": [
        "考慮減碼台積電至 15% 以下",
        "增加非電子類股配置以降低集中風險"
      ]
    },

    "portfolioOverview": {
      "totalValue": 5000000,
      "cashPosition": 250000,
      "equityPosition": 4750000,
      "positionCount": 12,
      "turnover30d": 0.15
    },

    "riskMetrics": {
      "var": {
        "var95Daily": 125000,
        "var99Daily": 185000,
        "cvar95": 165000
      },
      "volatility": {
        "daily": 0.018,
        "annualized": 0.285
      },
      "marketRisk": {
        "beta": 1.15,
        "correlation": 0.82
      },
      "drawdown": {
        "maxDrawdown": 0.12,
        "currentDrawdown": 0.03,
        "avgDrawdown": 0.045
      }
    },

    "limitCompliance": {
      "compliant": true,
      "warnings": 2,
      "breaches": 0,
      "details": [
        {
          "limitType": "VAR_95",
          "status": "WARNING",
          "limitValue": 150000,
          "currentValue": 125000,
          "utilization": 0.833
        }
      ]
    },

    "stressTestSummary": {
      "worstCaseScenario": "MARKET_CRASH_2008",
      "worstCaseLoss": 1250000,
      "worstCasePct": 0.25,
      "scenariosPassed": 8,
      "scenariosFailed": 2
    },

    "riskAttribution": {
      "byFactor": [
        {"factor": "MARKET", "contribution": 0.72},
        {"factor": "SECTOR", "contribution": 0.15},
        {"factor": "SPECIFIC", "contribution": 0.13}
      ],
      "bySector": [
        {"sector": "電子", "contribution": 0.55},
        {"sector": "金融", "contribution": 0.20},
        {"sector": "傳產", "contribution": 0.15},
        {"sector": "其他", "contribution": 0.10}
      ]
    },

    "trendAnalysis": {
      "varTrend": {
        "direction": "STABLE",
        "change7d": 0.02,
        "change30d": -0.05
      },
      "volatilityTrend": {
        "direction": "INCREASING",
        "change7d": 0.08,
        "change30d": 0.15
      }
    }
  },
  "timestamp": "2026-01-15T09:45:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.5 取得有效風險預警

取得目前有效的風險預警清單。

**端點**
```
GET /api/v1/risk/alerts/active
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| portfolioId | string | N | - | 篩選投資組合 |
| severity | string | N | - | 篩選嚴重等級：LOW/MEDIUM/HIGH/CRITICAL |
| type | string | N | - | 篩選預警類型 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "activeAlerts": [
      {
        "alertId": "ALT_20260115_001",
        "portfolioId": "PF_001",
        "portfolioName": "主力持股組合",
        "alertType": "VAR_THRESHOLD",
        "severity": "HIGH",
        "status": "ACTIVE",
        "title": "VaR 接近上限",
        "message": "投資組合 VaR(95%) 達到限額的 83%，請注意風險控管",
        "triggerValue": 125000,
        "thresholdValue": 150000,
        "triggeredAt": "2026-01-15T09:00:00+08:00",
        "acknowledgedAt": null,
        "acknowledgedBy": null
      },
      {
        "alertId": "ALT_20260115_002",
        "portfolioId": "PF_001",
        "portfolioName": "主力持股組合",
        "alertType": "CONCENTRATION",
        "severity": "MEDIUM",
        "status": "ACTIVE",
        "title": "單一個股權重警告",
        "message": "台積電 (2330) 權重達 18%，接近單一個股上限 20%",
        "triggerValue": 0.18,
        "thresholdValue": 0.20,
        "stockId": "2330",
        "stockName": "台積電",
        "triggeredAt": "2026-01-14T15:30:00+08:00",
        "acknowledgedAt": null,
        "acknowledgedBy": null
      },
      {
        "alertId": "ALT_20260114_003",
        "portfolioId": "PF_002",
        "portfolioName": "成長股組合",
        "alertType": "VOLATILITY_SPIKE",
        "severity": "MEDIUM",
        "status": "ACKNOWLEDGED",
        "title": "波動度上升",
        "message": "投資組合年化波動度上升至 32%，高於歷史 75 百分位",
        "triggerValue": 0.32,
        "thresholdValue": 0.30,
        "triggeredAt": "2026-01-14T10:15:00+08:00",
        "acknowledgedAt": "2026-01-14T14:00:00+08:00",
        "acknowledgedBy": "user@example.com"
      }
    ],
    "summary": {
      "totalActive": 3,
      "bySeverity": {
        "CRITICAL": 0,
        "HIGH": 1,
        "MEDIUM": 2,
        "LOW": 0
      },
      "byType": {
        "VAR_THRESHOLD": 1,
        "CONCENTRATION": 1,
        "VOLATILITY_SPIKE": 1
      }
    }
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 2.6 取得個股風險指標

取得單一股票的風險指標。

**端點**
```
GET /api/v1/risk/stocks/{stockId}/risk
```

**路徑參數**

| 參數 | 類型 | 說明 |
|-----|------|------|
| stockId | string | 股票代碼 |

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| period | string | N | 1Y | 分析期間：1M/3M/6M/1Y/2Y/5Y |
| benchmarkCode | string | N | TAIEX | 比較基準代碼 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stockId": "2330",
    "stockName": "台積電",
    "asOfDate": "2026-01-15",
    "period": "1Y",

    "riskProfile": {
      "riskLevel": "MEDIUM_HIGH",
      "riskScore": 68
    },

    "volatility": {
      "daily": 0.022,
      "annualized": 0.349,
      "rank": 156,
      "percentile": 78,
      "trend": "STABLE"
    },

    "beta": {
      "value": 1.25,
      "rSquared": 0.68,
      "interpretation": "較大盤波動 25%"
    },

    "var": {
      "var95Daily": 0.035,
      "var99Daily": 0.052,
      "cvar95Daily": 0.045
    },

    "drawdown": {
      "maxDrawdown": 0.28,
      "maxDrawdownStart": "2024-07-15",
      "maxDrawdownEnd": "2024-08-05",
      "avgDrawdown": 0.08,
      "currentDrawdown": 0.05
    },

    "tailRisk": {
      "skewness": -0.42,
      "kurtosis": 4.2,
      "leftTailRatio": 1.35
    },

    "correlation": {
      "withBenchmark": 0.82,
      "withSector": 0.75,
      "topCorrelated": [
        {"stockId": "2454", "stockName": "聯發科", "correlation": 0.72},
        {"stockId": "2303", "stockName": "聯電", "correlation": 0.68}
      ]
    },

    "historicalExtremes": {
      "bestDay": {"date": "2024-02-15", "return": 0.085},
      "worstDay": {"date": "2024-08-05", "return": -0.095},
      "avgPositiveDay": 0.015,
      "avgNegativeDay": -0.013
    },

    "calculatedAt": "2026-01-15T09:30:00+08:00"
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

## 3. P1 進階 API

### 3.1 建立風險限額

建立新的風險限額規則。

**端點**
```
POST /api/v1/risk/limits
```

**請求主體**
```json
{
  "portfolioId": "PF_001",
  "limitType": "VAR_95",
  "limitValue": 150000,
  "warningThreshold": 0.80,
  "criticalThreshold": 0.95,
  "action": "ALERT",
  "description": "每日 VaR(95%) 上限",
  "enabled": true
}
```

**請求參數說明**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| portfolioId | string | Y | 投資組合 ID |
| limitType | string | Y | 限額類型 (詳見限額類型表) |
| limitValue | number | Y | 限額值 |
| warningThreshold | number | N | 警告閾值 (0-1)，預設 0.80 |
| criticalThreshold | number | N | 嚴重閾值 (0-1)，預設 0.95 |
| action | string | N | 觸發動作：ALERT/BLOCK_TRADE，預設 ALERT |
| description | string | N | 限額說明 |
| enabled | boolean | N | 是否啟用，預設 true |

**限額類型**

| 類型代碼 | 說明 | 值域 |
|---------|------|------|
| VAR_95 | VaR(95%) 絕對金額上限 | 金額 |
| VAR_99 | VaR(99%) 絕對金額上限 | 金額 |
| VAR_PCT | VaR 百分比上限 | 0-1 |
| VOLATILITY | 年化波動度上限 | 0-1 |
| MAX_SINGLE_STOCK | 單一個股權重上限 | 0-1 |
| MAX_SECTOR | 單一產業權重上限 | 0-1 |
| MAX_DRAWDOWN | 最大回撤上限 | 0-1 |
| BETA | Beta 係數上限 | 數值 |

**成功回應 (201 Created)**
```json
{
  "code": 201,
  "message": "Risk limit created successfully",
  "data": {
    "limitId": "LMT_20260115_001",
    "portfolioId": "PF_001",
    "limitType": "VAR_95",
    "limitValue": 150000,
    "warningThreshold": 0.80,
    "criticalThreshold": 0.95,
    "action": "ALERT",
    "enabled": true,
    "createdAt": "2026-01-15T10:00:00+08:00"
  },
  "timestamp": "2026-01-15T10:00:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.2 取得風險限額清單

查詢風險限額設定。

**端點**
```
GET /api/v1/risk/limits
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| portfolioId | string | N | - | 篩選投資組合 |
| limitType | string | N | - | 篩選限額類型 |
| enabled | boolean | N | - | 篩選啟用狀態 |
| page | integer | N | 1 | 頁碼 |
| size | integer | N | 20 | 每頁筆數 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "limits": [
      {
        "limitId": "LMT_001",
        "portfolioId": "PF_001",
        "portfolioName": "主力持股組合",
        "limitType": "VAR_95",
        "limitValue": 150000,
        "currentValue": 125000,
        "utilization": 0.833,
        "status": "WARNING",
        "warningThreshold": 0.80,
        "criticalThreshold": 0.95,
        "action": "ALERT",
        "enabled": true,
        "lastCheckedAt": "2026-01-15T09:30:00+08:00"
      },
      {
        "limitId": "LMT_002",
        "portfolioId": "PF_001",
        "portfolioName": "主力持股組合",
        "limitType": "MAX_SINGLE_STOCK",
        "limitValue": 0.20,
        "currentValue": 0.18,
        "utilization": 0.90,
        "status": "WARNING",
        "warningThreshold": 0.80,
        "criticalThreshold": 0.95,
        "action": "ALERT",
        "enabled": true,
        "lastCheckedAt": "2026-01-15T09:30:00+08:00"
      }
    ],
    "summary": {
      "totalLimits": 8,
      "byStatus": {
        "NORMAL": 6,
        "WARNING": 2,
        "CRITICAL": 0,
        "BREACHED": 0
      }
    },
    "pagination": {
      "page": 1,
      "size": 20,
      "totalItems": 8,
      "totalPages": 1
    }
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.3 更新風險限額

更新現有風險限額設定。

**端點**
```
PUT /api/v1/risk/limits/{limitId}
```

**請求主體**
```json
{
  "limitValue": 180000,
  "warningThreshold": 0.75,
  "criticalThreshold": 0.90,
  "action": "ALERT",
  "description": "調高 VaR 上限",
  "enabled": true
}
```

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Risk limit updated successfully",
  "data": {
    "limitId": "LMT_001",
    "limitValue": 180000,
    "previousValue": 150000,
    "updatedAt": "2026-01-15T10:30:00+08:00"
  },
  "timestamp": "2026-01-15T10:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.4 刪除風險限額

刪除風險限額設定。

**端點**
```
DELETE /api/v1/risk/limits/{limitId}
```

**成功回應 (204 No Content)**
```
(無回應主體)
```

---

### 3.5 建立風險預警

建立自訂風險預警規則。

**端點**
```
POST /api/v1/risk/alerts
```

**請求主體**
```json
{
  "portfolioId": "PF_001",
  "alertType": "VOLATILITY_SPIKE",
  "condition": {
    "metric": "VOLATILITY_ANNUALIZED",
    "operator": "GREATER_THAN",
    "threshold": 0.35
  },
  "severity": "MEDIUM",
  "notificationChannels": ["EMAIL", "PUSH"],
  "cooldownMinutes": 60,
  "enabled": true
}
```

**請求參數說明**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| portfolioId | string | Y | 投資組合 ID |
| alertType | string | Y | 預警類型 |
| condition | object | Y | 觸發條件 |
| condition.metric | string | Y | 監控指標 |
| condition.operator | string | Y | 比較運算子：GREATER_THAN/LESS_THAN/EQUALS |
| condition.threshold | number | Y | 閾值 |
| severity | string | N | 嚴重等級：LOW/MEDIUM/HIGH/CRITICAL |
| notificationChannels | array | N | 通知管道 |
| cooldownMinutes | integer | N | 冷卻時間（分鐘），預設 60 |
| enabled | boolean | N | 是否啟用，預設 true |

**成功回應 (201 Created)**
```json
{
  "code": 201,
  "message": "Risk alert created successfully",
  "data": {
    "alertRuleId": "ALR_20260115_001",
    "portfolioId": "PF_001",
    "alertType": "VOLATILITY_SPIKE",
    "condition": {
      "metric": "VOLATILITY_ANNUALIZED",
      "operator": "GREATER_THAN",
      "threshold": 0.35
    },
    "severity": "MEDIUM",
    "enabled": true,
    "createdAt": "2026-01-15T10:00:00+08:00"
  },
  "timestamp": "2026-01-15T10:00:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.6 取得預警清單

查詢預警規則和歷史記錄。

**端點**
```
GET /api/v1/risk/alerts
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| portfolioId | string | N | - | 篩選投資組合 |
| status | string | N | - | 篩選狀態：ACTIVE/ACKNOWLEDGED/RESOLVED |
| severity | string | N | - | 篩選嚴重等級 |
| startDate | date | N | - | 起始日期 |
| endDate | date | N | - | 結束日期 |
| page | integer | N | 1 | 頁碼 |
| size | integer | N | 20 | 每頁筆數 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "alerts": [
      {
        "alertId": "ALT_20260115_001",
        "alertRuleId": "ALR_001",
        "portfolioId": "PF_001",
        "portfolioName": "主力持股組合",
        "alertType": "VAR_THRESHOLD",
        "severity": "HIGH",
        "status": "ACTIVE",
        "title": "VaR 接近上限",
        "message": "投資組合 VaR(95%) 達到限額的 83%",
        "triggerValue": 125000,
        "thresholdValue": 150000,
        "triggeredAt": "2026-01-15T09:00:00+08:00"
      }
    ],
    "pagination": {
      "page": 1,
      "size": 20,
      "totalItems": 15,
      "totalPages": 1
    }
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.7 確認預警

確認已閱讀風險預警。

**端點**
```
POST /api/v1/risk/alerts/{alertId}/acknowledge
```

**請求主體**
```json
{
  "note": "已知悉，將於下午調整持倉"
}
```

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Alert acknowledged",
  "data": {
    "alertId": "ALT_20260115_001",
    "status": "ACKNOWLEDGED",
    "acknowledgedAt": "2026-01-15T10:30:00+08:00",
    "acknowledgedBy": "user@example.com"
  },
  "timestamp": "2026-01-15T10:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.8 取得相關性矩陣

取得投資組合成分股的相關性分析。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/correlation
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| period | string | N | 1Y | 分析期間：1M/3M/6M/1Y/2Y |
| method | string | N | PEARSON | 相關係數方法：PEARSON/SPEARMAN |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "asOfDate": "2026-01-15",
    "period": "1Y",
    "method": "PEARSON",

    "stocks": ["2330", "2317", "2454", "2303", "2882"],
    "stockNames": {
      "2330": "台積電",
      "2317": "鴻海",
      "2454": "聯發科",
      "2303": "聯電",
      "2882": "國泰金"
    },

    "correlationMatrix": [
      [1.00, 0.65, 0.72, 0.68, 0.35],
      [0.65, 1.00, 0.58, 0.55, 0.42],
      [0.72, 0.58, 1.00, 0.75, 0.28],
      [0.68, 0.55, 0.75, 1.00, 0.25],
      [0.35, 0.42, 0.28, 0.25, 1.00]
    ],

    "statistics": {
      "avgCorrelation": 0.52,
      "maxCorrelation": {
        "pair": ["2454", "2303"],
        "value": 0.75
      },
      "minCorrelation": {
        "pair": ["2303", "2882"],
        "value": 0.25
      },
      "diversificationRatio": 1.35
    },

    "correlationClusters": [
      {
        "clusterId": 1,
        "name": "半導體群",
        "stocks": ["2330", "2454", "2303"],
        "avgIntraCorrelation": 0.72
      },
      {
        "clusterId": 2,
        "name": "金融群",
        "stocks": ["2882"],
        "avgIntraCorrelation": 1.00
      }
    ],

    "calculatedAt": "2026-01-15T09:30:00+08:00"
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.9 取得集中度分析

取得投資組合的集中度風險分析。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/concentration
```

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "asOfDate": "2026-01-15",

    "overallConcentration": {
      "hhi": 0.085,
      "normalizedHhi": 0.075,
      "effectiveN": 11.8,
      "concentrationLevel": "MODERATE"
    },

    "sectorConcentration": {
      "hhi": 0.285,
      "distribution": [
        {"sector": "電子", "weight": 0.45, "count": 5},
        {"sector": "金融", "weight": 0.20, "count": 3},
        {"sector": "傳產", "weight": 0.15, "count": 2},
        {"sector": "生技", "weight": 0.10, "count": 1},
        {"sector": "其他", "weight": 0.10, "count": 1}
      ],
      "topSector": {"sector": "電子", "weight": 0.45}
    },

    "stockConcentration": {
      "top5Weight": 0.58,
      "top10Weight": 0.82,
      "largestPosition": {
        "stockId": "2330",
        "stockName": "台積電",
        "weight": 0.18
      },
      "distribution": [
        {"stockId": "2330", "stockName": "台積電", "weight": 0.18},
        {"stockId": "2317", "stockName": "鴻海", "weight": 0.12},
        {"stockId": "2454", "stockName": "聯發科", "weight": 0.10},
        {"stockId": "2882", "stockName": "國泰金", "weight": 0.09},
        {"stockId": "2303", "stockName": "聯電", "weight": 0.09}
      ]
    },

    "marketCapConcentration": {
      "largeCap": {"weight": 0.65, "count": 4},
      "midCap": {"weight": 0.25, "count": 5},
      "smallCap": {"weight": 0.10, "count": 3}
    },

    "riskConcentration": {
      "top5RiskContribution": 0.72,
      "dominantRiskFactor": "SEMICONDUCTOR_SECTOR"
    },

    "recommendations": [
      {
        "type": "REDUCE",
        "stockId": "2330",
        "reason": "單一持股接近上限",
        "suggestedWeight": 0.15
      },
      {
        "type": "DIVERSIFY",
        "sector": "電子",
        "reason": "電子類股過度集中",
        "suggestedWeight": 0.35
      }
    ],

    "calculatedAt": "2026-01-15T09:30:00+08:00"
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.10 執行壓力測試

對投資組合執行壓力測試情境分析。

**端點**
```
POST /api/v1/risk/stress-test
```

**請求主體**
```json
{
  "portfolioId": "PF_001",
  "scenarios": ["MARKET_CRASH_2008", "COVID_2020", "RATE_HIKE"],
  "customScenarios": [
    {
      "name": "電子股大跌",
      "shocks": [
        {"factor": "SECTOR_ELECTRONIC", "change": -0.20}
      ]
    }
  ],
  "includeHistoricalWorst": true
}
```

**請求參數說明**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|:----:|------|
| portfolioId | string | Y | 投資組合 ID |
| scenarios | array | N | 預設情境清單 |
| customScenarios | array | N | 自訂情境 |
| customScenarios[].name | string | Y | 情境名稱 |
| customScenarios[].shocks | array | Y | 衝擊設定 |
| includeHistoricalWorst | boolean | N | 是否包含歷史最差情境 |

**預設情境**

| 情境代碼 | 說明 |
|---------|------|
| MARKET_CRASH_2008 | 2008 金融海嘯 |
| COVID_2020 | 2020 新冠疫情 |
| RATE_HIKE | 利率急升 |
| CURRENCY_CRISIS | 匯率危機 |
| TECH_CRASH | 科技股崩盤 |
| BLACK_SWAN | 黑天鵝事件 |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "totalValue": 5000000,
    "testedAt": "2026-01-15T10:00:00+08:00",

    "scenarioResults": [
      {
        "scenario": "MARKET_CRASH_2008",
        "description": "2008 金融海嘯情境",
        "marketChange": -0.45,
        "portfolioChange": -0.38,
        "estimatedLoss": 1900000,
        "finalValue": 3100000,
        "worstPositions": [
          {"stockId": "2330", "change": -0.42, "loss": 378000},
          {"stockId": "2882", "change": -0.52, "loss": 234000}
        ],
        "passed": false,
        "breachedLimits": ["MAX_DRAWDOWN"]
      },
      {
        "scenario": "COVID_2020",
        "description": "2020 新冠疫情情境",
        "marketChange": -0.30,
        "portfolioChange": -0.25,
        "estimatedLoss": 1250000,
        "finalValue": 3750000,
        "worstPositions": [
          {"stockId": "2317", "change": -0.35, "loss": 210000}
        ],
        "passed": true,
        "breachedLimits": []
      },
      {
        "scenario": "電子股大跌",
        "description": "自訂情境：電子類股下跌20%",
        "marketChange": null,
        "portfolioChange": -0.12,
        "estimatedLoss": 600000,
        "finalValue": 4400000,
        "worstPositions": [
          {"stockId": "2330", "change": -0.20, "loss": 180000},
          {"stockId": "2454", "change": -0.20, "loss": 100000}
        ],
        "passed": true,
        "breachedLimits": []
      }
    ],

    "historicalWorst": {
      "date": "2008-10-13",
      "marketChange": -0.067,
      "portfolioChange": -0.072,
      "estimatedLoss": 360000
    },

    "summary": {
      "scenariosTested": 3,
      "scenariosPassed": 2,
      "scenariosFailed": 1,
      "worstCaseScenario": "MARKET_CRASH_2008",
      "worstCaseLoss": 1900000,
      "avgLoss": 1250000
    }
  },
  "timestamp": "2026-01-15T10:00:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.11 取得 Beta 分析

取得投資組合相對於基準的 Beta 分析。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/beta
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| benchmarkCode | string | N | TAIEX | 基準代碼 |
| period | string | N | 1Y | 分析期間：1M/3M/6M/1Y/2Y |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "asOfDate": "2026-01-15",
    "benchmarkCode": "TAIEX",
    "period": "1Y",

    "portfolioBeta": {
      "value": 1.15,
      "standardError": 0.08,
      "tStatistic": 14.4,
      "pValue": 0.000,
      "interpretation": "投資組合波動較大盤高 15%"
    },

    "alpha": {
      "value": 0.0023,
      "annualized": 0.028,
      "standardError": 0.0012,
      "significant": true
    },

    "rSquared": {
      "value": 0.82,
      "adjustedRSquared": 0.81,
      "interpretation": "82% 的投資組合變異可由大盤解釋"
    },

    "componentBeta": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "weight": 0.18,
        "beta": 1.25,
        "weightedBeta": 0.225,
        "contribution": 0.196
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "weight": 0.12,
        "beta": 1.10,
        "weightedBeta": 0.132,
        "contribution": 0.115
      },
      {
        "stockId": "2882",
        "stockName": "國泰金",
        "weight": 0.09,
        "beta": 0.95,
        "weightedBeta": 0.086,
        "contribution": 0.074
      }
    ],

    "betaHistory": [
      {"date": "2025-01-15", "beta": 1.08},
      {"date": "2025-04-15", "beta": 1.12},
      {"date": "2025-07-15", "beta": 1.10},
      {"date": "2025-10-15", "beta": 1.18},
      {"date": "2026-01-15", "beta": 1.15}
    ],

    "betaStability": {
      "mean": 1.13,
      "stdDev": 0.04,
      "min": 1.05,
      "max": 1.22,
      "trend": "SLIGHTLY_INCREASING"
    },

    "calculatedAt": "2026-01-15T09:30:00+08:00"
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 3.12 取得風險歸因

取得投資組合的風險來源分解。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/attribution
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| method | string | N | FACTOR | 歸因方法：FACTOR/MARGINAL/INCREMENTAL |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "asOfDate": "2026-01-15",
    "totalRisk": 0.285,

    "factorAttribution": {
      "totalFactorRisk": 0.248,
      "specificRisk": 0.037,
      "factorRiskPct": 0.87,

      "factors": [
        {
          "factor": "MARKET",
          "exposure": 1.15,
          "factorVolatility": 0.18,
          "riskContribution": 0.207,
          "contributionPct": 0.725
        },
        {
          "factor": "SECTOR_ELECTRONIC",
          "exposure": 0.45,
          "factorVolatility": 0.12,
          "riskContribution": 0.054,
          "contributionPct": 0.189
        },
        {
          "factor": "SIZE",
          "exposure": 0.30,
          "factorVolatility": 0.08,
          "riskContribution": 0.024,
          "contributionPct": 0.084
        }
      ]
    },

    "stockAttribution": [
      {
        "stockId": "2330",
        "stockName": "台積電",
        "weight": 0.18,
        "totalRiskContribution": 0.091,
        "contributionPct": 0.32,
        "systematicRisk": 0.072,
        "specificRisk": 0.019
      },
      {
        "stockId": "2317",
        "stockName": "鴻海",
        "weight": 0.12,
        "totalRiskContribution": 0.043,
        "contributionPct": 0.15,
        "systematicRisk": 0.035,
        "specificRisk": 0.008
      }
    ],

    "marginalRisk": [
      {
        "stockId": "2330",
        "marginalVar": 0.044,
        "interpretation": "增加 1% 台積電權重，VaR 增加 0.044%"
      },
      {
        "stockId": "2882",
        "marginalVar": 0.018,
        "interpretation": "增加 1% 國泰金權重，VaR 增加 0.018%"
      }
    ],

    "diversificationEffect": {
      "undiversifiedRisk": 0.342,
      "diversifiedRisk": 0.285,
      "diversificationBenefit": 0.057,
      "diversificationRatio": 1.20
    },

    "calculatedAt": "2026-01-15T09:30:00+08:00"
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

## 4. P2 次要 API

### 4.1 歷史風險查詢

查詢投資組合的歷史風險指標。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/risk/history
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| startDate | date | N | -90d | 起始日期 |
| endDate | date | N | today | 結束日期 |
| metrics | array | N | ALL | 指標清單：VAR/VOLATILITY/BETA/DRAWDOWN |
| interval | string | N | DAILY | 資料間隔：DAILY/WEEKLY/MONTHLY |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "portfolioId": "PF_001",
    "period": {
      "startDate": "2025-10-15",
      "endDate": "2026-01-15"
    },
    "interval": "DAILY",

    "history": [
      {
        "date": "2025-10-15",
        "var95": 118000,
        "var99": 175000,
        "volatility": 0.265,
        "beta": 1.12,
        "drawdown": 0.02
      },
      {
        "date": "2025-10-16",
        "var95": 120000,
        "var99": 178000,
        "volatility": 0.268,
        "beta": 1.13,
        "drawdown": 0.025
      }
    ],

    "statistics": {
      "var95": {
        "mean": 122000,
        "min": 98000,
        "max": 145000,
        "stdDev": 12000
      },
      "volatility": {
        "mean": 0.272,
        "min": 0.245,
        "max": 0.302,
        "trend": "INCREASING"
      }
    },

    "dataPoints": 92
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 4.2 匯出風險報告

匯出風險報告為檔案格式。

**端點**
```
GET /api/v1/risk/portfolios/{portfolioId}/risk/export
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| format | string | N | PDF | 匯出格式：PDF/EXCEL/CSV |
| reportType | string | N | STANDARD | 報告類型 |
| asOfDate | date | N | today | 報告基準日期 |

**成功回應 (200 OK)**

回傳檔案下載，Content-Type 依 format 而定。

---

### 4.3 建立自訂情境

建立自訂壓力測試情境。

**端點**
```
POST /api/v1/risk/scenarios
```

**請求主體**
```json
{
  "name": "台海危機情境",
  "description": "模擬台海衝突對投資組合的影響",
  "shocks": [
    {"factor": "MARKET", "change": -0.25},
    {"factor": "SECTOR_ELECTRONIC", "change": -0.35},
    {"factor": "CURRENCY_TWD", "change": -0.10}
  ],
  "isPublic": false
}
```

**成功回應 (201 Created)**
```json
{
  "code": 201,
  "message": "Scenario created successfully",
  "data": {
    "scenarioId": "SCN_20260115_001",
    "name": "台海危機情境",
    "shocksCount": 3,
    "createdAt": "2026-01-15T10:30:00+08:00"
  },
  "timestamp": "2026-01-15T10:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

### 4.4 取得情境清單

查詢可用的壓力測試情境。

**端點**
```
GET /api/v1/risk/scenarios
```

**查詢參數**

| 參數 | 類型 | 必填 | 預設值 | 說明 |
|-----|------|:----:|-------|------|
| type | string | N | ALL | 情境類型：SYSTEM/CUSTOM/ALL |

**成功回應 (200 OK)**
```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "scenarios": [
      {
        "scenarioId": "MARKET_CRASH_2008",
        "name": "2008 金融海嘯",
        "type": "SYSTEM",
        "description": "模擬 2008 年金融危機的市場衝擊",
        "shocks": [
          {"factor": "MARKET", "change": -0.45},
          {"factor": "SECTOR_FINANCIAL", "change": -0.55}
        ]
      },
      {
        "scenarioId": "SCN_20260115_001",
        "name": "台海危機情境",
        "type": "CUSTOM",
        "description": "模擬台海衝突對投資組合的影響",
        "shocks": [
          {"factor": "MARKET", "change": -0.25},
          {"factor": "SECTOR_ELECTRONIC", "change": -0.35}
        ],
        "createdAt": "2026-01-15T10:30:00+08:00"
      }
    ]
  },
  "timestamp": "2026-01-15T09:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

## 5. 共用錯誤回應

| 錯誤碼 | HTTP Status | 說明 |
|-------|-------------|------|
| M17-001 | 404 | 投資組合不存在 |
| M17-002 | 400 | 風險計算參數無效 |
| M17-003 | 400 | 歷史資料不足 |
| M17-004 | 404 | 股票不存在 |
| M17-005 | 404 | 風險限額不存在 |
| M17-006 | 400 | 限額類型無效 |
| M17-007 | 409 | 限額已存在 |
| M17-008 | 404 | 預警不存在 |
| M17-009 | 400 | 情境參數無效 |
| M17-010 | 500 | VaR 計算失敗 |
| M17-011 | 500 | 壓力測試執行失敗 |

**錯誤回應格式**
```json
{
  "code": 400,
  "message": "Invalid request",
  "error": {
    "errorCode": "M17-003",
    "errorMessage": "歷史資料不足",
    "details": {
      "required": 252,
      "available": 180,
      "reason": "VaR 計算需要至少 252 天歷史資料"
    }
  },
  "timestamp": "2026-01-15T10:30:00+08:00",
  "traceId": "req_abc123"
}
```

---

## 6. 相關文檔

- [M17 功能需求](../functional/M17-風險管理功能需求.md)
- [M17 資料庫設計](../../design/M17-資料庫設計.md)
- [M18 投資組合管理 API](./M18-API規格.md)
- [M06 資料管理 API](./M06-API規格.md)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-15
**下次審核**: 2026-04-15
