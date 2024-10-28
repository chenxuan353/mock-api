package com.chenxuan353.mock.config;

import com.chenxuan353.mock.filter.RequestLogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebFilterConfiguration {

    @Bean
    public FilterRegistrationBean<RequestLogFilter> requestLogFilterRegistrationBean(RequestLogFilter requestLogFilter) {
        FilterRegistrationBean<RequestLogFilter> bean = new FilterRegistrationBean<>();
        bean.setOrder(1);
        bean.setFilter(requestLogFilter);
        // 匹配所有url
        bean.addUrlPatterns("/*");
        return bean;
    }
}
