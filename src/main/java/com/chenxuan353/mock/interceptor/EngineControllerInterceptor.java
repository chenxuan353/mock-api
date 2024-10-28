package com.chenxuan353.mock.interceptor;


import com.chenxuan353.mock.config.MockConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@AllArgsConstructor
@Component
@Slf4j
public class EngineControllerInterceptor implements HandlerInterceptor {

    private MockConfig mockConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!mockConfig.isEnableEngineDebug()) {
            if (!"v1".equals(request.getHeader("MockVersion"))) {
                return true;
            }
            response.sendError(401);
            return false;
        }
        return true;
    }
}
