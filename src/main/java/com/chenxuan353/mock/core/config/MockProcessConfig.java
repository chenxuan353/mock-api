package com.chenxuan353.mock.core.config;

import lombok.Data;

/**
 * 处理器配置
 */
@Data
public class MockProcessConfig implements MockConfigInterface {
    /**
     * 处理器名称
     */
    private String name;
    /**
     * 处理器描述
     */
    private String des;
    /**
     * 是否启用
     */
    private boolean enable = true;
    private MockLogConfig log;
    private MockRequestMapping request;
    private MockResponseConfig response;
}
