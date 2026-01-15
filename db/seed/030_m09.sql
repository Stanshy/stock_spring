-- ============================================================
-- FinShark Seed: 030_m09.sql
-- Module: M09 - Chip Analysis
-- Description: Chip signal definitions and ranking types
-- Idempotent: Yes (ON CONFLICT DO UPDATE)
-- ============================================================

-- ------------------------------------------------------------
-- 1. chip_signal_definitions - Chip signal type definitions
-- ------------------------------------------------------------

INSERT INTO chip_signal_definitions (
    signal_code, signal_name, signal_name_en, signal_type,
    default_severity, description, detection_rule,
    recommendation, is_active, display_order
) VALUES

-- ============================================================
-- INSTITUTIONAL SIGNALS (法人信號)
-- ============================================================
('CHIP_SIG_001', '外資大買', 'Foreign Large Buy', 'INSTITUTIONAL',
 'HIGH',
 '外資單日買超金額或張數超過一定門檻',
 '{"type": "threshold", "field": "foreign_net", "operator": ">=", "value": 10000000}'::jsonb,
 '外資大量買進，可能代表中長期看好，建議關注後續買超持續性',
 true, 1),

('CHIP_SIG_002', '外資大賣', 'Foreign Large Sell', 'INSTITUTIONAL',
 'HIGH',
 '外資單日賣超金額或張數超過一定門檻',
 '{"type": "threshold", "field": "foreign_net", "operator": "<=", "value": -10000000}'::jsonb,
 '外資大量賣出，需留意是否為趨勢轉變，建議觀察賣壓是否持續',
 true, 2),

('CHIP_SIG_003', '外資連續買超', 'Foreign Consecutive Buy', 'INSTITUTIONAL',
 'MEDIUM',
 '外資連續多日買超',
 '{"type": "consecutive", "field": "foreign_net", "operator": ">", "value": 0, "days": 5}'::jsonb,
 '外資持續布局，籌碼逐漸集中，中期偏多看待',
 true, 3),

('CHIP_SIG_004', '外資連續賣超', 'Foreign Consecutive Sell', 'INSTITUTIONAL',
 'MEDIUM',
 '外資連續多日賣超',
 '{"type": "consecutive", "field": "foreign_net", "operator": "<", "value": 0, "days": 5}'::jsonb,
 '外資持續出貨，籌碼逐漸分散，中期偏空看待',
 true, 4),

('CHIP_SIG_005', '投信大買', 'Trust Large Buy', 'INSTITUTIONAL',
 'HIGH',
 '投信單日買超金額或張數超過一定門檻',
 '{"type": "threshold", "field": "trust_net", "operator": ">=", "value": 1000000}'::jsonb,
 '投信積極買進，可能納入投組標的，關注是否持續加碼',
 true, 5),

('CHIP_SIG_006', '投信連續買超', 'Trust Consecutive Buy', 'INSTITUTIONAL',
 'MEDIUM',
 '投信連續多日買超',
 '{"type": "consecutive", "field": "trust_net", "operator": ">", "value": 0, "days": 3}'::jsonb,
 '投信持續買進，可能為基金建倉期，後勢可期',
 true, 6),

('CHIP_SIG_007', '三大法人同買', 'All Institutional Buy', 'INSTITUTIONAL',
 'MEDIUM',
 '外資、投信、自營商同日買超',
 '{"type": "multi_condition", "conditions": [{"field": "foreign_net", "operator": ">", "value": 0}, {"field": "trust_net", "operator": ">", "value": 0}, {"field": "dealer_net", "operator": ">", "value": 0}]}'::jsonb,
 '三大法人同步看好，籌碼面強勁，短線偏多',
 true, 7),

('CHIP_SIG_008', '三大法人同賣', 'All Institutional Sell', 'INSTITUTIONAL',
 'HIGH',
 '外資、投信、自營商同日賣超',
 '{"type": "multi_condition", "conditions": [{"field": "foreign_net", "operator": "<", "value": 0}, {"field": "trust_net", "operator": "<", "value": 0}, {"field": "dealer_net", "operator": "<", "value": 0}]}'::jsonb,
 '三大法人同步出貨，籌碼面轉弱，需提高警覺',
 true, 8),

('CHIP_SIG_015', '外資大量回補', 'Foreign Large Cover', 'INSTITUTIONAL',
 'MEDIUM',
 '外資在前一日大賣後大量回補',
 '{"type": "pattern", "pattern": "reversal", "field": "foreign_net", "threshold": 5000000}'::jsonb,
 '外資快速回補，可能為短線調節後重新布局',
 true, 15),

('CHIP_SIG_016', '投信調節後再買', 'Trust Rebuy After Sell', 'INSTITUTIONAL',
 'MEDIUM',
 '投信賣超後隔日轉買超',
 '{"type": "pattern", "pattern": "reversal", "field": "trust_net"}'::jsonb,
 '投信調節籌碼後重新買進，可能為換股操作',
 true, 16),

-- ============================================================
-- MARGIN SIGNALS (融資融券信號)
-- ============================================================
('CHIP_SIG_009', '融資暴增', 'Margin Surge', 'MARGIN',
 'HIGH',
 '融資餘額單日大幅增加',
 '{"type": "change_rate", "field": "margin_change", "operator": ">=", "rate": 0.05}'::jsonb,
 '散戶大量融資買進，若股價已高可能為追漲，需留意套牢賣壓',
 true, 9),

('CHIP_SIG_010', '融資斷頭', 'Margin Call', 'MARGIN',
 'CRITICAL',
 '融資餘額大幅下降伴隨股價重挫',
 '{"type": "multi_condition", "conditions": [{"field": "margin_change", "operator": "<=", "rate": -0.10}, {"field": "price_change", "operator": "<=", "value": -0.05}]}'::jsonb,
 '融資戶被迫斷頭，恐慌性賣壓，需等待籌碼沈澱',
 true, 10),

('CHIP_SIG_011', '融券大增', 'Short Interest Surge', 'MARGIN',
 'MEDIUM',
 '融券餘額單日大幅增加',
 '{"type": "change_rate", "field": "short_change", "operator": ">=", "rate": 0.10}'::jsonb,
 '空頭勢力增強，但也可能形成軋空動能，需觀察後續',
 true, 11),

('CHIP_SIG_012', '券資比過高', 'High Short Margin Ratio', 'MARGIN',
 'HIGH',
 '券資比超過一定門檻',
 '{"type": "threshold", "field": "margin_short_ratio", "operator": ">=", "value": 30}'::jsonb,
 '空頭佔比過高，若股價反彈可能引發軋空行情',
 true, 12),

('CHIP_SIG_013', '融資使用率過高', 'High Margin Usage Rate', 'MARGIN',
 'HIGH',
 '融資使用率超過一定門檻',
 '{"type": "threshold", "field": "margin_usage_rate", "operator": ">=", "value": 70}'::jsonb,
 '融資水位過高，若股價下跌可能引發連環斷頭',
 true, 13),

('CHIP_SIG_017', '融資連續減少', 'Margin Consecutive Decrease', 'MARGIN',
 'MEDIUM',
 '融資餘額連續多日減少',
 '{"type": "consecutive", "field": "margin_change", "operator": "<", "value": 0, "days": 5}'::jsonb,
 '散戶持續出場，籌碼逐漸沈澱，有利後續上漲',
 true, 17),

('CHIP_SIG_018', '融券回補潮', 'Short Covering Wave', 'MARGIN',
 'MEDIUM',
 '融券餘額連續大幅減少',
 '{"type": "consecutive_change", "field": "short_balance", "operator": "<", "rate": -0.05, "days": 3}'::jsonb,
 '空頭大量回補，可能引發軋空行情',
 true, 18),

-- ============================================================
-- CONCENTRATION SIGNALS (籌碼集中度信號)
-- ============================================================
('CHIP_SIG_014', '籌碼分歧', 'Chip Divergence', 'COMPOSITE',
 'MEDIUM',
 '法人買賣方向不一致',
 '{"type": "divergence", "fields": ["foreign_net", "trust_net"], "threshold": 0}'::jsonb,
 '法人看法分歧，觀望為宜，等待方向明朗',
 true, 14),

('CHIP_SIG_019', '籌碼高度集中', 'High Chip Concentration', 'CONCENTRATION',
 'MEDIUM',
 '法人持股比例大幅提高',
 '{"type": "threshold", "field": "institutional_ratio", "operator": ">=", "value": 70}'::jsonb,
 '籌碼高度集中於法人，股價波動可能較大',
 true, 19),

('CHIP_SIG_020', '籌碼快速集中', 'Rapid Chip Concentration', 'CONCENTRATION',
 'HIGH',
 '法人持股比例短期內快速提高',
 '{"type": "change_rate", "field": "institutional_ratio", "operator": ">=", "rate": 5, "days": 20}'::jsonb,
 '法人短期大量買進，積極布局，後勢看好',
 true, 20),

('CHIP_SIG_021', '籌碼快速分散', 'Rapid Chip Dispersion', 'CONCENTRATION',
 'HIGH',
 '法人持股比例短期內快速降低',
 '{"type": "change_rate", "field": "institutional_ratio", "operator": "<=", "rate": -5, "days": 20}'::jsonb,
 '法人短期大量出貨，籌碼轉弱，需提高警覺',
 true, 21),

-- ============================================================
-- COMPOSITE SIGNALS (綜合信號)
-- ============================================================
('CHIP_SIG_022', '籌碼面強勢', 'Strong Chip Momentum', 'COMPOSITE',
 'MEDIUM',
 '多項籌碼指標同時轉強',
 '{"type": "score", "field": "chip_score", "operator": ">=", "value": 80}'::jsonb,
 '籌碼面整體強勢，有利股價表現',
 true, 22),

('CHIP_SIG_023', '籌碼面弱勢', 'Weak Chip Momentum', 'COMPOSITE',
 'MEDIUM',
 '多項籌碼指標同時轉弱',
 '{"type": "score", "field": "chip_score", "operator": "<=", "value": 20}'::jsonb,
 '籌碼面整體弱勢，股價可能承壓',
 true, 23),

('CHIP_SIG_024', '法人散戶對作', 'Institutional vs Retail', 'COMPOSITE',
 'HIGH',
 '法人買進但融資同步增加',
 '{"type": "multi_condition", "conditions": [{"field": "total_net", "operator": ">", "value": 0}, {"field": "margin_change", "operator": ">", "value": 0}]}'::jsonb,
 '法人與散戶同步買進，短線熱絡但需留意後續分歧',
 true, 24)

ON CONFLICT (signal_code) DO UPDATE SET
    signal_name = EXCLUDED.signal_name,
    signal_name_en = EXCLUDED.signal_name_en,
    signal_type = EXCLUDED.signal_type,
    default_severity = EXCLUDED.default_severity,
    description = EXCLUDED.description,
    detection_rule = EXCLUDED.detection_rule,
    recommendation = EXCLUDED.recommendation,
    is_active = EXCLUDED.is_active,
    display_order = EXCLUDED.display_order,
    updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 2. chip_ranking_types - Ranking type definitions
-- ------------------------------------------------------------

INSERT INTO chip_ranking_types (
    rank_type, rank_name, rank_name_en, description,
    value_column, order_direction, default_limit, is_active
) VALUES
('FOREIGN_NET_BUY', '外資買超排行', 'Foreign Net Buy Ranking',
 '外資單日買超金額排行',
 'foreign_net', 'DESC', 50, true),

('FOREIGN_NET_SELL', '外資賣超排行', 'Foreign Net Sell Ranking',
 '外資單日賣超金額排行',
 'foreign_net', 'ASC', 50, true),

('FOREIGN_CONTINUOUS_BUY', '外資連續買超排行', 'Foreign Consecutive Buy Ranking',
 '外資連續買超天數排行',
 'foreign_continuous_days', 'DESC', 50, true),

('TRUST_NET_BUY', '投信買超排行', 'Trust Net Buy Ranking',
 '投信單日買超金額排行',
 'trust_net', 'DESC', 50, true),

('TRUST_CONTINUOUS_BUY', '投信連續買超排行', 'Trust Consecutive Buy Ranking',
 '投信連續買超天數排行',
 'trust_continuous_days', 'DESC', 50, true),

('TOTAL_NET_BUY', '三大法人買超排行', 'Total Institutional Buy Ranking',
 '三大法人合計買超金額排行',
 'total_net', 'DESC', 50, true),

('MARGIN_DECREASE', '融資減少排行', 'Margin Decrease Ranking',
 '融資餘額減少幅度排行',
 'margin_change', 'ASC', 50, true),

('SHORT_INCREASE', '融券增加排行', 'Short Interest Increase Ranking',
 '融券餘額增加幅度排行',
 'short_change', 'DESC', 50, true),

('HIGH_SHORT_RATIO', '高券資比排行', 'High Short Ratio Ranking',
 '券資比由高至低排行',
 'margin_short_ratio', 'DESC', 50, true),

('CHIP_SCORE_HIGH', '籌碼評分排行', 'Chip Score Ranking',
 '籌碼綜合評分由高至低排行',
 'chip_score', 'DESC', 50, true),

('FOREIGN_ACCUMULATED', '外資累計買超排行', 'Foreign Accumulated Buy Ranking',
 '外資近20日累計買超排行',
 'foreign_accumulated_20d', 'DESC', 50, true)

ON CONFLICT (rank_type) DO UPDATE SET
    rank_name = EXCLUDED.rank_name,
    rank_name_en = EXCLUDED.rank_name_en,
    description = EXCLUDED.description,
    value_column = EXCLUDED.value_column,
    order_direction = EXCLUDED.order_direction,
    default_limit = EXCLUDED.default_limit,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- End of 030_m09.sql
-- ============================================================
