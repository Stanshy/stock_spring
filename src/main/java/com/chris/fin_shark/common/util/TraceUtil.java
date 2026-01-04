package com.chris.fin_shark.common.util;

import java.util.UUID;

/**
 * 追蹤 ID 工具
 *
 * 用於生成請求追蹤 ID
 *
 * @author chris
 * @since 2025-12-24
 */
public final class TraceUtil {

    private TraceUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 追蹤 ID 前綴
     */
    private static final String TRACE_ID_PREFIX = "req_";

    /**
     * 生成追蹤 ID
     *
     * 格式: req_{uuid}
     * 範例: req_abc123def456
     *
     * @return 追蹤 ID
     */
    public static String generateTraceId() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return TRACE_ID_PREFIX + uuid.substring(0, 12);  // 取前 12 位
    }

    /**
     * 生成短追蹤 ID
     *
     * 格式: req_{timestamp}_{random}
     * 範例: req_1703401200_abc
     *
     * @return 短追蹤 ID
     */
    public static String generateShortTraceId() {
        long timestamp = System.currentTimeMillis() / 1000;  // 秒級時間戳
        String random = UUID.randomUUID().toString().substring(0, 3);
        return TRACE_ID_PREFIX + timestamp + "_" + random;
    }

    /**
     * 驗證追蹤 ID 格式
     *
     * @param traceId 追蹤 ID
     * @return true 合法，false 不合法
     */
    public static boolean isValidTraceId(String traceId) {
        if (traceId == null) {
            return false;
        }
        return traceId.startsWith(TRACE_ID_PREFIX) && traceId.length() > TRACE_ID_PREFIX.length();
    }
}
