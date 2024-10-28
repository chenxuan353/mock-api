package com.chenxuan353.mock.core.config;

import lombok.Data;

/**
 * 响应组配置
 */
@Data
public class MockGroupConfig implements MockConfigInterface {
    /**
     * 响应组名称
     */
    private String name;
    /**
     * 响应组描述
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
