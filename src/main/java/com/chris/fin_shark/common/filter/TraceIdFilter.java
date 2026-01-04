package com.chris.fin_shark.common.filter;

import com.chris.fin_shark.common.constant.ApiConstants;
import com.chris.fin_shark.common.util.TraceUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 追蹤 ID 過濾器
 *
 * 為每個請求生成唯一的追蹤 ID，並存入 MDC
 *
 * @author chris
 * @since 2025-12-24
 */
@Component
@Order(1)  // 最高優先級，確保最先執行
@Slf4j
public class TraceIdFilter implements Filter {

    /**
     * MDC 中的追蹤 ID 鍵名
     */
    private static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 1. 從請求頭取得追蹤 ID（如果有）
            String traceId = httpRequest.getHeader(ApiConstants.HEADER_TRACE_ID);

            // 2. 如果沒有，則生成新的追蹤 ID
            if (traceId == null || traceId.isEmpty()) {
                traceId = TraceUtil.generateTraceId();
            }

            // 3. 存入 MDC（Mapped Diagnostic Context）
            MDC.put(MDC_TRACE_ID_KEY, traceId);

            // 4. 將追蹤 ID 加入回應頭
            httpResponse.setHeader(ApiConstants.HEADER_TRACE_ID, traceId);

            // 5. 繼續執行
            chain.doFilter(request, response);

        } finally {
            // 6. 清除 MDC（避免記憶體洩漏）
            MDC.clear();
        }
    }
}
