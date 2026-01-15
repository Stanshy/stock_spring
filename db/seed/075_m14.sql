-- ============================================================
-- FinShark Seed: 075_m14.sql
-- Module: M14 - Stock Screening Engine
-- Description: Screening condition definitions and templates
-- Idempotent: Yes (ON CONFLICT DO UPDATE)
-- ============================================================

-- ------------------------------------------------------------
-- 1. condition_definitions - Screening condition definitions
-- ------------------------------------------------------------

INSERT INTO condition_definitions (
    condition_code, condition_name, description, category, data_source,
    data_type, operators, allowed_values, value_range, unit, example, display_order, is_active
) VALUES

-- ============================================================
-- FUNDAMENTAL CONDITIONS (基本面條件)
-- ============================================================
('F_PE', '本益比', '股價 / 每股盈餘', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '倍',
 '{"operator": "BETWEEN", "values": [5, 15]}'::jsonb, 1, true),

('F_PB', '股價淨值比', '股價 / 每股淨值', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '倍',
 '{"operator": "<=", "values": [2]}'::jsonb, 2, true),

('F_ROE', '股東權益報酬率', '淨利 / 股東權益', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">=", "values": [15]}'::jsonb, 3, true),

('F_ROA', '資產報酬率', '淨利 / 總資產', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">=", "values": [8]}'::jsonb, 4, true),

('F_EPS', '每股盈餘', '淨利 / 在外流通股數', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '元',
 '{"operator": ">=", "values": [3]}'::jsonb, 5, true),

('F_DIVIDEND_YIELD', '股利率', '每股股利 / 股價', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">=", "values": [3]}'::jsonb, 6, true),

('F_REVENUE_GROWTH', '營收成長率', '營收年增率', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">=", "values": [10]}'::jsonb, 7, true),

('F_EPS_GROWTH', 'EPS成長率', '每股盈餘年增率', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">=", "values": [15]}'::jsonb, 8, true),

('F_GROSS_MARGIN', '毛利率', '毛利 / 營收', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">=", "values": [30]}'::jsonb, 9, true),

('F_OPERATING_MARGIN', '營業利益率', '營業利益 / 營收', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">=", "values": [15]}'::jsonb, 10, true),

('F_DEBT_RATIO', '負債比率', '總負債 / 總資產', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": "<=", "values": [50]}'::jsonb, 11, true),

('F_CURRENT_RATIO', '流動比率', '流動資產 / 流動負債', 'FUNDAMENTAL', 'M08', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '倍',
 '{"operator": ">=", "values": [1.5]}'::jsonb, 12, true),

-- ============================================================
-- TECHNICAL CONDITIONS (技術面條件)
-- ============================================================
('T_RSI', 'RSI 指標', '相對強弱指標（14 日）', 'TECHNICAL', 'M07', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, '[0, 100]'::jsonb, NULL,
 '{"operator": "BETWEEN", "values": [30, 70]}'::jsonb, 20, true),

('T_KD_K', 'KD 指標 K 值', 'KD 隨機指標 K 值', 'TECHNICAL', 'M07', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, '[0, 100]'::jsonb, NULL,
 '{"operator": ">=", "values": [20]}'::jsonb, 21, true),

('T_KD_D', 'KD 指標 D 值', 'KD 隨機指標 D 值', 'TECHNICAL', 'M07', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, '[0, 100]'::jsonb, NULL,
 '{"operator": ">=", "values": [20]}'::jsonb, 22, true),

('T_MACD', 'MACD', 'MACD 指標值', 'TECHNICAL', 'M07', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, NULL,
 '{"operator": ">", "values": [0]}'::jsonb, 23, true),

('T_MACD_SIGNAL', 'MACD 交叉', 'MACD 與信號線交叉狀態', 'TECHNICAL', 'M07', 'ENUM',
 '["="]'::jsonb, '["GOLDEN_CROSS", "DEATH_CROSS", "ABOVE", "BELOW"]'::jsonb, NULL, NULL,
 '{"operator": "=", "values": ["GOLDEN_CROSS"]}'::jsonb, 24, true),

('T_MA5', '5日均線位置', '股價相對5日均線位置', 'TECHNICAL', 'M07', 'ENUM',
 '["="]'::jsonb, '["ABOVE", "BELOW", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb, NULL, NULL,
 '{"operator": "=", "values": ["ABOVE"]}'::jsonb, 25, true),

('T_MA20', '20日均線位置', '股價相對20日均線位置', 'TECHNICAL', 'M07', 'ENUM',
 '["="]'::jsonb, '["ABOVE", "BELOW", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb, NULL, NULL,
 '{"operator": "=", "values": ["CROSS_ABOVE"]}'::jsonb, 26, true),

('T_MA60', '60日均線位置', '股價相對60日均線位置', 'TECHNICAL', 'M07', 'ENUM',
 '["="]'::jsonb, '["ABOVE", "BELOW", "CROSS_ABOVE", "CROSS_BELOW"]'::jsonb, NULL, NULL,
 '{"operator": "=", "values": ["ABOVE"]}'::jsonb, 27, true),

('T_MA_ALIGNMENT', '均線排列', '多空均線排列狀態', 'TECHNICAL', 'M07', 'ENUM',
 '["="]'::jsonb, '["BULLISH", "BEARISH", "MIXED"]'::jsonb, NULL, NULL,
 '{"operator": "=", "values": ["BULLISH"]}'::jsonb, 28, true),

('T_VOLUME_RATIO', '成交量倍數', '當日成交量 / 平均成交量', 'TECHNICAL', 'M07', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '倍',
 '{"operator": ">", "values": [1.5]}'::jsonb, 29, true),

('T_BBANDS_POSITION', '布林通道位置', '股價在布林通道中的位置', 'TECHNICAL', 'M07', 'ENUM',
 '["="]'::jsonb, '["ABOVE_UPPER", "UPPER_ZONE", "MIDDLE_ZONE", "LOWER_ZONE", "BELOW_LOWER"]'::jsonb, NULL, NULL,
 '{"operator": "=", "values": ["LOWER_ZONE"]}'::jsonb, 30, true),

-- ============================================================
-- CHIP CONDITIONS (籌碼面條件)
-- ============================================================
('C_FOREIGN_NET', '外資買賣超', '外資淨買超張數', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '張',
 '{"operator": ">", "values": [0]}'::jsonb, 40, true),

('C_FOREIGN_CONT', '外資連續買賣', '外資連續買超天數', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">"]'::jsonb, NULL, NULL, '日',
 '{"operator": ">=", "values": [3]}'::jsonb, 41, true),

('C_TRUST_NET', '投信買賣超', '投信淨買超張數', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '張',
 '{"operator": ">", "values": [0]}'::jsonb, 42, true),

('C_TRUST_CONT', '投信連續買賣', '投信連續買超天數', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">"]'::jsonb, NULL, NULL, '日',
 '{"operator": ">=", "values": [3]}'::jsonb, 43, true),

('C_DEALER_NET', '自營商買賣超', '自營商淨買超張數', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '張',
 '{"operator": ">", "values": [0]}'::jsonb, 44, true),

('C_INST_TOTAL', '三大法人合計', '三大法人合計買賣超', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '張',
 '{"operator": ">", "values": [0]}'::jsonb, 45, true),

('C_MARGIN_CHANGE', '融資增減', '融資餘額增減張數', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '張',
 '{"operator": "<", "values": [0]}'::jsonb, 46, true),

('C_SHORT_RATIO', '券資比', '融券餘額 / 融資餘額', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">=", "values": [20]}'::jsonb, 47, true),

('C_CONCENTRATION', '籌碼集中度', '籌碼集中度變化', 'CHIP', 'M09', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '%',
 '{"operator": ">", "values": [0]}'::jsonb, 48, true),

-- ============================================================
-- SIGNAL CONDITIONS (信號條件)
-- ============================================================
('S_DIRECTION', '信號方向', 'M13 統一信號方向', 'SIGNAL', 'M13', 'ENUM',
 '["="]'::jsonb, '["BUY", "SELL", "HOLD"]'::jsonb, NULL, NULL,
 '{"operator": "=", "values": ["BUY"]}'::jsonb, 60, true),

('S_GRADE', '信號評級', 'M13 統一信號評級', 'SIGNAL', 'M13', 'ENUM',
 '["=", ">="]'::jsonb, '["A+", "A", "B+", "B", "C", "D"]'::jsonb, NULL, NULL,
 '{"operator": ">=", "values": ["B+"]}'::jsonb, 61, true),

('S_SCORE', '信號評分', 'M13 統一信號評分', 'SIGNAL', 'M13', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, '[0, 100]'::jsonb, '分',
 '{"operator": ">=", "values": [70]}'::jsonb, 62, true),

-- ============================================================
-- ATTRIBUTE CONDITIONS (屬性條件)
-- ============================================================
('A_PRICE', '股價', '當前股價', 'ATTRIBUTE', 'M06', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '元',
 '{"operator": "BETWEEN", "values": [50, 500]}'::jsonb, 80, true),

('A_MARKET', '市場別', '上市或上櫃', 'ATTRIBUTE', 'M06', 'ENUM',
 '["=", "IN"]'::jsonb, '["TWSE", "OTC"]'::jsonb, NULL, NULL,
 '{"operator": "=", "values": ["TWSE"]}'::jsonb, 81, true),

('A_SECTOR', '產業別', '產業分類', 'ATTRIBUTE', 'M06', 'ENUM',
 '["=", "IN"]'::jsonb, NULL, NULL, NULL,
 '{"operator": "IN", "values": ["半導體", "電子零組件"]}'::jsonb, 82, true),

('A_MARKET_CAP', '市值', '股票市值', 'ATTRIBUTE', 'M06', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '億',
 '{"operator": ">=", "values": [100]}'::jsonb, 83, true),

('A_VOLUME', '成交量', '當日成交量', 'ATTRIBUTE', 'M06', 'NUMBER',
 '["<", "<=", "=", ">=", ">", "BETWEEN"]'::jsonb, NULL, NULL, '張',
 '{"operator": ">=", "values": [1000]}'::jsonb, 84, true)

ON CONFLICT (condition_code) DO UPDATE SET
    condition_name = EXCLUDED.condition_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    data_source = EXCLUDED.data_source,
    data_type = EXCLUDED.data_type,
    operators = EXCLUDED.operators,
    allowed_values = EXCLUDED.allowed_values,
    value_range = EXCLUDED.value_range,
    unit = EXCLUDED.unit,
    example = EXCLUDED.example,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 2. screening_templates - Quick screening templates
-- ------------------------------------------------------------

INSERT INTO screening_templates (
    template_code, template_name, description, category,
    conditions, condition_summary, sort_by, sort_direction, default_limit, display_order, is_active
) VALUES

('QUICK_01', '強勢股', 'M13 高評級買入信號股票', '信號',
 '{"logic":"AND","conditions":[{"condition_code":"S_GRADE","operator":">=","values":["A"]},{"condition_code":"S_DIRECTION","operator":"=","values":["BUY"]}]}'::jsonb,
 '信號評級 >= A, 方向 = BUY',
 '["signal_score"]'::jsonb, '["DESC"]'::jsonb, 50, 1, true),

('QUICK_02', '外資連買', '外資連續買超的股票', '籌碼',
 '{"logic":"AND","conditions":[{"condition_code":"C_FOREIGN_CONT","operator":">=","values":[3]}]}'::jsonb,
 '外資連續買超 >= 3 日',
 '["foreign_cont_days"]'::jsonb, '["DESC"]'::jsonb, 50, 2, true),

('QUICK_03', '低估價值股', '低估值高殖利率股票', '基本面',
 '{"logic":"AND","conditions":[{"condition_code":"F_PE","operator":"<","values":[15]},{"condition_code":"F_ROE","operator":">","values":[15]},{"condition_code":"F_DIVIDEND_YIELD","operator":">","values":[3]}]}'::jsonb,
 'PE < 15, ROE > 15%, 股利率 > 3%',
 '["dividend_yield"]'::jsonb, '["DESC"]'::jsonb, 50, 3, true),

('QUICK_04', '技術突破', '突破均線且量增', '技術面',
 '{"logic":"AND","conditions":[{"condition_code":"T_MA20","operator":"=","values":["CROSS_ABOVE"]},{"condition_code":"T_VOLUME_RATIO","operator":">","values":[1.5]}]}'::jsonb,
 '突破 20 日均線, 成交量放大 > 50%',
 '["volume_ratio"]'::jsonb, '["DESC"]'::jsonb, 50, 4, true),

('QUICK_05', '動能選股', '動能指標轉強', '技術面',
 '{"logic":"AND","conditions":[{"condition_code":"T_RSI","operator":">","values":[50]},{"condition_code":"T_MACD_SIGNAL","operator":"=","values":["GOLDEN_CROSS"]}]}'::jsonb,
 'RSI > 50, MACD 黃金交叉',
 '["rsi"]'::jsonb, '["DESC"]'::jsonb, 50, 5, true),

('QUICK_06', '績優成長', '營收與獲利成長股', '基本面',
 '{"logic":"AND","conditions":[{"condition_code":"F_REVENUE_GROWTH","operator":">","values":[10]},{"condition_code":"F_EPS_GROWTH","operator":">","values":[15]}]}'::jsonb,
 '營收 YoY > 10%, EPS 成長 > 15%',
 '["eps_growth"]'::jsonb, '["DESC"]'::jsonb, 50, 6, true),

('QUICK_07', '籌碼集中', '主力連續買超', '籌碼',
 '{"logic":"AND","conditions":[{"condition_code":"C_INST_TOTAL","operator":">","values":[0]},{"condition_code":"C_CONCENTRATION","operator":">","values":[0]}]}'::jsonb,
 '法人買超, 籌碼集中度增加',
 '["inst_total"]'::jsonb, '["DESC"]'::jsonb, 50, 7, true),

('QUICK_08', '多頭排列', '均線多頭排列', '技術面',
 '{"logic":"AND","conditions":[{"condition_code":"T_MA_ALIGNMENT","operator":"=","values":["BULLISH"]}]}'::jsonb,
 'MA5 > MA10 > MA20 > MA60',
 '["price_change_pct"]'::jsonb, '["DESC"]'::jsonb, 50, 8, true),

('QUICK_09', '投信認養', '投信連續買超個股', '籌碼',
 '{"logic":"AND","conditions":[{"condition_code":"C_TRUST_CONT","operator":">=","values":[3]},{"condition_code":"C_TRUST_NET","operator":">","values":[100]}]}'::jsonb,
 '投信連續買超 >= 3 日, 單日買超 > 100 張',
 '["trust_cont_days"]'::jsonb, '["DESC"]'::jsonb, 50, 9, true),

('QUICK_10', '高殖利率', '高股息殖利率選股', '基本面',
 '{"logic":"AND","conditions":[{"condition_code":"F_DIVIDEND_YIELD","operator":">=","values":[5]},{"condition_code":"F_EPS","operator":">","values":[0]}]}'::jsonb,
 '殖利率 >= 5%, EPS > 0',
 '["dividend_yield"]'::jsonb, '["DESC"]'::jsonb, 50, 10, true),

('QUICK_11', 'RSI超賣', 'RSI低檔反彈機會', '技術面',
 '{"logic":"AND","conditions":[{"condition_code":"T_RSI","operator":"<","values":[30]},{"condition_code":"A_VOLUME","operator":">=","values":[500]}]}'::jsonb,
 'RSI < 30 (超賣), 成交量 >= 500 張',
 '["rsi"]'::jsonb, '["ASC"]'::jsonb, 50, 11, true),

('QUICK_12', '軋空題材', '高券資比股票', '籌碼',
 '{"logic":"AND","conditions":[{"condition_code":"C_SHORT_RATIO","operator":">=","values":[30]},{"condition_code":"C_FOREIGN_NET","operator":">","values":[0]}]}'::jsonb,
 '券資比 >= 30%, 外資買超',
 '["short_ratio"]'::jsonb, '["DESC"]'::jsonb, 50, 12, true),

('QUICK_13', '財務穩健', '低負債高流動性', '基本面',
 '{"logic":"AND","conditions":[{"condition_code":"F_DEBT_RATIO","operator":"<","values":[40]},{"condition_code":"F_CURRENT_RATIO","operator":">=","values":[1.5]},{"condition_code":"F_ROE","operator":">=","values":[10]}]}'::jsonb,
 '負債比 < 40%, 流動比 >= 1.5, ROE >= 10%',
 '["roe"]'::jsonb, '["DESC"]'::jsonb, 50, 13, true),

('QUICK_14', '法人同買', '三大法人同步買超', '籌碼',
 '{"logic":"AND","conditions":[{"condition_code":"C_FOREIGN_NET","operator":">","values":[0]},{"condition_code":"C_TRUST_NET","operator":">","values":[0]},{"condition_code":"C_DEALER_NET","operator":">","values":[0]}]}'::jsonb,
 '外資、投信、自營商同步買超',
 '["inst_total"]'::jsonb, '["DESC"]'::jsonb, 50, 14, true),

('QUICK_15', '綜合強勢', '技術面與籌碼面雙強', '綜合',
 '{"logic":"AND","conditions":[{"condition_code":"T_MA_ALIGNMENT","operator":"=","values":["BULLISH"]},{"condition_code":"C_INST_TOTAL","operator":">","values":[0]},{"condition_code":"S_DIRECTION","operator":"=","values":["BUY"]}]}'::jsonb,
 '多頭排列 + 法人買超 + 買入信號',
 '["signal_score"]'::jsonb, '["DESC"]'::jsonb, 50, 15, true)

ON CONFLICT (template_code) DO UPDATE SET
    template_name = EXCLUDED.template_name,
    description = EXCLUDED.description,
    category = EXCLUDED.category,
    conditions = EXCLUDED.conditions,
    condition_summary = EXCLUDED.condition_summary,
    sort_by = EXCLUDED.sort_by,
    sort_direction = EXCLUDED.sort_direction,
    default_limit = EXCLUDED.default_limit,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- End of 075_m14.sql
-- ============================================================
