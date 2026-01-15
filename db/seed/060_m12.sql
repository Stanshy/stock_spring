-- ============================================================
-- FinShark Seed: 060_m12.sql
-- Module: M12 - Macro & Industry Analysis
-- Description: Macro indicators, sectors, and themes definitions
-- Idempotent: Yes (ON CONFLICT DO NOTHING / DO UPDATE)
-- ============================================================

-- ------------------------------------------------------------
-- 1. macro_indicators - Macroeconomic indicator definitions
-- ------------------------------------------------------------

INSERT INTO macro_indicators (
    indicator_code, indicator_name, indicator_name_en,
    region, category, unit, frequency, source, source_url,
    is_active, display_order
) VALUES
-- Taiwan Macro Indicators
('TW_GDP_YOY', 'GDP 年增率', 'GDP YoY Growth Rate',
 'TW', 'GDP', '%', 'QUARTERLY', '主計總處',
 'https://www.stat.gov.tw/', true, 1),

('TW_CPI_YOY', 'CPI 年增率', 'CPI YoY Growth Rate',
 'TW', 'INFLATION', '%', 'MONTHLY', '主計總處',
 'https://www.stat.gov.tw/', true, 2),

('TW_PMI', '製造業 PMI', 'Manufacturing PMI',
 'TW', 'SENTIMENT', '指數', 'MONTHLY', '中經院',
 'https://www.cier.edu.tw/', true, 3),

('TW_EXPORT_YOY', '出口年增率', 'Export YoY Growth Rate',
 'TW', 'TRADE', '%', 'MONTHLY', '財政部',
 'https://www.mof.gov.tw/', true, 4),

('TW_IMPORT_YOY', '進口年增率', 'Import YoY Growth Rate',
 'TW', 'TRADE', '%', 'MONTHLY', '財政部',
 'https://www.mof.gov.tw/', true, 5),

('TW_M1B_YOY', 'M1B 年增率', 'M1B YoY Growth Rate',
 'TW', 'MONEY', '%', 'MONTHLY', '央行',
 'https://www.cbc.gov.tw/', true, 6),

('TW_M2_YOY', 'M2 年增率', 'M2 YoY Growth Rate',
 'TW', 'MONEY', '%', 'MONTHLY', '央行',
 'https://www.cbc.gov.tw/', true, 7),

('TW_POLICY_RATE', '央行重貼現率', 'CBC Rediscount Rate',
 'TW', 'INTEREST', '%', 'IRREGULAR', '央行',
 'https://www.cbc.gov.tw/', true, 8),

('TW_LEADING_INDEX', '景氣領先指標', 'Leading Economic Index',
 'TW', 'SENTIMENT', '指數', 'MONTHLY', '國發會',
 'https://www.ndc.gov.tw/', true, 9),

('TW_COINCIDENT_INDEX', '景氣同時指標', 'Coincident Economic Index',
 'TW', 'SENTIMENT', '指數', 'MONTHLY', '國發會',
 'https://www.ndc.gov.tw/', true, 10),

('TW_MONITOR_SCORE', '景氣燈號分數', 'Economic Monitor Score',
 'TW', 'SENTIMENT', '分', 'MONTHLY', '國發會',
 'https://www.ndc.gov.tw/', true, 11),

('TW_UNEMPLOYMENT_RATE', '失業率', 'Unemployment Rate',
 'TW', 'EMPLOYMENT', '%', 'MONTHLY', '主計總處',
 'https://www.stat.gov.tw/', true, 12),

-- US Macro Indicators
('US_FED_RATE', '聯邦基金利率', 'Federal Funds Rate',
 'US', 'INTEREST', '%', 'IRREGULAR', 'Fed',
 'https://www.federalreserve.gov/', true, 20),

('US_CPI_YOY', '美國 CPI 年增率', 'US CPI YoY Growth Rate',
 'US', 'INFLATION', '%', 'MONTHLY', 'BLS',
 'https://www.bls.gov/', true, 21),

('US_10Y_YIELD', '美國 10 年期公債殖利率', 'US 10Y Treasury Yield',
 'US', 'INTEREST', '%', 'DAILY', 'Treasury',
 'https://www.treasury.gov/', true, 22),

('US_2Y_YIELD', '美國 2 年期公債殖利率', 'US 2Y Treasury Yield',
 'US', 'INTEREST', '%', 'DAILY', 'Treasury',
 'https://www.treasury.gov/', true, 23),

-- Global Market Indicators
('VIX', 'VIX 恐慌指數', 'VIX Volatility Index',
 'GLOBAL', 'MARKET', '指數', 'DAILY', 'CBOE',
 'https://www.cboe.com/', true, 30),

('DXY', '美元指數', 'US Dollar Index',
 'GLOBAL', 'MARKET', '指數', 'DAILY', 'ICE',
 'https://www.theice.com/', true, 31)

ON CONFLICT (indicator_code) DO UPDATE SET
    indicator_name = EXCLUDED.indicator_name,
    indicator_name_en = EXCLUDED.indicator_name_en,
    region = EXCLUDED.region,
    category = EXCLUDED.category,
    unit = EXCLUDED.unit,
    frequency = EXCLUDED.frequency,
    source = EXCLUDED.source,
    source_url = EXCLUDED.source_url,
    is_active = EXCLUDED.is_active,
    display_order = EXCLUDED.display_order,
    updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 2. sectors - Industry sector definitions (Level 1: Major categories)
-- ------------------------------------------------------------

INSERT INTO sectors (
    sector_code, sector_name, sector_name_en,
    level, parent_code, classification_type, description,
    display_order, is_active
) VALUES
-- Level 1: Major Categories
('E', '電子工業', 'Electronics Industry',
 1, NULL, 'TWSE', '電子相關產業',
 1, true),

('F', '金融保險', 'Finance & Insurance',
 1, NULL, 'TWSE', '金融保險相關產業',
 2, true),

('T', '傳統產業', 'Traditional Industry',
 1, NULL, 'TWSE', '傳統製造與服務業',
 3, true)

ON CONFLICT (sector_code) DO UPDATE SET
    sector_name = EXCLUDED.sector_name,
    sector_name_en = EXCLUDED.sector_name_en,
    level = EXCLUDED.level,
    parent_code = EXCLUDED.parent_code,
    classification_type = EXCLUDED.classification_type,
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- Level 2: Sub-categories (Electronics)
INSERT INTO sectors (
    sector_code, sector_name, sector_name_en,
    level, parent_code, classification_type, description,
    display_order, is_active
) VALUES
('24', '半導體業', 'Semiconductor',
 2, 'E', 'TWSE', '半導體設計、製造、封測',
 101, true),

('25', '電腦及週邊設備業', 'Computer & Peripheral Equipment',
 2, 'E', 'TWSE', '電腦主機、筆電、週邊設備',
 102, true),

('26', '光電業', 'Optoelectronics',
 2, 'E', 'TWSE', '面板、LED、太陽能',
 103, true),

('27', '通信網路業', 'Communications & Internet',
 2, 'E', 'TWSE', '網路設備、通訊設備',
 104, true),

('28', '電子零組件業', 'Electronic Parts & Components',
 2, 'E', 'TWSE', '被動元件、連接器、PCB',
 105, true),

('29', '電子通路業', 'Electronic Products Distribution',
 2, 'E', 'TWSE', '電子產品通路商',
 106, true),

('30', '資訊服務業', 'Information Service',
 2, 'E', 'TWSE', '軟體、系統整合、資訊服務',
 107, true),

('31', '其他電子業', 'Other Electronics',
 2, 'E', 'TWSE', '其他電子相關',
 108, true)

ON CONFLICT (sector_code) DO UPDATE SET
    sector_name = EXCLUDED.sector_name,
    sector_name_en = EXCLUDED.sector_name_en,
    level = EXCLUDED.level,
    parent_code = EXCLUDED.parent_code,
    classification_type = EXCLUDED.classification_type,
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- Level 2: Sub-categories (Finance)
INSERT INTO sectors (
    sector_code, sector_name, sector_name_en,
    level, parent_code, classification_type, description,
    display_order, is_active
) VALUES
('17', '金融保險業', 'Financial & Insurance',
 2, 'F', 'TWSE', '銀行、保險、證券、金控',
 201, true)

ON CONFLICT (sector_code) DO UPDATE SET
    sector_name = EXCLUDED.sector_name,
    sector_name_en = EXCLUDED.sector_name_en,
    level = EXCLUDED.level,
    parent_code = EXCLUDED.parent_code,
    classification_type = EXCLUDED.classification_type,
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- Level 2: Sub-categories (Traditional)
INSERT INTO sectors (
    sector_code, sector_name, sector_name_en,
    level, parent_code, classification_type, description,
    display_order, is_active
) VALUES
('01', '水泥工業', 'Cement',
 2, 'T', 'TWSE', '水泥製造',
 301, true),

('02', '食品工業', 'Foods',
 2, 'T', 'TWSE', '食品加工製造',
 302, true),

('03', '塑膠工業', 'Plastics',
 2, 'T', 'TWSE', '塑膠製品製造',
 303, true),

('04', '紡織纖維', 'Textiles',
 2, 'T', 'TWSE', '紡織、成衣',
 304, true),

('05', '電機機械', 'Electric Machinery',
 2, 'T', 'TWSE', '電機設備製造',
 305, true),

('06', '電器電纜', 'Electrical & Cable',
 2, 'T', 'TWSE', '電線電纜製造',
 306, true),

('08', '玻璃陶瓷', 'Glass & Ceramic',
 2, 'T', 'TWSE', '玻璃陶瓷製造',
 307, true),

('09', '造紙工業', 'Paper & Pulp',
 2, 'T', 'TWSE', '紙漿、紙品製造',
 308, true),

('10', '鋼鐵工業', 'Iron & Steel',
 2, 'T', 'TWSE', '鋼鐵冶煉、加工',
 309, true),

('11', '橡膠工業', 'Rubber',
 2, 'T', 'TWSE', '橡膠製品製造',
 310, true),

('12', '汽車工業', 'Automobile',
 2, 'T', 'TWSE', '汽車製造、零件',
 311, true),

('14', '建材營造', 'Building Material & Construction',
 2, 'T', 'TWSE', '建材製造、營建',
 312, true),

('15', '航運業', 'Shipping & Transportation',
 2, 'T', 'TWSE', '海運、空運、陸運',
 313, true),

('16', '觀光餐旅', 'Tourism',
 2, 'T', 'TWSE', '觀光旅遊、餐飲',
 314, true),

('18', '貿易百貨', 'Trading & Consumer Goods',
 2, 'T', 'TWSE', '貿易、百貨零售',
 315, true),

('20', '油電燃氣業', 'Oil, Gas & Electricity',
 2, 'T', 'TWSE', '石油、天然氣、電力',
 316, true),

('21', '化學工業', 'Chemical',
 2, 'T', 'TWSE', '化學原料、製品',
 317, true),

('22', '生技醫療業', 'Biotechnology & Medical Care',
 2, 'T', 'TWSE', '生技製藥、醫療器材',
 318, true),

('23', '其他業', 'Others',
 2, 'T', 'TWSE', '其他未分類產業',
 319, true)

ON CONFLICT (sector_code) DO UPDATE SET
    sector_name = EXCLUDED.sector_name,
    sector_name_en = EXCLUDED.sector_name_en,
    level = EXCLUDED.level,
    parent_code = EXCLUDED.parent_code,
    classification_type = EXCLUDED.classification_type,
    description = EXCLUDED.description,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ------------------------------------------------------------
-- 3. themes - Custom investment theme definitions
-- ------------------------------------------------------------

INSERT INTO themes (
    theme_code, theme_name, theme_name_en,
    description, inclusion_criteria,
    display_order, is_active, created_by
) VALUES
('AI_CONCEPT', 'AI 概念股', 'AI Concept',
 '人工智慧相關概念股，包含 AI 晶片、雲端運算、AI 應用等',
 '1. AI 晶片設計/製造 2. AI 伺服器供應鏈 3. AI 軟體/服務 4. 雲端運算基礎設施',
 1, true, 'SYSTEM'),

('EV_CONCEPT', '電動車概念', 'Electric Vehicle Concept',
 '電動車供應鏈相關，包含電池、馬達、充電設備等',
 '1. 電池芯/模組製造 2. 馬達/驅動系統 3. 充電設備 4. 車用電子 5. 整車製造',
 2, true, 'SYSTEM'),

('5G_CONCEPT', '5G 概念股', '5G Concept',
 '5G 基礎建設與應用相關',
 '1. 5G 基站設備 2. 小型基站 3. 5G 終端裝置 4. 5G 應用服務',
 3, true, 'SYSTEM'),

('ESG_LEADER', 'ESG 領袖股', 'ESG Leaders',
 'ESG 評分優良的企業',
 '1. MSCI ESG 評級 AA 以上 2. 獲得 CDP A 級評價 3. 納入道瓊永續指數成分股',
 4, true, 'SYSTEM'),

('DIVIDEND_KING', '高股息精選', 'Dividend Kings',
 '連續配息且殖利率穩定的股票',
 '1. 連續 5 年以上配息 2. 殖利率高於 4% 3. 配息率低於 80% 4. 獲利穩定成長',
 5, true, 'SYSTEM'),

('GROWTH_STOCK', '成長動能股', 'Growth Momentum',
 '營收與獲利高成長的企業',
 '1. 近四季營收年增率 > 20% 2. 近四季 EPS 年增率 > 20% 3. 毛利率維持或成長',
 6, true, 'SYSTEM'),

('APPLE_SUPPLY', '蘋果供應鏈', 'Apple Supply Chain',
 'Apple 產品主要供應商',
 '1. Apple 認證供應商 2. 營收來自 Apple 佔比顯著',
 7, true, 'SYSTEM'),

('SERVER_CONCEPT', '伺服器概念', 'Server Concept',
 '伺服器及資料中心相關供應鏈',
 '1. 伺服器 ODM/OEM 2. 伺服器零組件 3. 資料中心設備',
 8, true, 'SYSTEM'),

('GREEN_ENERGY', '綠能概念', 'Green Energy',
 '再生能源與節能相關',
 '1. 太陽能 2. 風力發電 3. 儲能系統 4. 節能設備',
 9, true, 'SYSTEM'),

('METAVERSE', '元宇宙概念', 'Metaverse Concept',
 '元宇宙、VR/AR 相關',
 '1. VR/AR 裝置 2. 元宇宙平台 3. 3D 內容/引擎',
 10, true, 'SYSTEM')

ON CONFLICT (theme_code) DO UPDATE SET
    theme_name = EXCLUDED.theme_name,
    theme_name_en = EXCLUDED.theme_name_en,
    description = EXCLUDED.description,
    inclusion_criteria = EXCLUDED.inclusion_criteria,
    display_order = EXCLUDED.display_order,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- End of 060_m12.sql
-- ============================================================
