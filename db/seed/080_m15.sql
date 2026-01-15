-- ============================================================
-- FinShark Seed: 080_m15.sql
-- Module: M15 - Alert Notification System
-- Description: Notification templates for various channels
-- Idempotent: Yes (ON CONFLICT DO UPDATE)
-- ============================================================

-- ------------------------------------------------------------
-- 1. notification_templates - System notification templates
-- ------------------------------------------------------------

INSERT INTO notification_templates (
    template_id, template_name, template_type, channel,
    subject_template, body_template, is_default, is_active
) VALUES

-- ============================================================
-- EMAIL TEMPLATES (Email 通知範本)
-- ============================================================
('TPL_EMAIL_SIGNAL', '信號通知 Email', 'SIGNAL', 'EMAIL',
 '📈 交易信號: {{stockId}} {{stockName}} - {{direction}}',
 '<h2>📈 交易信號警報</h2>
<table style="border-collapse: collapse; width: 100%; max-width: 500px;">
<tr style="background-color: #f5f5f5;"><td style="padding: 8px; border: 1px solid #ddd;">股票</td><td style="padding: 8px; border: 1px solid #ddd;">{{stockId}} {{stockName}}</td></tr>
<tr><td style="padding: 8px; border: 1px solid #ddd;">信號方向</td><td style="padding: 8px; border: 1px solid #ddd;">{{direction}}</td></tr>
<tr style="background-color: #f5f5f5;"><td style="padding: 8px; border: 1px solid #ddd;">評級</td><td style="padding: 8px; border: 1px solid #ddd;">{{grade}} ({{score}}分)</td></tr>
<tr><td style="padding: 8px; border: 1px solid #ddd;">現價</td><td style="padding: 8px; border: 1px solid #ddd;">{{price}} ({{changePercent}}%)</td></tr>
</table>
<p><strong>信號摘要</strong></p>
<p>{{signalSummary}}</p>
<hr>
<p style="color: #888; font-size: 12px;">此信件由 FinShark 系統自動發送</p>',
 true, true),

('TPL_EMAIL_PRICE', '價格警報 Email', 'PRICE', 'EMAIL',
 '💰 價格警報: {{stockId}} {{stockName}} 達到 {{price}}',
 '<h2>💰 價格警報</h2>
<table style="border-collapse: collapse; width: 100%; max-width: 500px;">
<tr style="background-color: #f5f5f5;"><td style="padding: 8px; border: 1px solid #ddd;">股票</td><td style="padding: 8px; border: 1px solid #ddd;">{{stockId}} {{stockName}}</td></tr>
<tr><td style="padding: 8px; border: 1px solid #ddd;">觸發條件</td><td style="padding: 8px; border: 1px solid #ddd;">{{condition}}</td></tr>
<tr style="background-color: #f5f5f5;"><td style="padding: 8px; border: 1px solid #ddd;">觸發價格</td><td style="padding: 8px; border: 1px solid #ddd;">{{price}}</td></tr>
<tr><td style="padding: 8px; border: 1px solid #ddd;">漲跌幅</td><td style="padding: 8px; border: 1px solid #ddd;">{{changePercent}}%</td></tr>
</table>
<p><strong>規則名稱</strong>: {{ruleName}}</p>
<hr>
<p style="color: #888; font-size: 12px;">此信件由 FinShark 系統自動發送</p>',
 true, true),

('TPL_EMAIL_CHANGE', '漲跌幅警報 Email', 'CHANGE', 'EMAIL',
 '📊 漲跌幅警報: {{stockId}} {{stockName}} {{changePercent}}%',
 '<h2>📊 漲跌幅警報</h2>
<table style="border-collapse: collapse; width: 100%; max-width: 500px;">
<tr style="background-color: #f5f5f5;"><td style="padding: 8px; border: 1px solid #ddd;">股票</td><td style="padding: 8px; border: 1px solid #ddd;">{{stockId}} {{stockName}}</td></tr>
<tr><td style="padding: 8px; border: 1px solid #ddd;">當前價格</td><td style="padding: 8px; border: 1px solid #ddd;">{{price}}</td></tr>
<tr style="background-color: #f5f5f5;"><td style="padding: 8px; border: 1px solid #ddd;">漲跌幅</td><td style="padding: 8px; border: 1px solid #ddd;">{{changePercent}}%</td></tr>
<tr><td style="padding: 8px; border: 1px solid #ddd;">觸發門檻</td><td style="padding: 8px; border: 1px solid #ddd;">{{threshold}}%</td></tr>
</table>
<hr>
<p style="color: #888; font-size: 12px;">此信件由 FinShark 系統自動發送</p>',
 true, true),

('TPL_EMAIL_VOLUME', '成交量警報 Email', 'VOLUME', 'EMAIL',
 '📈 成交量警報: {{stockId}} {{stockName}} 量能放大',
 '<h2>📈 成交量警報</h2>
<table style="border-collapse: collapse; width: 100%; max-width: 500px;">
<tr style="background-color: #f5f5f5;"><td style="padding: 8px; border: 1px solid #ddd;">股票</td><td style="padding: 8px; border: 1px solid #ddd;">{{stockId}} {{stockName}}</td></tr>
<tr><td style="padding: 8px; border: 1px solid #ddd;">當日成交量</td><td style="padding: 8px; border: 1px solid #ddd;">{{volume}} 張</td></tr>
<tr style="background-color: #f5f5f5;"><td style="padding: 8px; border: 1px solid #ddd;">平均成交量</td><td style="padding: 8px; border: 1px solid #ddd;">{{avgVolume}} 張</td></tr>
<tr><td style="padding: 8px; border: 1px solid #ddd;">量能倍數</td><td style="padding: 8px; border: 1px solid #ddd;">{{volumeRatio}}x</td></tr>
</table>
<hr>
<p style="color: #888; font-size: 12px;">此信件由 FinShark 系統自動發送</p>',
 true, true),

('TPL_EMAIL_BATCH', '批次通知 Email', 'BATCH', 'EMAIL',
 '📊 您有 {{count}} 則新警報',
 '<h2>📊 警報摘要</h2>
<p>您有 <strong>{{count}}</strong> 則新警報：</p>
<ul>
{{#alerts}}
<li>{{stockId}} {{stockName}} - {{direction}} ({{grade}})</li>
{{/alerts}}
</ul>
<p><a href="{{detailUrl}}" style="background-color: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">點擊查看詳情</a></p>
<hr>
<p style="color: #888; font-size: 12px;">此信件由 FinShark 系統自動發送</p>',
 true, true),

-- ============================================================
-- LINE TEMPLATES (Line 通知範本)
-- ============================================================
('TPL_LINE_SIGNAL', '信號通知 Line', 'SIGNAL', 'LINE',
 NULL,
 '📈 交易信號警報

股票: {{stockId}} {{stockName}}
方向: {{direction}} {{directionEmoji}}
評級: {{grade}} ({{score}}分)
現價: {{price}} ({{changePercent}}%)

【信號摘要】
{{signalSummary}}

⏰ {{triggeredAt}}',
 true, true),

('TPL_LINE_PRICE', '價格警報 Line', 'PRICE', 'LINE',
 NULL,
 '💰 價格警報

股票: {{stockId}} {{stockName}}
觸發條件: {{condition}}
觸發價格: {{price}}
漲跌幅: {{changePercent}}%

規則: {{ruleName}}
⏰ {{triggeredAt}}',
 true, true),

('TPL_LINE_CHANGE', '漲跌幅警報 Line', 'CHANGE', 'LINE',
 NULL,
 '📊 漲跌幅警報

股票: {{stockId}} {{stockName}}
現價: {{price}}
漲跌幅: {{changePercent}}%
門檻: {{threshold}}%

⏰ {{triggeredAt}}',
 true, true),

('TPL_LINE_VOLUME', '成交量警報 Line', 'VOLUME', 'LINE',
 NULL,
 '📈 成交量警報

股票: {{stockId}} {{stockName}}
當日量: {{volume}} 張
平均量: {{avgVolume}} 張
倍數: {{volumeRatio}}x

⏰ {{triggeredAt}}',
 true, true),

('TPL_LINE_BATCH', '批次通知 Line', 'BATCH', 'LINE',
 NULL,
 '📊 警報摘要

您有 {{count}} 則新警報:
{{#alerts}}
• {{stockId}} {{stockName}} - {{direction}} ({{grade}})
{{/alerts}}

查看詳情: {{detailUrl}}',
 true, true),

-- ============================================================
-- PUSH TEMPLATES (推播通知範本)
-- ============================================================
('TPL_PUSH_SIGNAL', '信號通知 Push', 'SIGNAL', 'PUSH',
 NULL,
 '{{stockId}} {{stockName}} - {{direction}} ({{grade}})',
 true, true),

('TPL_PUSH_PRICE', '價格警報 Push', 'PRICE', 'PUSH',
 NULL,
 '{{stockId}} {{stockName}} 達到 {{price}}',
 true, true),

('TPL_PUSH_CHANGE', '漲跌幅警報 Push', 'CHANGE', 'PUSH',
 NULL,
 '{{stockId}} {{stockName}} {{changePercent}}%',
 true, true),

('TPL_PUSH_VOLUME', '成交量警報 Push', 'VOLUME', 'PUSH',
 NULL,
 '{{stockId}} {{stockName}} 量能放大 {{volumeRatio}}x',
 true, true),

('TPL_PUSH_BATCH', '批次通知 Push', 'BATCH', 'PUSH',
 NULL,
 '您有 {{count}} 則新警報',
 true, true)

ON CONFLICT (template_id) DO UPDATE SET
    template_name = EXCLUDED.template_name,
    template_type = EXCLUDED.template_type,
    channel = EXCLUDED.channel,
    subject_template = EXCLUDED.subject_template,
    body_template = EXCLUDED.body_template,
    is_default = EXCLUDED.is_default,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- ============================================================
-- End of 080_m15.sql
-- ============================================================
