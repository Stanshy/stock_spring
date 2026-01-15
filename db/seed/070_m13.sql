-- ============================================================
-- FinShark Seed: 070_m13.sql
-- Module: M13 - Signal Judgment Engine
-- Description: Signal semantic groups for deduplication
-- Idempotent: Yes (ON CONFLICT DO UPDATE)
-- ============================================================

-- ------------------------------------------------------------
-- 1. signal_semantic_groups - Semantic signal group definitions
--    Used for identifying and merging semantically similar signals
-- ------------------------------------------------------------

INSERT INTO signal_semantic_groups (
    group_code, group_name, signal_codes,
    merge_strategy, description, is_active
) VALUES

-- ============================================================
-- Oversold/Overbought Groups (Technical Indicators)
-- ============================================================
('OVERSOLD_GROUP', '超賣指標群組',
 'RSI_OVERSOLD,KD_OVERSOLD,WILLIAMS_OVERSOLD,STOCH_OVERSOLD,MFI_OVERSOLD',
 'HIGHEST_CONFIDENCE',
 '多個超賣指標（RSI<30, KD<20, Williams%R<-80 等）視為同一信號，避免重複計算',
 true),

('OVERBOUGHT_GROUP', '超買指標群組',
 'RSI_OVERBOUGHT,KD_OVERBOUGHT,WILLIAMS_OVERBOUGHT,STOCH_OVERBOUGHT,MFI_OVERBOUGHT',
 'HIGHEST_CONFIDENCE',
 '多個超買指標（RSI>70, KD>80, Williams%R>-20 等）視為同一信號，避免重複計算',
 true),

-- ============================================================
-- Cross Signal Groups (Technical Indicators)
-- ============================================================
('GOLDEN_CROSS_GROUP', '黃金交叉群組',
 'MACD_GOLDEN_CROSS,MA_GOLDEN_CROSS,KD_GOLDEN_CROSS,EMA_GOLDEN_CROSS',
 'HIGHEST_CONFIDENCE',
 '多種黃金交叉（MACD/均線/KD）視為同一多頭信號',
 true),

('DEATH_CROSS_GROUP', '死亡交叉群組',
 'MACD_DEATH_CROSS,MA_DEATH_CROSS,KD_DEATH_CROSS,EMA_DEATH_CROSS',
 'HIGHEST_CONFIDENCE',
 '多種死亡交叉（MACD/均線/KD）視為同一空頭信號',
 true),

-- ============================================================
-- Institutional Chip Groups
-- ============================================================
('INSTITUTIONAL_BUY_GROUP', '法人買超群組',
 'FOREIGN_BUY,TRUST_BUY,DEALER_BUY,TOTAL_INSTITUTIONAL_BUY,FOREIGN_CONTINUOUS_BUY,TRUST_CONTINUOUS_BUY',
 'AVERAGE',
 '外資/投信/自營商買超信號合併，採平均信心度',
 true),

('INSTITUTIONAL_SELL_GROUP', '法人賣超群組',
 'FOREIGN_SELL,TRUST_SELL,DEALER_SELL,TOTAL_INSTITUTIONAL_SELL,FOREIGN_CONTINUOUS_SELL,TRUST_CONTINUOUS_SELL',
 'AVERAGE',
 '外資/投信/自營商賣超信號合併，採平均信心度',
 true),

-- ============================================================
-- Margin Trading Groups
-- ============================================================
('MARGIN_BULLISH_GROUP', '融資多頭群組',
 'MARGIN_DECREASE,SHORT_INCREASE,SHORT_RATIO_HIGH,MARGIN_USAGE_LOW',
 'HIGHEST_CONFIDENCE',
 '融資減少、融券增加等多頭籌碼信號合併',
 true),

('MARGIN_BEARISH_GROUP', '融資空頭群組',
 'MARGIN_INCREASE,SHORT_DECREASE,MARGIN_USAGE_HIGH',
 'HIGHEST_CONFIDENCE',
 '融資增加、融券減少等空頭籌碼信號合併',
 true),

-- ============================================================
-- Bollinger Band Groups
-- ============================================================
('BBANDS_OVERSOLD_GROUP', '布林通道超賣群組',
 'BBANDS_LOWER_TOUCH,BBANDS_LOWER_BREAK,BBANDS_PERCENT_B_LOW',
 'HIGHEST_CONFIDENCE',
 '布林通道下緣相關的超賣信號合併',
 true),

('BBANDS_OVERBOUGHT_GROUP', '布林通道超買群組',
 'BBANDS_UPPER_TOUCH,BBANDS_UPPER_BREAK,BBANDS_PERCENT_B_HIGH',
 'HIGHEST_CONFIDENCE',
 '布林通道上緣相關的超買信號合併',
 true),

-- ============================================================
-- Volume Confirmation Groups
-- ============================================================
('VOLUME_BULLISH_GROUP', '量能多頭群組',
 'VOLUME_BREAKOUT,VOLUME_SURGE_UP,OBV_BULLISH,MFI_BULLISH',
 'HIGHEST_CONFIDENCE',
 '量能放大配合上漲的多頭信號合併',
 true),

('VOLUME_BEARISH_GROUP', '量能空頭群組',
 'VOLUME_SURGE_DOWN,OBV_BEARISH,MFI_BEARISH,VOLUME_DIVERGENCE_BEAR',
 'HIGHEST_CONFIDENCE',
 '量能放大配合下跌的空頭信號合併',
 true),

-- ============================================================
-- Divergence Groups
-- ============================================================
('BULLISH_DIVERGENCE_GROUP', '多頭背離群組',
 'RSI_BULLISH_DIVERGENCE,MACD_BULLISH_DIVERGENCE,KD_BULLISH_DIVERGENCE,OBV_BULLISH_DIVERGENCE',
 'HIGHEST_CONFIDENCE',
 '多種指標的多頭背離信號合併（價格創新低但指標未創新低）',
 true),

('BEARISH_DIVERGENCE_GROUP', '空頭背離群組',
 'RSI_BEARISH_DIVERGENCE,MACD_BEARISH_DIVERGENCE,KD_BEARISH_DIVERGENCE,OBV_BEARISH_DIVERGENCE',
 'HIGHEST_CONFIDENCE',
 '多種指標的空頭背離信號合併（價格創新高但指標未創新高）',
 true),

-- ============================================================
-- Fundamental Valuation Groups
-- ============================================================
('UNDERVALUED_GROUP', '低估值群組',
 'PE_LOW,PB_LOW,PS_LOW,PCF_LOW,PEG_LOW',
 'AVERAGE',
 '多種低估值指標（低本益比、低股價淨值比等）合併，採平均',
 true),

('OVERVALUED_GROUP', '高估值群組',
 'PE_HIGH,PB_HIGH,PS_HIGH,PCF_HIGH,PEG_HIGH',
 'AVERAGE',
 '多種高估值指標合併，採平均',
 true),

-- ============================================================
-- Profitability Groups
-- ============================================================
('PROFITABILITY_IMPROVE_GROUP', '獲利改善群組',
 'ROE_IMPROVE,ROA_IMPROVE,MARGIN_IMPROVE,EPS_GROWTH,REVENUE_GROWTH',
 'AVERAGE',
 '多種獲利改善信號合併（ROE/ROA/毛利率提升等）',
 true),

('PROFITABILITY_DECLINE_GROUP', '獲利衰退群組',
 'ROE_DECLINE,ROA_DECLINE,MARGIN_DECLINE,EPS_DECLINE,REVENUE_DECLINE',
 'AVERAGE',
 '多種獲利衰退信號合併',
 true)

ON CONFLICT (group_code) DO UPDATE SET
    group_name = EXCLUDED.group_name,
    signal_codes = EXCLUDED.signal_codes,
    merge_strategy = EXCLUDED.merge_strategy,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- End of 070_m13.sql
-- ============================================================
