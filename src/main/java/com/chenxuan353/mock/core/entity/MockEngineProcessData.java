package com.chenxuan353.mock.core.entity;

import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.config.MockRequestMapping;
import com.chenxuan353.mock.core.config.MockResponseConfig;
import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.process.MockProcessRunner;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.Builder;
import lombok.Data;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock执行器信息
 */
@Data
@Builder
public class MockEngineProcessData {
    private final MockProcess mockProcess;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final HttpSession session;
    private final Map<String, String[]> parameterMap;
    private final Map<String, String> pathVariable;
    private final Map<String, String> headers;
    private final String requestBody;
    private Map<String, String> envResources;
    private Map<String, String> strTemplateMap;
    private final MockRequestMapping requestMapping;
    private MockResponseConfig responseConfig;
    private MockProcessType processType;

    public Map<String, String> getStrTemplateMap() {
        if (strTemplateMap == null) {
            strTemplateMap = new HashMap<>();
            MockResponseConfig config = getResponseConfig();
            if(config.getEnvionment() != null){
                for (Map.Entry<String, String> entry : config.getEnvionment().entrySet()) {
                    strTemplateMap.put("envionment." + entry.getKey(), entry.getValue());
                    strTemplateMap.put(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                strTemplateMap.put("headers." + entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String> entry : pathVariable.entrySet()) {
                strTemplateMap.put("pathVariable." + entry.getKey(), entry.getValue());
                strTemplateMap.put("pathVar." + entry.getKey(), entry.getValue());
                strTemplateMap.put(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
                String[] value = entry.getValue();
                if (value.length == 1) {
                    strTemplateMap.put("parameterMap." + entry.getKey(), value[0]);
                    strTemplateMap.put(entry.getKey(), value[0]);
                    continue;
                }
                for (int i = 0; i < value.length; i++) {
                    strTemplateMap.put("parameterMap." + entry.getKey() + "[" + i + "]", value[0]);
                    strTemplateMap.put(entry.getKey() + "[" + i + "]", value[0]);
                }
            }

        }
        return strTemplateMap;
    }

    public Map<String, String> getEnvResources() {
        if (envResources == null) {
            envResources = mockProcess.getResourceMap();
        }
        return envResources;
    }

    public String getResourceBody() {
        if (envResources != null) {
            return envResources.get("body");
        }
        return mockProcess.getResourceBody();
    }

    public MockResponseConfig getResponseConfig() {
        if (responseConfig == null) {
            responseConfig = mockProcess.getMergeResponseConfig();
        }
        return responseConfig;
    }

    public MockProcessType getProcessType() {
        if (processType != null) {
            return processType;
        }
        MockProcessType processType = getResponseConfig().getProcessType();
        if (processType != null) {
            this.processType = processType;
        } else if (this.processType == null) {
            this.processType = MockProcessType.TextTemplate;
        }
        MockProcess mockProcess = getMockProcess();
        if (mockProcess.isStaticMode()) {
            return MockProcessType.StaticFile;
        }
        String scriptPath = getResponseConfig().getScriptPath();
        Map<String, File> internalResourceMap = mockProcess.getInternalResourceMap();
        this.processType = MockProcessRunner.getProcessType(processType, scriptPath, internalResourceMap);
        return this.processType;
    }

}
