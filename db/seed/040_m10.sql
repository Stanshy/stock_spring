-- ============================================================
-- FinShark Seed: 040_m10.sql
-- Module: M10 - Technical Pattern Recognition
-- Description: K-line patterns, chart patterns, and signal definitions
-- Idempotent: Yes (ON CONFLICT DO UPDATE)
-- ============================================================

-- ------------------------------------------------------------
-- 1. kline_pattern_definitions - K-line candlestick pattern definitions
-- ------------------------------------------------------------

INSERT INTO kline_pattern_definitions (
    pattern_id, pattern_name, english_name, pattern_category,
    candle_count, signal_type, typical_strength, description,
    detection_rules, reliability_notes, is_active, display_order
) VALUES

-- ============================================================
-- SINGLE CANDLESTICK PATTERNS (單一K線型態)
-- ============================================================
('KLINE_001', '十字線', 'Doji', 'SINGLE_KLINE',
 1, 'NEUTRAL_REVERSAL', 50,
 '開盤價與收盤價幾乎相等，顯示多空勢均力敵',
 '{"body_ratio_max": 0.1, "shadow_exists": true}'::jsonb,
 '趨勢末端出現可靠度較高，需配合其他指標確認',
 true, 1),

('KLINE_002', '長腳十字', 'Long-Legged Doji', 'SINGLE_KLINE',
 1, 'NEUTRAL_REVERSAL', 55,
 '上下影線都很長的十字線，顯示日內波動劇烈',
 '{"body_ratio_max": 0.1, "shadow_ratio_min": 2.0}'::jsonb,
 '在趨勢頂部或底部出現時，反轉信號較強',
 true, 2),

('KLINE_003', '蜻蜓十字', 'Dragonfly Doji', 'SINGLE_KLINE',
 1, 'BULLISH_REVERSAL', 60,
 '開高走低後拉回收平，下影線很長',
 '{"body_ratio_max": 0.1, "lower_shadow_ratio_min": 2.0, "upper_shadow_max": 0.1}'::jsonb,
 '在下跌趨勢底部出現時為看漲信號',
 true, 3),

('KLINE_004', '墓碑十字', 'Gravestone Doji', 'SINGLE_KLINE',
 1, 'BEARISH_REVERSAL', 60,
 '開低走高後拉回收平，上影線很長',
 '{"body_ratio_max": 0.1, "upper_shadow_ratio_min": 2.0, "lower_shadow_max": 0.1}'::jsonb,
 '在上漲趨勢頂部出現時為看跌信號',
 true, 4),

('KLINE_005', '錘子', 'Hammer', 'SINGLE_KLINE',
 1, 'BULLISH_REVERSAL', 65,
 '小實體在上方，長下影線，下跌後出現',
 '{"body_ratio_max": 0.3, "lower_shadow_ratio_min": 2.0, "trend": "DOWNTREND"}'::jsonb,
 '需出現在下跌趨勢中才有效，次日確認更佳',
 true, 5),

('KLINE_006', '倒錘子', 'Inverted Hammer', 'SINGLE_KLINE',
 1, 'BULLISH_REVERSAL', 55,
 '小實體在下方，長上影線，下跌後出現',
 '{"body_ratio_max": 0.3, "upper_shadow_ratio_min": 2.0, "trend": "DOWNTREND"}'::jsonb,
 '可靠度較錘子低，需次日陽線確認',
 true, 6),

('KLINE_007', '吊人', 'Hanging Man', 'SINGLE_KLINE',
 1, 'BEARISH_REVERSAL', 60,
 '小實體在上方，長下影線，上漲後出現',
 '{"body_ratio_max": 0.3, "lower_shadow_ratio_min": 2.0, "trend": "UPTREND"}'::jsonb,
 '需出現在上漲趨勢中才有效，次日確認更佳',
 true, 7),

('KLINE_008', '射擊之星', 'Shooting Star', 'SINGLE_KLINE',
 1, 'BEARISH_REVERSAL', 65,
 '小實體在下方，長上影線，上漲後出現',
 '{"body_ratio_max": 0.3, "upper_shadow_ratio_min": 2.0, "trend": "UPTREND"}'::jsonb,
 '強勁的頂部反轉信號，上影線越長越可靠',
 true, 8),

('KLINE_009', '大陽線', 'Bullish Marubozu', 'SINGLE_KLINE',
 1, 'BULLISH_CONTINUATION', 70,
 '長實體陽線，幾乎無上下影線',
 '{"body_ratio_min": 0.8, "is_bullish": true}'::jsonb,
 '強勁多頭信號，後續易續漲',
 true, 9),

('KLINE_010', '大陰線', 'Bearish Marubozu', 'SINGLE_KLINE',
 1, 'BEARISH_CONTINUATION', 70,
 '長實體陰線，幾乎無上下影線',
 '{"body_ratio_min": 0.8, "is_bullish": false}'::jsonb,
 '強勁空頭信號，後續易續跌',
 true, 10),

('KLINE_011', '紡錘線', 'Spinning Top', 'SINGLE_KLINE',
 1, 'NEUTRAL', 40,
 '小實體配上下影線，顯示猶豫不決',
 '{"body_ratio_max": 0.3, "shadow_exists": true}'::jsonb,
 '趨勢末端出現可能為反轉前兆',
 true, 11),

-- ============================================================
-- DOUBLE CANDLESTICK PATTERNS (雙K線型態)
-- ============================================================
('KLINE_020', '看漲吞噬', 'Bullish Engulfing', 'DOUBLE_KLINE',
 2, 'BULLISH_REVERSAL', 75,
 '陽線完全吞噬前一根陰線',
 '{"day1": {"is_bullish": false}, "day2": {"is_bullish": true, "engulfs": true}}'::jsonb,
 '下跌趨勢中出現時可靠度高，成交量放大更佳',
 true, 20),

('KLINE_021', '看跌吞噬', 'Bearish Engulfing', 'DOUBLE_KLINE',
 2, 'BEARISH_REVERSAL', 75,
 '陰線完全吞噬前一根陽線',
 '{"day1": {"is_bullish": true}, "day2": {"is_bullish": false, "engulfs": true}}'::jsonb,
 '上漲趨勢中出現時可靠度高，成交量放大更佳',
 true, 21),

('KLINE_022', '貫穿線', 'Piercing Line', 'DOUBLE_KLINE',
 2, 'BULLISH_REVERSAL', 65,
 '陽線開低後上漲超過前陰線實體一半',
 '{"day1": {"is_bullish": false}, "day2": {"is_bullish": true, "pierce_ratio_min": 0.5}}'::jsonb,
 '下跌趨勢中出現為看漲信號',
 true, 22),

('KLINE_023', '烏雲蓋頂', 'Dark Cloud Cover', 'DOUBLE_KLINE',
 2, 'BEARISH_REVERSAL', 65,
 '陰線開高後下跌超過前陽線實體一半',
 '{"day1": {"is_bullish": true}, "day2": {"is_bullish": false, "pierce_ratio_min": 0.5}}'::jsonb,
 '上漲趨勢中出現為看跌信號',
 true, 23),

('KLINE_024', '平頭頂部', 'Tweezer Top', 'DOUBLE_KLINE',
 2, 'BEARISH_REVERSAL', 55,
 '連續兩日最高價幾乎相同',
 '{"high_diff_max": 0.002}'::jsonb,
 '上漲趨勢中出現，顯示上檔壓力',
 true, 24),

('KLINE_025', '平頭底部', 'Tweezer Bottom', 'DOUBLE_KLINE',
 2, 'BULLISH_REVERSAL', 55,
 '連續兩日最低價幾乎相同',
 '{"low_diff_max": 0.002}'::jsonb,
 '下跌趨勢中出現，顯示下檔支撐',
 true, 25),

('KLINE_026', '母子線(看漲)', 'Bullish Harami', 'DOUBLE_KLINE',
 2, 'BULLISH_REVERSAL', 55,
 '小陽線被前一根大陰線包含',
 '{"day1": {"is_bullish": false, "body_min": 0.5}, "day2": {"is_bullish": true, "inside": true}}'::jsonb,
 '下跌趨勢中出現，需次日確認',
 true, 26),

('KLINE_027', '母子線(看跌)', 'Bearish Harami', 'DOUBLE_KLINE',
 2, 'BEARISH_REVERSAL', 55,
 '小陰線被前一根大陽線包含',
 '{"day1": {"is_bullish": true, "body_min": 0.5}, "day2": {"is_bullish": false, "inside": true}}'::jsonb,
 '上漲趨勢中出現，需次日確認',
 true, 27),

('KLINE_028', '十字母子(看漲)', 'Bullish Harami Cross', 'DOUBLE_KLINE',
 2, 'BULLISH_REVERSAL', 60,
 '十字線被前一根大陰線包含',
 '{"day1": {"is_bullish": false, "body_min": 0.5}, "day2": {"is_doji": true, "inside": true}}'::jsonb,
 '比一般母子線更強的反轉信號',
 true, 28),

('KLINE_029', '十字母子(看跌)', 'Bearish Harami Cross', 'DOUBLE_KLINE',
 2, 'BEARISH_REVERSAL', 60,
 '十字線被前一根大陽線包含',
 '{"day1": {"is_bullish": true, "body_min": 0.5}, "day2": {"is_doji": true, "inside": true}}'::jsonb,
 '比一般母子線更強的反轉信號',
 true, 29),

-- ============================================================
-- TRIPLE CANDLESTICK PATTERNS (三K線型態)
-- ============================================================
('KLINE_040', '晨星', 'Morning Star', 'TRIPLE_KLINE',
 3, 'BULLISH_REVERSAL', 80,
 '大陰線 + 小實體/十字 + 大陽線',
 '{"day1": {"is_bullish": false, "body_min": 0.5}, "day2": {"body_max": 0.3}, "day3": {"is_bullish": true, "body_min": 0.5}}'::jsonb,
 '經典底部反轉型態，可靠度高',
 true, 40),

('KLINE_041', '夜星', 'Evening Star', 'TRIPLE_KLINE',
 3, 'BEARISH_REVERSAL', 80,
 '大陽線 + 小實體/十字 + 大陰線',
 '{"day1": {"is_bullish": true, "body_min": 0.5}, "day2": {"body_max": 0.3}, "day3": {"is_bullish": false, "body_min": 0.5}}'::jsonb,
 '經典頂部反轉型態，可靠度高',
 true, 41),

('KLINE_042', '十字晨星', 'Morning Doji Star', 'TRIPLE_KLINE',
 3, 'BULLISH_REVERSAL', 85,
 '大陰線 + 十字線 + 大陽線',
 '{"day1": {"is_bullish": false, "body_min": 0.5}, "day2": {"is_doji": true}, "day3": {"is_bullish": true, "body_min": 0.5}}'::jsonb,
 '比晨星更強的反轉信號',
 true, 42),

('KLINE_043', '十字夜星', 'Evening Doji Star', 'TRIPLE_KLINE',
 3, 'BEARISH_REVERSAL', 85,
 '大陽線 + 十字線 + 大陰線',
 '{"day1": {"is_bullish": true, "body_min": 0.5}, "day2": {"is_doji": true}, "day3": {"is_bullish": false, "body_min": 0.5}}'::jsonb,
 '比夜星更強的反轉信號',
 true, 43),

('KLINE_044', '三白兵', 'Three White Soldiers', 'TRIPLE_KLINE',
 3, 'BULLISH_CONTINUATION', 75,
 '連續三根長陽線，每根開在前一根實體內並創新高',
 '{"consecutive_bullish": 3, "each_higher_high": true, "body_min": 0.5}'::jsonb,
 '強勁多頭信號，但需注意是否過度拉升',
 true, 44),

('KLINE_045', '三黑鴉', 'Three Black Crows', 'TRIPLE_KLINE',
 3, 'BEARISH_CONTINUATION', 75,
 '連續三根長陰線，每根開在前一根實體內並創新低',
 '{"consecutive_bearish": 3, "each_lower_low": true, "body_min": 0.5}'::jsonb,
 '強勁空頭信號，但需注意是否過度下跌',
 true, 45),

('KLINE_046', '上升三法', 'Rising Three Methods', 'MULTI_KLINE',
 5, 'BULLISH_CONTINUATION', 70,
 '大陽線 + 三根小陰線(不破大陽低點) + 大陽線創新高',
 '{"pattern": "continuation", "direction": "bullish"}'::jsonb,
 '強勢整理後續漲型態',
 true, 46),

('KLINE_047', '下降三法', 'Falling Three Methods', 'MULTI_KLINE',
 5, 'BEARISH_CONTINUATION', 70,
 '大陰線 + 三根小陽線(不破大陰高點) + 大陰線創新低',
 '{"pattern": "continuation", "direction": "bearish"}'::jsonb,
 '弱勢整理後續跌型態',
 true, 47),

('KLINE_048', '棄嬰(看漲)', 'Bullish Abandoned Baby', 'TRIPLE_KLINE',
 3, 'BULLISH_REVERSAL', 90,
 '大陰線 + 跳空十字線 + 跳空大陽線',
 '{"day1": {"is_bullish": false}, "day2": {"is_doji": true, "gap_down": true}, "day3": {"is_bullish": true, "gap_up": true}}'::jsonb,
 '極罕見但極可靠的底部反轉信號',
 true, 48),

('KLINE_049', '棄嬰(看跌)', 'Bearish Abandoned Baby', 'TRIPLE_KLINE',
 3, 'BEARISH_REVERSAL', 90,
 '大陽線 + 跳空十字線 + 跳空大陰線',
 '{"day1": {"is_bullish": true}, "day2": {"is_doji": true, "gap_up": true}, "day3": {"is_bullish": false, "gap_down": true}}'::jsonb,
 '極罕見但極可靠的頂部反轉信號',
 true, 49),

('KLINE_050', '三內升', 'Three Inside Up', 'TRIPLE_KLINE',
 3, 'BULLISH_REVERSAL', 70,
 '大陰線 + 母子陽線 + 創新高陽線',
 '{"pattern": "three_inside", "direction": "up"}'::jsonb,
 '確認母子線反轉的型態',
 true, 50),

('KLINE_051', '三內降', 'Three Inside Down', 'TRIPLE_KLINE',
 3, 'BEARISH_REVERSAL', 70,
 '大陽線 + 母子陰線 + 創新低陰線',
 '{"pattern": "three_inside", "direction": "down"}'::jsonb,
 '確認母子線反轉的型態',
 true, 51),

('KLINE_052', '三外升', 'Three Outside Up', 'TRIPLE_KLINE',
 3, 'BULLISH_REVERSAL', 75,
 '小陰線 + 吞噬陽線 + 創新高陽線',
 '{"pattern": "three_outside", "direction": "up"}'::jsonb,
 '確認吞噬反轉的型態',
 true, 52),

('KLINE_053', '三外降', 'Three Outside Down', 'TRIPLE_KLINE',
 3, 'BEARISH_REVERSAL', 75,
 '小陽線 + 吞噬陰線 + 創新低陰線',
 '{"pattern": "three_outside", "direction": "down"}'::jsonb,
 '確認吞噬反轉的型態',
 true, 53)

ON CONFLICT (pattern_id) DO UPDATE SET
    pattern_name = EXCLUDED.pattern_name,
    english_name = EXCLUDED.english_name,
    pattern_category = EXCLUDED.pattern_category,
    candle_count = EXCLUDED.candle_count,
    signal_type = EXCLUDED.signal_type,
    typical_strength = EXCLUDED.typical_strength,
    description = EXCLUDED.description,
    detection_rules = EXCLUDED.detection_rules,
    reliability_notes = EXCLUDED.reliability_notes,
    is_active = EXCLUDED.is_active,
    display_order = EXCLUDED.display_order,
    updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 2. chart_pattern_definitions - Chart pattern definitions
-- ------------------------------------------------------------

INSERT INTO chart_pattern_definitions (
    pattern_id, pattern_name, english_name, pattern_category,
    typical_duration, signal_type, typical_strength, description,
    key_characteristics, target_calculation, is_active, display_order
) VALUES

-- ============================================================
-- REVERSAL PATTERNS (反轉型態)
-- ============================================================
('CHART_001', '頭肩頂', 'Head and Shoulders Top', 'REVERSAL',
 '30-90', 'BEARISH_REVERSAL', 85,
 '由左肩、頭部、右肩組成的頂部反轉型態',
 '{"peaks": 3, "neckline": true, "volume_pattern": "decreasing_on_right_shoulder"}'::jsonb,
 '目標價 = 頸線 - (頭部高點 - 頸線)',
 true, 1),

('CHART_002', '頭肩底', 'Head and Shoulders Bottom', 'REVERSAL',
 '30-90', 'BULLISH_REVERSAL', 85,
 '由左肩、頭部、右肩組成的底部反轉型態（倒頭肩）',
 '{"troughs": 3, "neckline": true, "volume_pattern": "increasing_on_breakout"}'::jsonb,
 '目標價 = 頸線 + (頸線 - 頭部低點)',
 true, 2),

('CHART_003', '雙重頂', 'Double Top', 'REVERSAL',
 '20-60', 'BEARISH_REVERSAL', 80,
 '價格兩次觸及相近高點後回落',
 '{"peaks": 2, "peak_diff_max": 0.03, "trough_support": true}'::jsonb,
 '目標價 = 支撐線 - (高點 - 支撐線)',
 true, 3),

('CHART_004', '雙重底', 'Double Bottom', 'REVERSAL',
 '20-60', 'BULLISH_REVERSAL', 80,
 '價格兩次觸及相近低點後反彈（W底）',
 '{"troughs": 2, "trough_diff_max": 0.03, "peak_resistance": true}'::jsonb,
 '目標價 = 壓力線 + (壓力線 - 低點)',
 true, 4),

('CHART_005', '三重頂', 'Triple Top', 'REVERSAL',
 '40-100', 'BEARISH_REVERSAL', 85,
 '價格三次觸及相近高點後回落',
 '{"peaks": 3, "peak_diff_max": 0.03}'::jsonb,
 '目標價 = 支撐線 - (高點 - 支撐線)',
 true, 5),

('CHART_006', '三重底', 'Triple Bottom', 'REVERSAL',
 '40-100', 'BULLISH_REVERSAL', 85,
 '價格三次觸及相近低點後反彈',
 '{"troughs": 3, "trough_diff_max": 0.03}'::jsonb,
 '目標價 = 壓力線 + (壓力線 - 低點)',
 true, 6),

('CHART_007', '圓弧頂', 'Rounding Top', 'REVERSAL',
 '60-180', 'BEARISH_REVERSAL', 70,
 '價格緩慢形成弧形頂部',
 '{"shape": "arc", "direction": "down"}'::jsonb,
 '無明確目標價計算，需配合其他分析',
 true, 7),

('CHART_008', '圓弧底', 'Rounding Bottom', 'REVERSAL',
 '60-180', 'BULLISH_REVERSAL', 75,
 '價格緩慢形成弧形底部（碗型底）',
 '{"shape": "arc", "direction": "up"}'::jsonb,
 '無明確目標價計算，通常為長期多頭起點',
 true, 8),

('CHART_009', 'V型反轉', 'V-Reversal', 'REVERSAL',
 '5-20', 'BULLISH_REVERSAL', 60,
 '快速下跌後立即快速反彈',
 '{"shape": "V", "speed": "fast"}'::jsonb,
 '無明確目標，通常回到下跌前高點',
 true, 9),

('CHART_010', '倒V型反轉', 'Inverted V-Reversal', 'REVERSAL',
 '5-20', 'BEARISH_REVERSAL', 60,
 '快速上漲後立即快速下跌',
 '{"shape": "inverted_V", "speed": "fast"}'::jsonb,
 '無明確目標，通常回到上漲前低點',
 true, 10),

-- ============================================================
-- CONTINUATION PATTERNS (持續型態)
-- ============================================================
('CHART_020', '上升三角形', 'Ascending Triangle', 'CONTINUATION',
 '20-60', 'BULLISH_CONTINUATION', 75,
 '水平壓力線配上升支撐線',
 '{"resistance": "horizontal", "support": "ascending"}'::jsonb,
 '目標價 = 突破點 + 三角形高度',
 true, 20),

('CHART_021', '下降三角形', 'Descending Triangle', 'CONTINUATION',
 '20-60', 'BEARISH_CONTINUATION', 75,
 '水平支撐線配下降壓力線',
 '{"support": "horizontal", "resistance": "descending"}'::jsonb,
 '目標價 = 跌破點 - 三角形高度',
 true, 21),

('CHART_022', '對稱三角形', 'Symmetrical Triangle', 'BILATERAL',
 '20-60', 'NEUTRAL', 65,
 '收斂的支撐與壓力線，雙向突破可能',
 '{"support": "ascending", "resistance": "descending", "convergence": true}'::jsonb,
 '目標價 = 突破點 ± 三角形最寬處',
 true, 22),

('CHART_023', '上升旗形', 'Bull Flag', 'CONTINUATION',
 '5-20', 'BULLISH_CONTINUATION', 70,
 '急漲後形成向下傾斜的平行通道整理',
 '{"flagpole": true, "channel_direction": "down", "prior_trend": "up"}'::jsonb,
 '目標價 = 突破點 + 旗桿長度',
 true, 23),

('CHART_024', '下降旗形', 'Bear Flag', 'CONTINUATION',
 '5-20', 'BEARISH_CONTINUATION', 70,
 '急跌後形成向上傾斜的平行通道整理',
 '{"flagpole": true, "channel_direction": "up", "prior_trend": "down"}'::jsonb,
 '目標價 = 跌破點 - 旗桿長度',
 true, 24),

('CHART_025', '上升楔形', 'Rising Wedge', 'REVERSAL',
 '15-45', 'BEARISH_REVERSAL', 65,
 '向上收斂的支撐與壓力線，通常向下突破',
 '{"support": "ascending", "resistance": "ascending", "convergence": true}'::jsonb,
 '目標價 = 跌破點 - 楔形高度',
 true, 25),

('CHART_026', '下降楔形', 'Falling Wedge', 'REVERSAL',
 '15-45', 'BULLISH_REVERSAL', 65,
 '向下收斂的支撐與壓力線，通常向上突破',
 '{"support": "descending", "resistance": "descending", "convergence": true}'::jsonb,
 '目標價 = 突破點 + 楔形高度',
 true, 26),

('CHART_027', '矩形整理', 'Rectangle', 'CONTINUATION',
 '20-60', 'NEUTRAL', 60,
 '價格在水平支撐與壓力間震盪',
 '{"support": "horizontal", "resistance": "horizontal"}'::jsonb,
 '目標價 = 突破點 ± 矩形高度',
 true, 27),

('CHART_028', '上升通道', 'Ascending Channel', 'CONTINUATION',
 '30-90', 'BULLISH_CONTINUATION', 65,
 '平行的上升支撐與壓力線',
 '{"support": "ascending", "resistance": "ascending", "parallel": true}'::jsonb,
 '在通道內操作，突破後目標為通道寬度',
 true, 28),

('CHART_029', '下降通道', 'Descending Channel', 'CONTINUATION',
 '30-90', 'BEARISH_CONTINUATION', 65,
 '平行的下降支撐與壓力線',
 '{"support": "descending", "resistance": "descending", "parallel": true}'::jsonb,
 '在通道內操作，突破後目標為通道寬度',
 true, 29),

-- ============================================================
-- GAP PATTERNS (缺口型態)
-- ============================================================
('CHART_040', '突破缺口', 'Breakaway Gap', 'GAP',
 '1-5', 'BULLISH_CONTINUATION', 75,
 '價格跳空突破重要支撐或壓力',
 '{"gap_type": "breakaway", "volume": "high"}'::jsonb,
 '缺口通常不會被回補，為強勢信號',
 true, 40),

('CHART_041', '逃逸缺口', 'Runaway Gap', 'GAP',
 '1-5', 'BULLISH_CONTINUATION', 70,
 '趨勢中途出現的跳空，加速趨勢',
 '{"gap_type": "runaway", "in_trend": true}'::jsonb,
 '目標價可用缺口測量法估計',
 true, 41),

('CHART_042', '竭盡缺口', 'Exhaustion Gap', 'GAP',
 '1-5', 'BEARISH_REVERSAL', 65,
 '趨勢末端出現的跳空，常為反轉前兆',
 '{"gap_type": "exhaustion", "near_end": true}'::jsonb,
 '缺口很快會被回補',
 true, 42),

('CHART_043', '島狀反轉', 'Island Reversal', 'GAP',
 '3-10', 'BEARISH_REVERSAL', 85,
 '竭盡缺口後出現反向突破缺口，形成島狀',
 '{"gap_up": true, "gap_down": true, "island": true}'::jsonb,
 '非常可靠的反轉信號',
 true, 43),

-- ============================================================
-- TREND PATTERNS (趨勢型態)
-- ============================================================
('CHART_050', '高點抬升', 'Higher Highs', 'CONTINUATION',
 '10-30', 'BULLISH_CONTINUATION', 70,
 '連續出現更高的波峰',
 '{"structure": "HH", "count_min": 2}'::jsonb,
 '多頭趨勢確認，持續看漲',
 true, 50),

('CHART_051', '低點抬升', 'Higher Lows', 'CONTINUATION',
 '10-30', 'BULLISH_CONTINUATION', 70,
 '連續出現更高的波谷',
 '{"structure": "HL", "count_min": 2}'::jsonb,
 '多頭趨勢確認，支撐逐步墊高',
 true, 51),

('CHART_052', '高點壓低', 'Lower Highs', 'CONTINUATION',
 '10-30', 'BEARISH_CONTINUATION', 70,
 '連續出現更低的波峰',
 '{"structure": "LH", "count_min": 2}'::jsonb,
 '空頭趨勢確認，壓力逐步下移',
 true, 52),

('CHART_053', '低點壓低', 'Lower Lows', 'CONTINUATION',
 '10-30', 'BEARISH_CONTINUATION', 70,
 '連續出現更低的波谷',
 '{"structure": "LL", "count_min": 2}'::jsonb,
 '空頭趨勢確認，持續看跌',
 true, 53),

('CHART_054', '杯柄型態', 'Cup and Handle', 'CONTINUATION',
 '30-180', 'BULLISH_CONTINUATION', 80,
 '圓弧底後形成小幅回檔（柄部）再突破',
 '{"cup": "rounded", "handle": "pullback", "breakout": true}'::jsonb,
 '目標價 = 突破點 + 杯深',
 true, 54)

ON CONFLICT (pattern_id) DO UPDATE SET
    pattern_name = EXCLUDED.pattern_name,
    english_name = EXCLUDED.english_name,
    pattern_category = EXCLUDED.pattern_category,
    typical_duration = EXCLUDED.typical_duration,
    signal_type = EXCLUDED.signal_type,
    typical_strength = EXCLUDED.typical_strength,
    description = EXCLUDED.description,
    key_characteristics = EXCLUDED.key_characteristics,
    target_calculation = EXCLUDED.target_calculation,
    is_active = EXCLUDED.is_active,
    display_order = EXCLUDED.display_order,
    updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 3. pattern_signal_definitions - Pattern signal definitions
-- ------------------------------------------------------------

INSERT INTO pattern_signal_definitions (
    signal_code, signal_name, signal_name_en, signal_type,
    source_category, default_strength, description, is_active, display_order
) VALUES
('PTN_SIG_001', '看漲K線型態', 'Bullish Candlestick Pattern', 'BUY', 'KLINE', 'MEDIUM',
 '偵測到看漲的K線型態', true, 1),
('PTN_SIG_002', '看跌K線型態', 'Bearish Candlestick Pattern', 'SELL', 'KLINE', 'MEDIUM',
 '偵測到看跌的K線型態', true, 2),
('PTN_SIG_003', '頭肩頂完成', 'Head and Shoulders Top Confirmed', 'SELL', 'CHART', 'HIGH',
 '頭肩頂型態跌破頸線', true, 3),
('PTN_SIG_004', '頭肩底完成', 'Head and Shoulders Bottom Confirmed', 'BUY', 'CHART', 'HIGH',
 '頭肩底型態突破頸線', true, 4),
('PTN_SIG_005', '雙重頂確認', 'Double Top Confirmed', 'SELL', 'CHART', 'HIGH',
 '雙重頂型態跌破支撐', true, 5),
('PTN_SIG_006', '雙重底確認', 'Double Bottom Confirmed', 'BUY', 'CHART', 'HIGH',
 '雙重底型態突破壓力', true, 6),
('PTN_SIG_007', '三角形向上突破', 'Triangle Breakout Up', 'BUY', 'CHART', 'MEDIUM',
 '三角形整理向上突破', true, 7),
('PTN_SIG_008', '三角形向下突破', 'Triangle Breakout Down', 'SELL', 'CHART', 'MEDIUM',
 '三角形整理向下突破', true, 8),
('PTN_SIG_009', '旗形向上突破', 'Flag Breakout Up', 'BUY', 'CHART', 'MEDIUM',
 '旗形整理向上突破', true, 9),
('PTN_SIG_010', '旗形向下突破', 'Flag Breakout Down', 'SELL', 'CHART', 'MEDIUM',
 '旗形整理向下突破', true, 10),
('PTN_SIG_011', '趨勢轉多', 'Trend Turn Bullish', 'BUY', 'TREND', 'HIGH',
 '趨勢由空轉多確認', true, 11),
('PTN_SIG_012', '趨勢轉空', 'Trend Turn Bearish', 'SELL', 'TREND', 'HIGH',
 '趨勢由多轉空確認', true, 12),
('PTN_SIG_013', '支撐確認', 'Support Confirmed', 'BUY', 'SUPPORT_RESISTANCE', 'MEDIUM',
 '價格在支撐位獲得支撐', true, 13),
('PTN_SIG_014', '壓力突破', 'Resistance Breakout', 'BUY', 'SUPPORT_RESISTANCE', 'MEDIUM',
 '價格突破重要壓力位', true, 14),
('PTN_SIG_015', '支撐跌破', 'Support Breakdown', 'SELL', 'SUPPORT_RESISTANCE', 'MEDIUM',
 '價格跌破重要支撐位', true, 15),
('PTN_SIG_016', '壓力確認', 'Resistance Confirmed', 'SELL', 'SUPPORT_RESISTANCE', 'MEDIUM',
 '價格在壓力位受阻', true, 16),
('PTN_SIG_017', '突破缺口', 'Breakaway Gap', 'BUY', 'GAP', 'HIGH',
 '向上突破缺口出現', true, 17),
('PTN_SIG_018', '島狀反轉(看漲)', 'Island Reversal Bullish', 'BUY', 'GAP', 'CRITICAL',
 '看漲島狀反轉型態', true, 18),
('PTN_SIG_019', '島狀反轉(看跌)', 'Island Reversal Bearish', 'SELL', 'GAP', 'CRITICAL',
 '看跌島狀反轉型態', true, 19),
('PTN_SIG_020', '杯柄突破', 'Cup and Handle Breakout', 'BUY', 'CHART', 'HIGH',
 '杯柄型態突破柄部高點', true, 20)

ON CONFLICT (signal_code) DO UPDATE SET
    signal_name = EXCLUDED.signal_name,
    signal_name_en = EXCLUDED.signal_name_en,
    signal_type = EXCLUDED.signal_type,
    source_category = EXCLUDED.source_category,
    default_strength = EXCLUDED.default_strength,
    description = EXCLUDED.description,
    is_active = EXCLUDED.is_active,
    display_order = EXCLUDED.display_order,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- End of 040_m10.sql
-- ============================================================
