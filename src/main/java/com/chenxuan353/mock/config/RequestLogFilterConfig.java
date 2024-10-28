package com.chenxuan353.mock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "request.log")
@Data
public class RequestLogFilterConfig {
    private boolean onlyProcessLog = true;
}
