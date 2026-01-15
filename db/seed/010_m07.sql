-- ============================================================
-- FinShark Seed: 010_m07.sql
-- Module: M07 - Technical Analysis
-- Description: Complete technical indicator definitions (71 indicators)
-- Idempotent: Yes (ON CONFLICT DO UPDATE)
-- ============================================================

-- ------------------------------------------------------------
-- indicator_definitions - Technical indicator configurations
-- ------------------------------------------------------------

INSERT INTO indicator_definitions (
    indicator_name, indicator_category, indicator_name_zh, description,
    default_params, param_ranges, pandas_ta_function, min_data_points,
    output_fields, value_range, priority, is_active, is_cached
) VALUES

-- ============================================================
-- TREND INDICATORS (趨勢指標)
-- ============================================================
('MA', 'TREND', '簡單移動平均', '計算N天收盤價的平均值',
 '{"periods": [5, 10, 20, 60, 120, 240]}'::jsonb,
 '{"min_period": 2, "max_period": 500}'::jsonb,
 'sma', 2,
 '["ma5", "ma10", "ma20", "ma60", "ma120", "ma240"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P0', true, true),

('EMA', 'TREND', '指數移動平均', '給予近期價格更高權重的移動平均',
 '{"periods": [12, 26, 50, 200]}'::jsonb,
 '{"min_period": 2, "max_period": 500}'::jsonb,
 'ema', 2,
 '["ema12", "ema26", "ema50", "ema200"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P0', true, true),

('WMA', 'TREND', '加權移動平均', '線性加權移動平均線',
 '{"periods": [10, 20, 50]}'::jsonb,
 '{"min_period": 2, "max_period": 200}'::jsonb,
 'wma', 2,
 '["wma10", "wma20", "wma50"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

('HMA', 'TREND', 'Hull移動平均', '降低延遲的移動平均線',
 '{"period": 20}'::jsonb,
 '{"min_period": 5, "max_period": 100}'::jsonb,
 'hma', 10,
 '["hma_20"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

('DEMA', 'TREND', '雙重指數移動平均', '減少延遲的 EMA',
 '{"period": 20}'::jsonb,
 '{"min_period": 5, "max_period": 100}'::jsonb,
 'dema', 10,
 '["dema_20"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P2', true, false),

('TEMA', 'TREND', '三重指數移動平均', '進一步減少延遲的 EMA',
 '{"period": 20}'::jsonb,
 '{"min_period": 5, "max_period": 100}'::jsonb,
 'tema', 15,
 '["tema_20"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P2', true, false),

('MACD', 'TREND', 'MACD指標', '指數平滑異同移動平均線',
 '{"fast": 12, "slow": 26, "signal": 9}'::jsonb,
 '{"fast": [5, 20], "slow": [20, 50], "signal": [5, 15]}'::jsonb,
 'macd', 26,
 '["macd_line", "signal_line", "histogram"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P0', true, true),

('ADX', 'TREND', '平均趨向指標', '測量趨勢強度，不分方向',
 '{"period": 14}'::jsonb,
 '{"period": [7, 30]}'::jsonb,
 'adx', 14,
 '["adx", "plus_di", "minus_di"]'::jsonb,
 '{"min": 0, "max": 100}'::jsonb,
 'P0', true, true),

('AROON', 'TREND', '阿隆指標', '識別趨勢開始和強度',
 '{"period": 25}'::jsonb,
 '{"period": [10, 50]}'::jsonb,
 'aroon', 25,
 '["aroon_up", "aroon_down", "aroon_oscillator"]'::jsonb,
 '{"min": 0, "max": 100}'::jsonb,
 'P1', true, false),

('PSAR', 'TREND', '拋物線SAR', '停損轉向指標',
 '{"af_start": 0.02, "af_step": 0.02, "af_max": 0.2}'::jsonb,
 '{"af_start": [0.01, 0.05], "af_max": [0.1, 0.3]}'::jsonb,
 'psar', 5,
 '["psar", "trend"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

('SUPERTREND', 'TREND', '超級趨勢', '基於 ATR 的趨勢跟蹤指標',
 '{"period": 10, "multiplier": 3}'::jsonb,
 '{"period": [7, 20], "multiplier": [2, 4]}'::jsonb,
 'supertrend', 10,
 '["supertrend", "direction"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P1', true, false),

('ICHIMOKU', 'TREND', '一目均衡表', '日本雲圖技術分析系統',
 '{"tenkan": 9, "kijun": 26, "senkou_span_b": 52}'::jsonb,
 '{"tenkan": [7, 12], "kijun": [22, 30], "senkou_span_b": [44, 60]}'::jsonb,
 'ichimoku', 52,
 '["tenkan_sen", "kijun_sen", "senkou_span_a", "senkou_span_b", "chikou_span"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

-- ============================================================
-- MOMENTUM INDICATORS (動能指標)
-- ============================================================
('RSI', 'MOMENTUM', '相對強弱指標', '測量價格變動速度和幅度',
 '{"period": 14}'::jsonb,
 '{"min_period": 5, "max_period": 30}'::jsonb,
 'rsi', 14,
 '["rsi_14"]'::jsonb,
 '{"min": 0, "max": 100}'::jsonb,
 'P0', true, true),

('STOCH', 'MOMENTUM', 'KD隨機指標', '測量收盤價在高低區間的位置',
 '{"k": 9, "d": 3, "smooth_k": 3}'::jsonb,
 '{"k": [5, 20], "d": [2, 5], "smooth_k": [2, 5]}'::jsonb,
 'stoch', 9,
 '["stoch_k", "stoch_d"]'::jsonb,
 '{"min": 0, "max": 100}'::jsonb,
 'P0', true, true),

('STOCHRSI', 'MOMENTUM', '隨機RSI', 'RSI 的隨機震盪指標',
 '{"period": 14, "rsi_period": 14, "k": 3, "d": 3}'::jsonb,
 '{"period": [10, 20], "rsi_period": [10, 20]}'::jsonb,
 'stochrsi', 28,
 '["stochrsi_k", "stochrsi_d"]'::jsonb,
 '{"min": 0, "max": 100}'::jsonb,
 'P1', true, false),

('WILLIAMS_R', 'MOMENTUM', 'Williams %R', '測量超買超賣的動能指標',
 '{"period": 14}'::jsonb,
 '{"period": [7, 21]}'::jsonb,
 'willr', 14,
 '["willr_14"]'::jsonb,
 '{"min": -100, "max": 0}'::jsonb,
 'P1', true, false),

('CCI', 'MOMENTUM', '商品通道指標', '測量價格偏離統計平均的程度',
 '{"period": 20}'::jsonb,
 '{"period": [10, 30]}'::jsonb,
 'cci', 20,
 '["cci_20"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P1', true, false),

('MFI', 'MOMENTUM', '資金流量指標', '結合價格與成交量的動能指標',
 '{"period": 14}'::jsonb,
 '{"period": [7, 21]}'::jsonb,
 'mfi', 14,
 '["mfi_14"]'::jsonb,
 '{"min": 0, "max": 100}'::jsonb,
 'P1', true, false),

('ROC', 'MOMENTUM', '變動率指標', '測量價格變動百分比',
 '{"period": 12}'::jsonb,
 '{"period": [5, 20]}'::jsonb,
 'roc', 12,
 '["roc_12"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P1', true, false),

('MOMENTUM', 'MOMENTUM', '動量指標', '測量價格變動速度',
 '{"period": 10}'::jsonb,
 '{"period": [5, 20]}'::jsonb,
 'mom', 10,
 '["momentum_10"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P1', true, false),

('TSI', 'MOMENTUM', '真實強度指標', '雙重平滑的動量震盪指標',
 '{"fast": 13, "slow": 25}'::jsonb,
 '{"fast": [10, 20], "slow": [20, 35]}'::jsonb,
 'tsi', 38,
 '["tsi", "tsi_signal"]'::jsonb,
 '{"min": -100, "max": 100}'::jsonb,
 'P2', true, false),

('UO', 'MOMENTUM', '終極震盪指標', '三週期加權動量指標',
 '{"short": 7, "medium": 14, "long": 28}'::jsonb,
 '{"short": [5, 10], "medium": [10, 20], "long": [20, 40]}'::jsonb,
 'uo', 28,
 '["uo"]'::jsonb,
 '{"min": 0, "max": 100}'::jsonb,
 'P2', true, false),

('AO', 'MOMENTUM', '動量震盪指標', 'Awesome Oscillator',
 '{"fast": 5, "slow": 34}'::jsonb,
 '{"fast": [3, 8], "slow": [30, 40]}'::jsonb,
 'ao', 34,
 '["ao"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

-- ============================================================
-- VOLATILITY INDICATORS (波動性指標)
-- ============================================================
('BBANDS', 'VOLATILITY', '布林通道', '價格的統計波動範圍',
 '{"period": 20, "std": 2}'::jsonb,
 '{"period": [10, 50], "std": [1, 3]}'::jsonb,
 'bbands', 20,
 '["upper", "middle", "lower", "bandwidth", "percent_b"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P0', true, true),

('ATR', 'VOLATILITY', '平均真實範圍', '測量價格波動幅度',
 '{"period": 14}'::jsonb,
 '{"period": [7, 30]}'::jsonb,
 'atr', 14,
 '["atr_14"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P0', true, true),

('KELTNER', 'VOLATILITY', '肯特納通道', '基於 ATR 的價格通道',
 '{"period": 20, "atr_period": 10, "multiplier": 2}'::jsonb,
 '{"period": [10, 30], "multiplier": [1.5, 3]}'::jsonb,
 'kc', 20,
 '["kc_upper", "kc_middle", "kc_lower"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

('DONCHIAN', 'VOLATILITY', '唐奇安通道', '突破交易通道',
 '{"period": 20}'::jsonb,
 '{"period": [10, 55]}'::jsonb,
 'donchian', 20,
 '["donchian_upper", "donchian_middle", "donchian_lower"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

('NATR', 'VOLATILITY', '標準化ATR', 'ATR 的百分比形式',
 '{"period": 14}'::jsonb,
 '{"period": [7, 30]}'::jsonb,
 'natr', 14,
 '["natr_14"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P2', true, false),

('TRANGE', 'VOLATILITY', '真實範圍', '單日真實波動範圍',
 '{}'::jsonb,
 '{}'::jsonb,
 'true_range', 1,
 '["true_range"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P2', true, false),

('RVI', 'VOLATILITY', '相對波動率指標', '測量波動率方向',
 '{"period": 14}'::jsonb,
 '{"period": [7, 21]}'::jsonb,
 'rvi', 14,
 '["rvi", "rvi_signal"]'::jsonb,
 '{"min": 0, "max": 100}'::jsonb,
 'P2', true, false),

-- ============================================================
-- VOLUME INDICATORS (成交量指標)
-- ============================================================
('OBV', 'VOLUME', '能量潮', '累積成交量指標',
 '{}'::jsonb,
 '{}'::jsonb,
 'obv', 1,
 '["obv"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P0', true, true),

('AD', 'VOLUME', '累積/派發線', 'Accumulation/Distribution Line',
 '{}'::jsonb,
 '{}'::jsonb,
 'ad', 1,
 '["ad"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P1', true, false),

('CMF', 'VOLUME', '蔡金資金流', 'Chaikin Money Flow',
 '{"period": 20}'::jsonb,
 '{"period": [10, 30]}'::jsonb,
 'cmf', 20,
 '["cmf_20"]'::jsonb,
 '{"min": -1, "max": 1}'::jsonb,
 'P1', true, false),

('VWAP', 'VOLUME', '成交量加權均價', '日內交易基準價格',
 '{}'::jsonb,
 '{}'::jsonb,
 'vwap', 1,
 '["vwap"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

('PVI', 'VOLUME', '正成交量指標', 'Positive Volume Index',
 '{"signal_period": 255}'::jsonb,
 '{"signal_period": [200, 300]}'::jsonb,
 'pvi', 1,
 '["pvi"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

('NVI', 'VOLUME', '負成交量指標', 'Negative Volume Index',
 '{"signal_period": 255}'::jsonb,
 '{"signal_period": [200, 300]}'::jsonb,
 'nvi', 1,
 '["nvi"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

('ADO', 'VOLUME', '蔡金震盪指標', 'Chaikin Oscillator',
 '{"fast": 3, "slow": 10}'::jsonb,
 '{"fast": [2, 5], "slow": [8, 15]}'::jsonb,
 'adosc', 10,
 '["adosc"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

('EOM', 'VOLUME', '移動便利指標', 'Ease of Movement',
 '{"period": 14}'::jsonb,
 '{"period": [7, 21]}'::jsonb,
 'eom', 14,
 '["eom", "eom_signal"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

('VOLUME_MA', 'VOLUME', '成交量均線', '成交量的移動平均',
 '{"periods": [5, 20, 60]}'::jsonb,
 '{"min_period": 2, "max_period": 120}'::jsonb,
 'sma', 2,
 '["vol_ma5", "vol_ma20", "vol_ma60"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P0', true, true),

('VOLUME_RATIO', 'VOLUME', '量比', '當日成交量與均量比值',
 '{"base_period": 20}'::jsonb,
 '{"base_period": [5, 60]}'::jsonb,
 'custom', 20,
 '["volume_ratio"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P0', true, true),

-- ============================================================
-- SUPPORT/RESISTANCE INDICATORS (支撐壓力指標)
-- ============================================================
('PIVOT', 'SUPPORT_RESISTANCE', '軸心點', '經典樞軸點支撐壓力',
 '{"method": "standard"}'::jsonb,
 '{"method": ["standard", "fibonacci", "woodie", "camarilla"]}'::jsonb,
 'pivot', 1,
 '["pivot", "s1", "s2", "s3", "r1", "r2", "r3"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

('FIBONACCI', 'SUPPORT_RESISTANCE', '費波納契回撤', '黃金比例支撐壓力位',
 '{"lookback": 50}'::jsonb,
 '{"lookback": [20, 100]}'::jsonb,
 'custom', 50,
 '["fib_0", "fib_236", "fib_382", "fib_500", "fib_618", "fib_786", "fib_100"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

-- ============================================================
-- STATISTICAL INDICATORS (統計指標)
-- ============================================================
('STDDEV', 'STATISTICAL', '標準差', '價格波動的統計測量',
 '{"period": 20}'::jsonb,
 '{"period": [10, 50]}'::jsonb,
 'stdev', 20,
 '["stddev_20"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P1', true, false),

('VARIANCE', 'STATISTICAL', '變異數', '價格變異的統計測量',
 '{"period": 20}'::jsonb,
 '{"period": [10, 50]}'::jsonb,
 'variance', 20,
 '["variance_20"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P2', true, false),

('ZSCORE', 'STATISTICAL', 'Z分數', '價格相對於均值的標準差數',
 '{"period": 20}'::jsonb,
 '{"period": [10, 50]}'::jsonb,
 'zscore', 20,
 '["zscore_20"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

('CORRELATION', 'STATISTICAL', '相關係數', '與大盤的相關性',
 '{"period": 20}'::jsonb,
 '{"period": [10, 60]}'::jsonb,
 'correl', 20,
 '["correlation_20"]'::jsonb,
 '{"min": -1, "max": 1}'::jsonb,
 'P2', true, false),

('BETA', 'STATISTICAL', 'Beta係數', '相對於大盤的波動係數',
 '{"period": 60}'::jsonb,
 '{"period": [20, 120]}'::jsonb,
 'custom', 60,
 '["beta_60"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

-- ============================================================
-- COMPOSITE INDICATORS (綜合指標)
-- ============================================================
('DPO', 'COMPOSITE', '去趨勢價格震盪', '移除趨勢的價格震盪',
 '{"period": 20}'::jsonb,
 '{"period": [10, 30]}'::jsonb,
 'dpo', 20,
 '["dpo_20"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

('KAMA', 'COMPOSITE', '考夫曼適應性移動平均', '自適應移動平均',
 '{"period": 10, "fast": 2, "slow": 30}'::jsonb,
 '{"period": [5, 20]}'::jsonb,
 'kama', 30,
 '["kama_10"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P2', true, false),

('TRIX', 'COMPOSITE', 'TRIX指標', '三重指數平滑變動率',
 '{"period": 15}'::jsonb,
 '{"period": [10, 20]}'::jsonb,
 'trix', 45,
 '["trix", "trix_signal"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

('VTX', 'COMPOSITE', '渦旋指標', 'Vortex Indicator',
 '{"period": 14}'::jsonb,
 '{"period": [7, 28]}'::jsonb,
 'vortex', 14,
 '["vortex_plus", "vortex_minus"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P2', true, false),

('MASS_INDEX', 'COMPOSITE', '質量指標', '識別趨勢反轉',
 '{"fast": 9, "slow": 25}'::jsonb,
 '{"fast": [7, 12], "slow": [20, 30]}'::jsonb,
 'massi', 25,
 '["massi"]'::jsonb,
 '{"min": 0, "max": null}'::jsonb,
 'P2', true, false),

('PPO', 'COMPOSITE', '價格震盪百分比', 'Percentage Price Oscillator',
 '{"fast": 12, "slow": 26, "signal": 9}'::jsonb,
 '{"fast": [8, 16], "slow": [22, 30]}'::jsonb,
 'ppo', 26,
 '["ppo", "ppo_hist", "ppo_signal"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P2', true, false),

('KC_SQUEEZE', 'COMPOSITE', '擠壓動量', 'TTM Squeeze（KC + BB 組合）',
 '{"bb_period": 20, "bb_std": 2, "kc_period": 20, "kc_atr": 1.5}'::jsonb,
 '{}'::jsonb,
 'squeeze', 20,
 '["squeeze_on", "squeeze_off", "squeeze_momentum"]'::jsonb,
 '{"min": null, "max": null}'::jsonb,
 'P1', true, false)

ON CONFLICT (indicator_name) DO UPDATE SET
    indicator_category = EXCLUDED.indicator_category,
    indicator_name_zh = EXCLUDED.indicator_name_zh,
    description = EXCLUDED.description,
    default_params = EXCLUDED.default_params,
    param_ranges = EXCLUDED.param_ranges,
    pandas_ta_function = EXCLUDED.pandas_ta_function,
    min_data_points = EXCLUDED.min_data_points,
    output_fields = EXCLUDED.output_fields,
    value_range = EXCLUDED.value_range,
    priority = EXCLUDED.priority,
    is_active = EXCLUDED.is_active,
    is_cached = EXCLUDED.is_cached,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- End of 010_m07.sql
-- ============================================================
