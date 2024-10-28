package com.chenxuan353.mock.config;

import com.chenxuan353.mock.interceptor.EngineControllerInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AllArgsConstructor
@Configuration
public class WebInterceptorConfiguration implements WebMvcConfigurer {
    private final EngineControllerInterceptor engineControllerInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(engineControllerInterceptor)
                .addPathPatterns("/mock/engine/**");
    }
}
