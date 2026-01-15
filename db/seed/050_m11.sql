-- ============================================================
-- FinShark Seed: 050_m11.sql
-- Module: M11 - Quantitative Strategy
-- Description: Factor metadata definitions for strategy building
-- Idempotent: Yes (ON CONFLICT DO UPDATE)
-- ============================================================

-- ------------------------------------------------------------
-- 1. factor_metadata - Available factors for strategy conditions
-- ------------------------------------------------------------

INSERT INTO factor_metadata (
    factor_id, factor_name, display_name,
    category, source_module, data_type,
    value_range, typical_thresholds, supported_operators, default_operator,
    description, update_frequency, is_active
) VALUES

-- ============================================================
-- M06 Price/Volume Factors
-- ============================================================
('M06_CLOSE_PRICE', 'close_price', '收盤價',
 'PRICE_VOLUME', 'M06', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '股票收盤價', 'DAILY', true),

('M06_VOLUME', 'volume', '成交量',
 'PRICE_VOLUME', 'M06', 'INTEGER',
 '{"min": 0, "max": null}'::jsonb,
 '[1000000, 5000000, 10000000]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '股票成交量（股）', 'DAILY', true),

('M06_VOLUME_RATIO', 'volume_ratio', '量比',
 'PRICE_VOLUME', 'M06', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[0.5, 1.0, 1.5, 2.0]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '當日成交量 / 20日均量', 'DAILY', true),

('M06_PRICE_CHANGE_PCT', 'price_change_pct', '漲跌幅',
 'PRICE_VOLUME', 'M06', 'NUMERIC',
 '{"min": -10, "max": 10}'::jsonb,
 '[-3, -1, 0, 1, 3]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '當日漲跌幅（%）', 'DAILY', true),

-- ============================================================
-- M07 Technical Factors
-- ============================================================
('M07_RSI_14', 'rsi_14', 'RSI(14)',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": 0, "max": 100}'::jsonb,
 '[30, 50, 70]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb,
 'LESS_THAN',
 '14日相對強弱指標', 'DAILY', true),

('M07_KD_K', 'stoch_k', 'K值',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": 0, "max": 100}'::jsonb,
 '[20, 50, 80]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb,
 'LESS_THAN',
 'KD指標K值', 'DAILY', true),

('M07_KD_D', 'stoch_d', 'D值',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": 0, "max": 100}'::jsonb,
 '[20, 50, 80]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb,
 'LESS_THAN',
 'KD指標D值', 'DAILY', true),

('M07_MACD_HISTOGRAM', 'macd_histogram', 'MACD柱狀',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": null, "max": null}'::jsonb,
 '[-1, 0, 1]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb,
 'GREATER_THAN',
 'MACD柱狀體（MACD - Signal）', 'DAILY', true),

('M07_MA5', 'ma5', 'MA5',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb,
 'GREATER_THAN',
 '5日移動平均線', 'DAILY', true),

('M07_MA20', 'ma20', 'MA20',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb,
 'GREATER_THAN',
 '20日移動平均線', 'DAILY', true),

('M07_MA60', 'ma60', 'MA60',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb,
 'GREATER_THAN',
 '60日移動平均線', 'DAILY', true),

('M07_BBANDS_PERCENT_B', 'bbands_percent_b', '布林%B',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": 0, "max": 1}'::jsonb,
 '[0.2, 0.5, 0.8]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'LESS_THAN',
 '布林通道%B值', 'DAILY', true),

('M07_ATR_14', 'atr_14', 'ATR(14)',
 'TECHNICAL', 'M07', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'LESS_THAN',
 '14日平均真實波幅', 'DAILY', true),

-- ============================================================
-- M08 Fundamental Factors
-- ============================================================
('M08_PE_RATIO', 'pe_ratio', '本益比',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[10, 15, 20, 30]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'LESS_THAN',
 '股價 / 每股盈餘', 'QUARTERLY', true),

('M08_PB_RATIO', 'pb_ratio', '股價淨值比',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[1, 2, 3, 5]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'LESS_THAN',
 '股價 / 每股淨值', 'QUARTERLY', true),

('M08_ROE', 'roe', '股東權益報酬率',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": -100, "max": 100}'::jsonb,
 '[10, 15, 20, 25]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 'ROE（%）', 'QUARTERLY', true),

('M08_EPS', 'eps', '每股盈餘',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": null, "max": null}'::jsonb,
 '[0, 1, 3, 5]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 'EPS（元）', 'QUARTERLY', true),

('M08_DIVIDEND_YIELD', 'dividend_yield', '殖利率',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": 0, "max": 100}'::jsonb,
 '[3, 5, 7]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '現金股利殖利率（%）', 'YEARLY', true),

('M08_REVENUE_GROWTH_YOY', 'revenue_growth_yoy', '營收年增率',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": -100, "max": null}'::jsonb,
 '[0, 10, 20, 30]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '營收年增率（%）', 'MONTHLY', true),

('M08_PROFIT_MARGIN', 'profit_margin', '淨利率',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": -100, "max": 100}'::jsonb,
 '[5, 10, 15, 20]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '淨利率（%）', 'QUARTERLY', true),

('M08_CURRENT_RATIO', 'current_ratio', '流動比率',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": 0, "max": null}'::jsonb,
 '[1, 1.5, 2]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '流動資產 / 流動負債', 'QUARTERLY', true),

('M08_DEBT_RATIO', 'debt_ratio', '負債比率',
 'FUNDAMENTAL', 'M08', 'NUMERIC',
 '{"min": 0, "max": 100}'::jsonb,
 '[30, 50, 70]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'LESS_THAN',
 '負債比率（%）', 'QUARTERLY', true),

-- ============================================================
-- M09 Chip Factors
-- ============================================================
('M09_FOREIGN_NET', 'foreign_net', '外資買賣超',
 'CHIP', 'M09', 'INTEGER',
 '{"min": null, "max": null}'::jsonb,
 '[-5000000, 0, 5000000]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '外資買賣超（股）', 'DAILY', true),

('M09_FOREIGN_CONTINUOUS_DAYS', 'foreign_continuous_days', '外資連續買賣超天數',
 'CHIP', 'M09', 'INTEGER',
 '{"min": null, "max": null}'::jsonb,
 '[-5, 0, 3, 5, 10]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '外資連續買超為正、連續賣超為負', 'DAILY', true),

('M09_FOREIGN_ACCUMULATED_20D', 'foreign_accumulated_20d', '外資20日累計',
 'CHIP', 'M09', 'INTEGER',
 '{"min": null, "max": null}'::jsonb,
 '[-10000000, 0, 10000000]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '外資近20日累計買賣超（股）', 'DAILY', true),

('M09_TRUST_NET', 'trust_net', '投信買賣超',
 'CHIP', 'M09', 'INTEGER',
 '{"min": null, "max": null}'::jsonb,
 '[-1000000, 0, 1000000]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '投信買賣超（股）', 'DAILY', true),

('M09_TRUST_CONTINUOUS_DAYS', 'trust_continuous_days', '投信連續買賣超天數',
 'CHIP', 'M09', 'INTEGER',
 '{"min": null, "max": null}'::jsonb,
 '[-5, 0, 3, 5]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '投信連續買超為正、連續賣超為負', 'DAILY', true),

('M09_DEALER_NET', 'dealer_net', '自營商買賣超',
 'CHIP', 'M09', 'INTEGER',
 '{"min": null, "max": null}'::jsonb,
 '[-500000, 0, 500000]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '自營商買賣超（股）', 'DAILY', true),

('M09_TOTAL_NET', 'total_net', '三大法人合計',
 'CHIP', 'M09', 'INTEGER',
 '{"min": null, "max": null}'::jsonb,
 '[-5000000, 0, 5000000]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '三大法人買賣超合計（股）', 'DAILY', true),

('M09_MARGIN_BALANCE', 'margin_balance', '融資餘額',
 'CHIP', 'M09', 'INTEGER',
 '{"min": 0, "max": null}'::jsonb,
 '[]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'LESS_THAN',
 '融資餘額（張）', 'DAILY', true),

('M09_MARGIN_CHANGE', 'margin_change', '融資增減',
 'CHIP', 'M09', 'INTEGER',
 '{"min": null, "max": null}'::jsonb,
 '[-1000, 0, 1000]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'LESS_THAN',
 '融資增減（張）', 'DAILY', true),

('M09_MARGIN_SHORT_RATIO', 'margin_short_ratio', '券資比',
 'CHIP', 'M09', 'NUMERIC',
 '{"min": 0, "max": 100}'::jsonb,
 '[10, 20, 30]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '融券餘額 / 融資餘額（%）', 'DAILY', true),

('M09_CHIP_SCORE', 'chip_score', '籌碼評分',
 'CHIP', 'M09', 'INTEGER',
 '{"min": 0, "max": 100}'::jsonb,
 '[30, 50, 70]'::jsonb,
 '["GREATER_THAN", "LESS_THAN", "BETWEEN"]'::jsonb,
 'GREATER_THAN',
 '籌碼綜合評分（0-100）', 'DAILY', true)

ON CONFLICT (factor_id) DO UPDATE SET
    factor_name = EXCLUDED.factor_name,
    display_name = EXCLUDED.display_name,
    category = EXCLUDED.category,
    source_module = EXCLUDED.source_module,
    data_type = EXCLUDED.data_type,
    value_range = EXCLUDED.value_range,
    typical_thresholds = EXCLUDED.typical_thresholds,
    supported_operators = EXCLUDED.supported_operators,
    default_operator = EXCLUDED.default_operator,
    description = EXCLUDED.description,
    update_frequency = EXCLUDED.update_frequency,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 2. strategies - Preset strategy definitions
-- ------------------------------------------------------------

INSERT INTO strategies (
    strategy_id, strategy_name, strategy_type, description,
    current_version, status, is_preset,
    conditions, parameters, output_config, created_by
) VALUES

-- ============================================================
-- Momentum Strategies (動能策略)
-- ============================================================
('STR_MOMENTUM_001', 'RSI超賣反彈策略', 'MOMENTUM',
 'RSI低於30且量能放大時買進，適合短線反彈操作',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M07_RSI_14", "operator": "LESS_THAN", "value": 30}, {"factor_id": "M06_VOLUME_RATIO", "operator": "GREATER_THAN", "value": 1.2}]}'::jsonb,
 '{"rsi_threshold": 30, "volume_ratio_min": 1.2}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "(30 - rsi) / 30 * 0.5 + volume_factor * 0.5"}'::jsonb,
 'SYSTEM'),

('STR_MOMENTUM_002', 'KD黃金交叉策略', 'MOMENTUM',
 'KD指標黃金交叉且法人買超時買進',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M07_KD_K", "operator": "CROSS_ABOVE", "reference": "M07_KD_D"}, {"factor_id": "M07_KD_K", "operator": "LESS_THAN", "value": 50}, {"factor_id": "M09_TOTAL_NET", "operator": "GREATER_THAN", "value": 0}]}'::jsonb,
 '{"kd_max": 50}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "(50 - kd_k) / 50 * 0.6 + chip_score * 0.4"}'::jsonb,
 'SYSTEM'),

('STR_MOMENTUM_003', 'MACD多頭確認策略', 'MOMENTUM',
 'MACD柱狀由負轉正且站上均線時買進',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M07_MACD_HISTOGRAM", "operator": "CROSS_ABOVE", "value": 0}, {"factor_id": "M06_CLOSE_PRICE", "operator": "GREATER_THAN", "reference": "M07_MA20"}]}'::jsonb,
 '{}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "macd_strength * 0.7 + trend_score * 0.3"}'::jsonb,
 'SYSTEM'),

-- ============================================================
-- Value Strategies (價值策略)
-- ============================================================
('STR_VALUE_001', '低本益比高殖利率策略', 'VALUE',
 '本益比低於15且殖利率高於4%的價值股',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M08_PE_RATIO", "operator": "LESS_THAN", "value": 15}, {"factor_id": "M08_PE_RATIO", "operator": "GREATER_THAN", "value": 0}, {"factor_id": "M08_DIVIDEND_YIELD", "operator": "GREATER_THAN", "value": 4}]}'::jsonb,
 '{"pe_max": 15, "dividend_yield_min": 4}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "(15 - pe) / 15 * 0.5 + dividend_yield / 10 * 0.5"}'::jsonb,
 'SYSTEM'),

('STR_VALUE_002', '低股價淨值比策略', 'VALUE',
 '股價淨值比低於1.5且ROE高於10%',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M08_PB_RATIO", "operator": "LESS_THAN", "value": 1.5}, {"factor_id": "M08_ROE", "operator": "GREATER_THAN", "value": 10}]}'::jsonb,
 '{"pb_max": 1.5, "roe_min": 10}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "(1.5 - pb) / 1.5 * 0.4 + roe / 30 * 0.6"}'::jsonb,
 'SYSTEM'),

('STR_VALUE_003', '成長價值混合策略', 'VALUE',
 'PEG低於1且營收年增率正成長',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M08_PE_RATIO", "operator": "LESS_THAN", "value": 20}, {"factor_id": "M08_REVENUE_GROWTH_YOY", "operator": "GREATER_THAN", "value": 10}, {"factor_id": "M08_EPS", "operator": "GREATER_THAN", "value": 0}]}'::jsonb,
 '{"pe_max": 20, "growth_min": 10}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "growth_score * 0.6 + value_score * 0.4"}'::jsonb,
 'SYSTEM'),

-- ============================================================
-- Hybrid Strategies (混合策略)
-- ============================================================
('STR_HYBRID_001', '技術面法人同買策略', 'HYBRID',
 'RSI超賣且法人連續買超',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M07_RSI_14", "operator": "LESS_THAN", "value": 35}, {"factor_id": "M09_FOREIGN_CONTINUOUS_DAYS", "operator": "GREATER_THAN", "value": 3}, {"factor_id": "M09_TRUST_NET", "operator": "GREATER_THAN", "value": 0}]}'::jsonb,
 '{"rsi_threshold": 35, "foreign_days_min": 3}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "tech_score * 0.4 + chip_score * 0.6"}'::jsonb,
 'SYSTEM'),

('STR_HYBRID_002', '價值動能雙確認策略', 'HYBRID',
 '低本益比且技術面轉強的股票',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M08_PE_RATIO", "operator": "BETWEEN", "value": [0, 15]}, {"factor_id": "M07_RSI_14", "operator": "BETWEEN", "value": [30, 50]}, {"factor_id": "M07_MACD_HISTOGRAM", "operator": "GREATER_THAN", "value": 0}]}'::jsonb,
 '{"pe_range": [0, 15], "rsi_range": [30, 50]}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "value_score * 0.5 + momentum_score * 0.5"}'::jsonb,
 'SYSTEM'),

('STR_HYBRID_003', '籌碼集中低估值策略', 'HYBRID',
 '法人大買且估值合理的股票',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M09_FOREIGN_ACCUMULATED_20D", "operator": "GREATER_THAN", "value": 5000000}, {"factor_id": "M08_PE_RATIO", "operator": "LESS_THAN", "value": 20}, {"factor_id": "M08_ROE", "operator": "GREATER_THAN", "value": 12}]}'::jsonb,
 '{"foreign_accumulated_min": 5000000, "pe_max": 20, "roe_min": 12}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "chip_score * 0.5 + value_score * 0.5"}'::jsonb,
 'SYSTEM'),

('STR_HYBRID_004', '突破放量策略', 'HYBRID',
 '價格突破20日均線且成交量放大',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M06_CLOSE_PRICE", "operator": "CROSS_ABOVE", "reference": "M07_MA20"}, {"factor_id": "M06_VOLUME_RATIO", "operator": "GREATER_THAN", "value": 1.5}, {"factor_id": "M07_RSI_14", "operator": "LESS_THAN", "value": 70}]}'::jsonb,
 '{"volume_ratio_min": 1.5, "rsi_max": 70}'::jsonb,
 '{"signal_type": "BUY", "confidence_formula": "breakout_score * 0.6 + volume_score * 0.4"}'::jsonb,
 'SYSTEM'),

-- ============================================================
-- Sell Strategies (賣出策略)
-- ============================================================
('STR_SELL_001', 'RSI超買出場策略', 'MOMENTUM',
 'RSI高於70時提示賣出',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M07_RSI_14", "operator": "GREATER_THAN", "value": 70}]}'::jsonb,
 '{"rsi_threshold": 70}'::jsonb,
 '{"signal_type": "SELL", "confidence_formula": "(rsi - 70) / 30"}'::jsonb,
 'SYSTEM'),

('STR_SELL_002', '法人連續賣超出場策略', 'HYBRID',
 '法人連續賣超且跌破均線時賣出',
 1, 'ACTIVE', true,
 '{"logic": "AND", "conditions": [{"factor_id": "M09_FOREIGN_CONTINUOUS_DAYS", "operator": "LESS_THAN", "value": -3}, {"factor_id": "M06_CLOSE_PRICE", "operator": "LESS_THAN", "reference": "M07_MA20"}]}'::jsonb,
 '{"foreign_days_min": -3}'::jsonb,
 '{"signal_type": "SELL", "confidence_formula": "abs(foreign_days) / 10 * 0.5 + break_score * 0.5"}'::jsonb,
 'SYSTEM')

ON CONFLICT (strategy_id) DO UPDATE SET
    strategy_name = EXCLUDED.strategy_name,
    strategy_type = EXCLUDED.strategy_type,
    description = EXCLUDED.description,
    current_version = EXCLUDED.current_version,
    status = EXCLUDED.status,
    is_preset = EXCLUDED.is_preset,
    conditions = EXCLUDED.conditions,
    parameters = EXCLUDED.parameters,
    output_config = EXCLUDED.output_config,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- End of 050_m11.sql
-- ============================================================
