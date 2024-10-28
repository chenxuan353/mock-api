package com.chenxuan353.mock.filter;

import com.chenxuan353.mock.config.RequestLogFilterConfig;
import com.chenxuan353.mock.core.component.MockGroup;
import com.chenxuan353.mock.core.service.MockResourceMappingService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.chenxuan353.mock.core.consts.MockProcessConsts.MOCK_REQUEST_MARK;

/**
 * 记录请求日志
 */
@Component
@Slf4j
public class RequestLogFilter extends OncePerRequestFilter {

    private RequestLogFilterConfig requestLogFilterConfig;
    private MockResourceMappingService mockResourceMappingService;
    private Map<String, MockGroup> resourceMapping;
    private List<String> resourceAbsPathList;

    @Autowired
    public void setRequestLogFilterConfig(RequestLogFilterConfig requestLogFilterConfig) {
        this.requestLogFilterConfig = requestLogFilterConfig;
    }


    private static final String[] HEADERS_TO_TRY = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR",
            "X-Real-IP"};

    /***
     * 获取客户端ip地址(可以穿透代理)
     * @param request
     * @return
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        for (String header : HEADERS_TO_TRY) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        StopWatch sw = new StopWatch("doFilterInternal");
        sw.start();
        try {
            chain.doFilter(request, response);
        } finally {
            if (!requestLogFilterConfig.isOnlyProcessLog() || request.getAttribute(MOCK_REQUEST_MARK) != null) {
                sw.stop();
                String method = request.getMethod();
                String url = request.getRequestURL().toString();
                String remoteAddr = getClientIpAddress(request);
                int status = response.getStatus();
                log.info("{} | {} => {} | {} | {}s", method, url, status, remoteAddr, String.format("%.2f", sw.lastTaskInfo().getTimeSeconds()));
            }
        }
    }
}