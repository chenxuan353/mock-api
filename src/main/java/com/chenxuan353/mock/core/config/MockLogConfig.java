package com.chenxuan353.mock.core.config;

import lombok.Data;

import java.util.List;

@Data
public class MockLogConfig {
    private boolean enable = false;
    private boolean printProcessTime = false;
    private boolean printRequest = true;
    private boolean printRequestBody = true;
    private boolean printHeaders = true;
    private boolean printUriVars = true;
    private boolean printParam = true;
    private boolean printSession = true;
    /**
     * 需打印的sessionKey
     */
    private List<String> printSessionKey;
    private boolean printResponse = true;
    private boolean printResponseHeaders = true;
    private boolean printResponseBody = true;
}
