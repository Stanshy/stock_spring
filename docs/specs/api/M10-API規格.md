# M10-技術型態辨識模組 API 規格

> **文件編號**: API-M10
> **模組名稱**: 技術型態辨識模組
> **版本**: v1.0
> **最後更新**: 2026-01-12
> **狀態**: Draft

---

## 📋 API 總覽

### 基礎資訊

| 項目 | 值 |
|-----|-----|
| Base URL | `/api/v1/pattern` |
| 認證方式 | JWT Bearer Token |
| 內容類型 | application/json |
| 字元編碼 | UTF-8 |

### API 清單

| 端點 | 方法 | 說明 | 優先級 |
|-----|------|------|-------|
| `/api/v1/pattern/{stockId}/kline` | GET | 查詢 K 線型態 | P0 |
| `/api/v1/pattern/{stockId}/chart` | GET | 查詢圖表型態 | P0 |
| `/api/v1/pattern/{stockId}/trend` | GET | 查詢趨勢分析 | P0 |
| `/api/v1/pattern/{stockId}/signals` | GET | 查詢型態訊號 | P0 |
| `/api/v1/pattern/{stockId}/analysis` | GET | 查詢完整型態分析 | P0 |
| `/api/v1/pattern/{stockId}/analysis` | POST | 執行即時型態偵測 | P1 |
| `/api/v1/pattern/{stockId}/support-resistance` | GET | 查詢支撐壓力位 | P1 |
| `/api/v1/pattern/scan/kline` | GET | 全市場 K 線型態掃描 | P1 |
| `/api/v1/pattern/scan/chart` | GET | 全市場圖表型態掃描 | P1 |
| `/api/v1/pattern/{stockId}/history` | GET | 查詢歷史型態紀錄 | P1 |
| `/api/v1/pattern/{stockId}/statistics` | GET | 查詢型態統計分析 | P2 |
| `/api/v1/pattern/metadata/patterns` | GET | 查詢支援的型態清單 | P2 |

---

## 1. K 線型態查詢

### GET `/api/v1/pattern/{stockId}/kline`

查詢指定股票的 K 線型態識別結果。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼（如 2330） |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| start_date | string | N | 60天前 | 開始日期（YYYY-MM-DD） |
| end_date | string | N | 今日 | 結束日期（YYYY-MM-DD） |
| pattern_types | string | N | all | 指定型態（逗號分隔，如 `KLINE001,KLINE020`） |
| min_strength | integer | N | 50 | 最低型態強度（0-100） |
| signal_filter | string | N | all | 訊號過濾（BULLISH, BEARISH, NEUTRAL） |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "query_period": {
      "start_date": "2024-10-24",
      "end_date": "2024-12-24"
    },
    "patterns_found": 8,
    "latest_patterns": [
      {
        "pattern_id": "KLINE020",
        "pattern_name": "看漲吞噬",
        "english_name": "Bullish Engulfing",
        "category": "DOUBLE_KLINE",
        "detection_date": "2024-12-23",
        "signal_type": "BULLISH_REVERSAL",
        "strength": 85,
        "involved_dates": ["2024-12-22", "2024-12-23"],
        "price_data": {
          "pattern_low": 565,
          "pattern_high": 582,
          "first_candle": {
            "open": 578,
            "high": 580,
            "low": 565,
            "close": 568
          },
          "second_candle": {
            "open": 566,
            "high": 582,
            "low": 565,
            "close": 580
          }
        },
        "volume_confirmation": true,
        "trend_context": "DOWNTREND",
        "description": "陽線實體完全包覆前一根陰線，出現在20日低點附近，成交量放大確認"
      },
      {
        "pattern_id": "KLINE005",
        "pattern_name": "十字星",
        "english_name": "Doji",
        "category": "SINGLE_KLINE",
        "detection_date": "2024-12-20",
        "signal_type": "NEUTRAL_REVERSAL",
        "strength": 65,
        "involved_dates": ["2024-12-20"],
        "price_data": {
          "open": 570,
          "high": 575,
          "low": 565,
          "close": 570.5
        },
        "trend_context": "DOWNTREND",
        "description": "開盤與收盤幾乎相同，顯示多空力道均衡"
      }
    ],
    "pattern_summary": {
      "bullish_count": 5,
      "bearish_count": 2,
      "neutral_count": 1,
      "avg_strength": 72.5
    },
    "diagnostics": {
      "calculation_time_ms": 28,
      "trading_days_scanned": 42,
      "patterns_checked": 30
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_001"
}
```

#### 錯誤回應

| HTTP Status | 錯誤碼 | 說明 |
|-------------|-------|------|
| 404 | M10_PTN_001 | 股票代碼不存在 |
| 400 | M10_PTN_002 | 價格資料不足 |
| 400 | M10_PARAM_002 | 日期範圍無效 |

---

## 2. 圖表型態查詢

### GET `/api/v1/pattern/{stockId}/chart`

查詢指定股票的圖表型態識別結果。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| lookback_days | integer | N | 120 | 回溯天數 |
| pattern_types | string | N | all | 指定型態 |
| min_strength | integer | N | 50 | 最低型態強度 |
| status | string | N | all | 型態狀態（FORMING, CONFIRMED, COMPLETED） |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "analysis_date": "2024-12-24",
    "current_price": 580,
    "patterns_found": 2,
    "chart_patterns": [
      {
        "pattern_id": "CHART003",
        "pattern_name": "雙重頂",
        "english_name": "Double Top",
        "category": "REVERSAL",
        "signal_type": "BEARISH_REVERSAL",
        "status": "FORMING",
        "strength": 72,
        "formation_period": {
          "start_date": "2024-11-15",
          "end_date": "2024-12-20",
          "duration_days": 25
        },
        "key_levels": {
          "first_peak": {
            "date": "2024-11-25",
            "price": 598
          },
          "second_peak": {
            "date": "2024-12-15",
            "price": 595
          },
          "neckline": 565,
          "pattern_height": 33
        },
        "targets": {
          "target_price": 532,
          "potential_move_percent": -8.28,
          "stop_loss": 602
        },
        "completion_criteria": "價格跌破頸線 565 元即確認型態",
        "volume_pattern": "兩次高點成交量遞減，符合雙頂特徵",
        "reliability_factors": {
          "time_symmetry": 85,
          "price_symmetry": 90,
          "volume_confirmation": 75
        },
        "description": "雙重頂型態形成中，兩次高點分別為 598 和 595，頸線位於 565 元"
      },
      {
        "pattern_id": "CHART029",
        "pattern_name": "上升通道",
        "english_name": "Ascending Channel",
        "category": "CONTINUATION",
        "signal_type": "BULLISH_CONTINUATION",
        "status": "CONFIRMED",
        "strength": 68,
        "formation_period": {
          "start_date": "2024-10-01",
          "end_date": "2024-12-24",
          "duration_days": 60
        },
        "key_levels": {
          "upper_trendline": 592,
          "lower_trendline": 568,
          "channel_width": 24,
          "slope_degree": 15
        },
        "current_position": {
          "distance_to_upper": 12,
          "distance_to_lower": 12,
          "position_percent": 50
        },
        "description": "股價在上升通道內運行，通道上緣 592，下緣 568"
      }
    ],
    "forming_patterns": [
      {
        "pattern_id": "CHART022",
        "pattern_name": "對稱三角形",
        "probability": 45,
        "expected_completion": "2024-12-30"
      }
    ],
    "diagnostics": {
      "calculation_time_ms": 85,
      "peaks_identified": 8,
      "troughs_identified": 7
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_002"
}
```

---

## 3. 趨勢分析查詢

### GET `/api/v1/pattern/{stockId}/trend`

查詢指定股票的趨勢型態分析。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| timeframe | string | N | daily | 時間週期（daily, weekly, monthly） |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "analysis_date": "2024-12-24",
    "current_price": 580,
    "trend_analysis": {
      "primary_trend": {
        "trend_id": "TREND001",
        "trend_name": "上升趨勢",
        "strength": 72,
        "duration_days": 45,
        "start_date": "2024-11-01",
        "start_price": 520,
        "trend_gain_percent": 11.54
      },
      "secondary_trend": {
        "trend_id": "TREND003",
        "trend_name": "盤整",
        "strength": 55,
        "duration_days": 10,
        "description": "短期在 565-590 區間盤整"
      },
      "trend_quality": {
        "consistency": 78,
        "volatility": 15.2,
        "momentum": "POSITIVE"
      }
    },
    "ma_analysis": {
      "ma5": 578,
      "ma10": 572,
      "ma20": 565,
      "ma60": 545,
      "ma120": 530,
      "alignment": "BULLISH",
      "alignment_strength": 85,
      "golden_cross": null,
      "death_cross": null,
      "nearest_ma_support": {
        "ma_period": 20,
        "price": 565,
        "distance_percent": 2.59
      }
    },
    "trend_indicators": {
      "adx": {
        "value": 32,
        "interpretation": "有明確趨勢",
        "trend_strength": "MEDIUM_STRONG"
      },
      "di_plus": 28,
      "di_minus": 18,
      "di_interpretation": "多方佔優"
    },
    "structure_analysis": {
      "higher_highs": 4,
      "higher_lows": 4,
      "lower_highs": 0,
      "lower_lows": 0,
      "structure": "BULLISH"
    },
    "key_levels": {
      "support_levels": [
        {"price": 565, "strength": 85, "type": "MA20"},
        {"price": 550, "strength": 72, "type": "PREVIOUS_LOW"}
      ],
      "resistance_levels": [
        {"price": 590, "strength": 80, "type": "RECENT_HIGH"},
        {"price": 600, "strength": 90, "type": "PSYCHOLOGICAL"}
      ]
    },
    "trend_forecast": {
      "short_term": "BULLISH",
      "medium_term": "BULLISH",
      "confidence": 72,
      "key_watch_levels": {
        "bullish_confirmation": 590,
        "bearish_warning": 565
      }
    },
    "warnings": [
      "短期接近壓力區 590-600",
      "成交量較前日萎縮"
    ]
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_003"
}
```

---

## 4. 型態訊號查詢

### GET `/api/v1/pattern/{stockId}/signals`

查詢指定股票的型態相關交易訊號。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| start_date | string | N | 30天前 | 開始日期 |
| end_date | string | N | 今日 | 結束日期 |
| signal_type | string | N | all | 訊號類型（BUY, SELL, WATCH） |
| source | string | N | all | 訊號來源（KLINE, CHART, TREND） |
| min_confidence | integer | N | 50 | 最低信心度 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "current_price": 580,
    "total_signals": 5,
    "signals": [
      {
        "signal_id": "PTN_SIG_001",
        "signal_name": "看漲K線型態",
        "signal_type": "BUY",
        "source": "KLINE",
        "pattern_id": "KLINE020",
        "pattern_name": "看漲吞噬",
        "trigger_date": "2024-12-23",
        "trigger_price": 580,
        "confidence": 78,
        "strength": "HIGH",
        "targets": {
          "target_price": 600,
          "target_gain_percent": 3.45,
          "stop_loss": 565,
          "stop_loss_percent": -2.59,
          "risk_reward_ratio": 1.33
        },
        "supporting_factors": [
          "出現在20日低點",
          "成交量放大確認",
          "外資同步買超"
        ],
        "description": "看漲吞噬型態形成，配合成交量放大，建議逢低布局"
      },
      {
        "signal_id": "PTN_SIG_011",
        "signal_name": "支撐確認",
        "signal_type": "BUY",
        "source": "TREND",
        "trigger_date": "2024-12-20",
        "trigger_price": 566,
        "confidence": 72,
        "strength": "MEDIUM",
        "targets": {
          "target_price": 590,
          "stop_loss": 555
        },
        "description": "價格在 MA20 支撐獲得支撐，反彈訊號"
      },
      {
        "signal_id": "PTN_SIG_005",
        "signal_name": "雙重頂警示",
        "signal_type": "WATCH",
        "source": "CHART",
        "pattern_id": "CHART003",
        "pattern_name": "雙重頂",
        "trigger_date": "2024-12-15",
        "trigger_price": 595,
        "confidence": 65,
        "strength": "MEDIUM",
        "warning_level": "頸線 565，跌破則確認",
        "description": "潛在雙重頂型態形成中，需留意頸線支撐"
      }
    ],
    "signal_summary": {
      "buy_signals": 2,
      "sell_signals": 0,
      "watch_signals": 3,
      "avg_confidence": 68
    },
    "overall_bias": {
      "direction": "BULLISH",
      "strength": 65,
      "interpretation": "短期偏多，但需留意壓力區"
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_004"
}
```

---

## 5. 完整型態分析查詢

### GET `/api/v1/pattern/{stockId}/analysis`

一次取得指定股票的完整型態分析結果。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| include_history | boolean | N | false | 是否包含歷史型態 |
| lookback_days | integer | N | 120 | 分析回溯天數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "analysis_date": "2024-12-24",
    "current_price": 580,
    "kline_analysis": {
      "latest_patterns": [
        {
          "pattern_id": "KLINE020",
          "pattern_name": "看漲吞噬",
          "detection_date": "2024-12-23",
          "signal_type": "BULLISH_REVERSAL",
          "strength": 85
        }
      ],
      "recent_pattern_count": {
        "bullish": 5,
        "bearish": 2,
        "neutral": 1
      }
    },
    "chart_analysis": {
      "active_patterns": [
        {
          "pattern_id": "CHART003",
          "pattern_name": "雙重頂",
          "status": "FORMING",
          "strength": 72,
          "neckline": 565
        },
        {
          "pattern_id": "CHART029",
          "pattern_name": "上升通道",
          "status": "CONFIRMED",
          "strength": 68
        }
      ]
    },
    "trend_analysis": {
      "current_trend": "TREND001",
      "trend_name": "上升趨勢",
      "trend_strength": 72,
      "ma_alignment": "BULLISH"
    },
    "support_resistance": {
      "nearest_support": 565,
      "nearest_resistance": 590,
      "key_support_levels": [565, 550, 530],
      "key_resistance_levels": [590, 600, 620]
    },
    "signals": [
      {
        "signal_id": "PTN_SIG_001",
        "signal_name": "看漲K線型態",
        "signal_type": "BUY",
        "confidence": 78
      }
    ],
    "overall_assessment": {
      "pattern_score": 72,
      "pattern_grade": "B+",
      "bias": "BULLISH",
      "confidence": 70,
      "summary": "K線型態偏多，圖表型態需留意雙頂風險，整體趨勢仍為上升",
      "key_observations": [
        "近期出現看漲吞噬型態",
        "上升趨勢維持，但需留意壓力區",
        "MA20 提供有效支撐"
      ],
      "recommendations": [
        "短線可逢低布局",
        "跌破 565 應減碼",
        "突破 590 可加碼"
      ]
    },
    "diagnostics": {
      "calculation_time_ms": 150,
      "data_completeness": 100,
      "last_update": "2024-12-24T15:00:00+08:00"
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_005"
}
```

---

## 6. 即時型態偵測

### POST `/api/v1/pattern/{stockId}/analysis`

執行即時型態偵測（強制重新計算，不使用快取）。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 請求主體

```json
{
  "detection_plan": {
    "include_kline_patterns": true,
    "include_chart_patterns": true,
    "include_trend_patterns": true,
    "include_support_resistance": true,
    "include_signals": true
  },
  "kline_options": {
    "pattern_types": ["KLINE001", "KLINE020", "KLINE040"],
    "min_strength": 60
  },
  "chart_options": {
    "pattern_types": ["CHART001", "CHART003", "CHART020"],
    "min_strength": 50
  },
  "lookback_period": 120,
  "force_recalculate": true
}
```

#### 成功回應 (200)

回應格式同 `GET /api/v1/pattern/{stockId}/analysis`。

---

## 7. 支撐壓力位查詢

### GET `/api/v1/pattern/{stockId}/support-resistance`

查詢指定股票的支撐與壓力位。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| lookback_days | integer | N | 120 | 回溯天數 |
| max_levels | integer | N | 5 | 每類型最多回傳筆數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "analysis_date": "2024-12-24",
    "current_price": 580,
    "support_levels": [
      {
        "price": 565,
        "strength": 85,
        "type": "WAVE_TROUGH",
        "source": "近期低點 2024-12-10",
        "test_count": 2,
        "last_test_date": "2024-12-20",
        "distance_percent": -2.59
      },
      {
        "price": 560,
        "strength": 78,
        "type": "MOVING_AVERAGE",
        "source": "MA20",
        "test_count": 1,
        "distance_percent": -3.45
      },
      {
        "price": 550,
        "strength": 72,
        "type": "VOLUME_PROFILE",
        "source": "成交密集區",
        "test_count": 0,
        "distance_percent": -5.17
      },
      {
        "price": 545,
        "strength": 68,
        "type": "MOVING_AVERAGE",
        "source": "MA60",
        "test_count": 2,
        "distance_percent": -6.03
      },
      {
        "price": 500,
        "strength": 90,
        "type": "PSYCHOLOGICAL",
        "source": "整數關卡",
        "test_count": 3,
        "distance_percent": -13.79
      }
    ],
    "resistance_levels": [
      {
        "price": 590,
        "strength": 80,
        "type": "WAVE_PEAK",
        "source": "近期高點 2024-12-15",
        "test_count": 1,
        "last_test_date": "2024-12-15",
        "distance_percent": 1.72
      },
      {
        "price": 598,
        "strength": 75,
        "type": "WAVE_PEAK",
        "source": "波段高點 2024-11-25",
        "test_count": 1,
        "distance_percent": 3.10
      },
      {
        "price": 600,
        "strength": 90,
        "type": "PSYCHOLOGICAL",
        "source": "整數關卡",
        "test_count": 3,
        "distance_percent": 3.45
      },
      {
        "price": 620,
        "strength": 65,
        "type": "HISTORICAL",
        "source": "歷史高點 2024-07-15",
        "test_count": 0,
        "distance_percent": 6.90
      }
    ],
    "pivot_points": {
      "pivot": 573.33,
      "r1": 586.67,
      "r2": 593.33,
      "r3": 606.67,
      "s1": 566.67,
      "s2": 553.33,
      "s3": 546.67
    },
    "fibonacci_levels": {
      "reference_range": {
        "high": 598,
        "low": 520,
        "date_range": "2024-11-01 ~ 2024-11-25"
      },
      "retracement_levels": {
        "0%": 598,
        "23.6%": 579.60,
        "38.2%": 568.20,
        "50%": 559,
        "61.8%": 549.80,
        "100%": 520
      }
    },
    "interpretation": "股價目前在 565-590 區間震盪，565 為關鍵支撐，590-600 為主要壓力區"
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_006"
}
```

---

## 8. 全市場 K 線型態掃描

### GET `/api/v1/pattern/scan/kline`

掃描全市場 K 線型態。

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| trade_date | string | N | 最近交易日 | 交易日期 |
| pattern_types | string | N | all | 指定型態 |
| signal_filter | string | N | all | 訊號過濾（BULLISH, BEARISH） |
| min_strength | integer | N | 60 | 最低型態強度 |
| market_type | string | N | all | 市場類型（TWSE, OTC） |
| limit | integer | N | 100 | 回傳筆數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "trade_date": "2024-12-24",
    "scan_time_ms": 5200,
    "total_stocks_scanned": 1800,
    "total_patterns_found": 156,
    "pattern_summary": {
      "bullish_patterns": 89,
      "bearish_patterns": 45,
      "neutral_patterns": 22
    },
    "results": [
      {
        "stock_id": "2330",
        "stock_name": "台積電",
        "market_type": "TWSE",
        "industry": "半導體業",
        "close_price": 580,
        "change_percent": 2.11,
        "pattern_id": "KLINE020",
        "pattern_name": "看漲吞噬",
        "signal_type": "BULLISH_REVERSAL",
        "strength": 85,
        "volume_ratio": 1.35
      },
      {
        "stock_id": "2454",
        "stock_name": "聯發科",
        "market_type": "TWSE",
        "industry": "半導體業",
        "close_price": 856,
        "change_percent": 2.15,
        "pattern_id": "KLINE040",
        "pattern_name": "晨星",
        "signal_type": "BULLISH_REVERSAL",
        "strength": 82,
        "volume_ratio": 1.28
      }
    ],
    "pattern_distribution": {
      "KLINE020": 25,
      "KLINE021": 18,
      "KLINE040": 15,
      "KLINE041": 12,
      "KLINE001": 10
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_007"
}
```

---

## 9. 全市場圖表型態掃描

### GET `/api/v1/pattern/scan/chart`

掃描全市場圖表型態。

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| pattern_types | string | N | all | 指定型態 |
| status | string | N | all | 型態狀態 |
| min_strength | integer | N | 50 | 最低型態強度 |
| market_type | string | N | all | 市場類型 |
| limit | integer | N | 50 | 回傳筆數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "scan_date": "2024-12-24",
    "scan_time_ms": 28000,
    "total_stocks_scanned": 1800,
    "total_patterns_found": 45,
    "results": [
      {
        "stock_id": "2317",
        "stock_name": "鴻海",
        "market_type": "TWSE",
        "pattern_id": "CHART004",
        "pattern_name": "雙重底",
        "signal_type": "BULLISH_REVERSAL",
        "status": "CONFIRMED",
        "strength": 78,
        "neckline": 102,
        "target_price": 112,
        "current_price": 105.5,
        "potential_gain_percent": 6.16
      },
      {
        "stock_id": "2882",
        "stock_name": "國泰金",
        "market_type": "TWSE",
        "pattern_id": "CHART020",
        "pattern_name": "上升三角形",
        "signal_type": "BULLISH_CONTINUATION",
        "status": "FORMING",
        "strength": 72,
        "breakout_level": 48,
        "current_price": 45.6
      }
    ],
    "pattern_distribution": {
      "REVERSAL": 18,
      "CONTINUATION": 22,
      "GAP": 5
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_008"
}
```

---

## 10. 歷史型態紀錄查詢

### GET `/api/v1/pattern/{stockId}/history`

查詢股票歷史型態出現紀錄。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| start_date | string | N | 1年前 | 開始日期 |
| end_date | string | N | 今日 | 結束日期 |
| pattern_types | string | N | all | 指定型態 |
| min_strength | integer | N | 50 | 最低型態強度 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "query_period": {
      "start": "2024-01-01",
      "end": "2024-12-24"
    },
    "total_patterns": 45,
    "pattern_history": [
      {
        "pattern_id": "KLINE020",
        "pattern_name": "看漲吞噬",
        "occurrence_count": 8,
        "success_rate": 75,
        "avg_gain_5d": 2.8,
        "avg_gain_10d": 4.2,
        "occurrences": [
          {
            "date": "2024-12-23",
            "strength": 85,
            "price_at_signal": 580,
            "price_after_5d": null,
            "price_after_10d": null,
            "result": "PENDING"
          },
          {
            "date": "2024-10-15",
            "strength": 78,
            "price_at_signal": 545,
            "price_after_5d": 560,
            "price_after_10d": 572,
            "result": "SUCCESS"
          }
        ]
      },
      {
        "pattern_id": "CHART004",
        "pattern_name": "雙重底",
        "occurrence_count": 2,
        "success_rate": 100,
        "avg_gain_5d": 3.5,
        "avg_gain_10d": 6.8
      }
    ],
    "summary": {
      "most_frequent_pattern": "KLINE005",
      "most_reliable_pattern": "CHART004",
      "overall_pattern_success_rate": 68.5
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_009"
}
```

---

## 11. 型態統計分析

### GET `/api/v1/pattern/{stockId}/statistics`

查詢型態出現後的價格表現統計。

#### 路徑參數

| 參數名 | 類型 | 必填 | 說明 |
|-------|------|------|------|
| stockId | string | Y | 股票代碼 |

#### 查詢參數

| 參數名 | 類型 | 必填 | 預設值 | 說明 |
|-------|------|------|-------|------|
| pattern_id | string | N | all | 指定型態 ID |
| lookback_years | integer | N | 3 | 統計回溯年數 |

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "stock_id": "2330",
    "stock_name": "台積電",
    "analysis_period": {
      "start": "2022-01-01",
      "end": "2024-12-24",
      "trading_days": 750
    },
    "pattern_statistics": [
      {
        "pattern_id": "KLINE020",
        "pattern_name": "看漲吞噬",
        "total_occurrences": 25,
        "performance": {
          "success_rate": 72,
          "avg_gain_1d": 0.85,
          "avg_gain_3d": 1.65,
          "avg_gain_5d": 2.45,
          "avg_gain_10d": 3.80,
          "avg_gain_20d": 5.20,
          "max_gain": 12.5,
          "max_loss": -4.2,
          "avg_loss_when_failed": -2.1
        },
        "optimal_conditions": {
          "best_volume_ratio": ">1.5",
          "best_trend_context": "DOWNTREND",
          "best_strength_threshold": 70
        },
        "confidence": 78
      },
      {
        "pattern_id": "CHART003",
        "pattern_name": "雙重頂",
        "total_occurrences": 8,
        "performance": {
          "success_rate": 62.5,
          "avg_decline_after_neckline_break": -6.5,
          "avg_time_to_target": 15,
          "target_reached_rate": 58
        },
        "confidence": 65
      }
    ],
    "overall_statistics": {
      "total_patterns_analyzed": 150,
      "avg_success_rate": 65.5,
      "most_reliable_bullish": "KLINE040",
      "most_reliable_bearish": "KLINE021"
    }
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_010"
}
```

---

## 12. 型態元數據查詢

### GET `/api/v1/pattern/metadata/patterns`

查詢支援的型態清單與說明。

#### 成功回應 (200)

```json
{
  "code": 200,
  "message": "Success",
  "data": {
    "total_patterns": 50,
    "categories": [
      {
        "category": "KLINE_SINGLE",
        "category_name": "單根K線型態",
        "patterns": [
          {
            "pattern_id": "KLINE001",
            "name": "錘子線",
            "english_name": "Hammer",
            "signal_type": "BULLISH_REVERSAL",
            "description": "下影線為實體2倍以上，上影線很短，出現在下跌趨勢末端",
            "reliability": "MEDIUM",
            "priority": "P0"
          },
          {
            "pattern_id": "KLINE005",
            "name": "十字星",
            "english_name": "Doji",
            "signal_type": "NEUTRAL_REVERSAL",
            "description": "開盤價與收盤價幾乎相同",
            "reliability": "MEDIUM",
            "priority": "P0"
          }
        ]
      },
      {
        "category": "KLINE_DOUBLE",
        "category_name": "雙根K線型態",
        "patterns": [
          {
            "pattern_id": "KLINE020",
            "name": "看漲吞噬",
            "english_name": "Bullish Engulfing",
            "signal_type": "BULLISH_REVERSAL",
            "description": "陽線實體完全包覆前一根陰線實體",
            "reliability": "HIGH",
            "priority": "P0"
          }
        ]
      },
      {
        "category": "CHART_REVERSAL",
        "category_name": "圖表反轉型態",
        "patterns": [
          {
            "pattern_id": "CHART001",
            "name": "頭肩頂",
            "english_name": "Head and Shoulders",
            "signal_type": "BEARISH_REVERSAL",
            "description": "三個高點，中間最高，左右對稱",
            "reliability": "HIGH",
            "priority": "P0"
          }
        ]
      },
      {
        "category": "TREND",
        "category_name": "趨勢型態",
        "patterns": [
          {
            "pattern_id": "TREND001",
            "name": "上升趨勢",
            "english_name": "Uptrend",
            "signal_type": "BULLISH",
            "description": "連續的更高高點與更高低點",
            "reliability": "HIGH",
            "priority": "P0"
          }
        ]
      }
    ],
    "signals": [
      {
        "signal_id": "PTN_SIG_001",
        "name": "看漲K線型態",
        "type": "BUY",
        "source": "KLINE",
        "description": "識別到看漲K線型態（如錘子線、晨星、看漲吞噬）"
      },
      {
        "signal_id": "PTN_SIG_003",
        "name": "頭肩頂完成",
        "type": "SELL",
        "source": "CHART",
        "description": "頭肩頂型態突破頸線確認"
      }
    ]
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_011"
}
```

---

## 共用錯誤回應格式

```json
{
  "code": 400,
  "message": "Bad Request",
  "error": {
    "error_code": "M10_PTN_002",
    "error_message": "價格資料不足，無法偵測型態",
    "error_detail": "股票 2330 僅有 15 個交易日資料，圖表型態分析至少需要 30 個交易日",
    "suggestion": "請確認資料同步是否完成，或減少回溯天數"
  },
  "timestamp": "2024-12-24T15:30:00+08:00",
  "trace_id": "req_ptn_err_001"
}
```

---

## 📚 相關文檔

- [M10 功能需求](../functional/M10-技術型態辨識功能需求.md)
- [M10 資料庫設計](../../design/M10-資料庫設計.md)
- [API 回應格式總綱](../technical/00-全系統契約.md#44-api-回應格式)

---

**文件維護者**: 後端工程師
**最後更新**: 2026-01-12
**下次審核**: 2026-03-31
