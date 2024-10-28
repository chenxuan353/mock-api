package com.chenxuan353.mock.core.service;

import cn.hutool.core.util.ArrayUtil;
import com.chenxuan353.mock.config.MockConfig;
import com.chenxuan353.mock.core.component.MockModule;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.config.MockRequestMapping;
import com.chenxuan353.mock.core.controller.ProcessController;
import com.chenxuan353.mock.core.process.MockProcessRunner;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock请求映射处理服务
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MockRequestMappingService {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final MockConfig mockConfig;
    private Method commonProcessMethod;
    private Method commonProcessCrossOrigin;
    private final Map<RequestMappingInfo, MockProcess> processRequestMapping = new HashMap<>();
    private final MockProcessRunner mockProcessRunner;

    @PostConstruct
    private void init() throws NoSuchMethodException {
        commonProcessMethod = ProcessController.class.getMethod("commonProcess", HttpServletRequest.class, HttpServletResponse.class, String.class);
        commonProcessCrossOrigin = ProcessController.class.getMethod("commonProcessCrossOrigin", HttpServletRequest.class, HttpServletResponse.class, String.class);
    }

    private RequestMappingInfo packageRequestMappingInfo(MockProcess mockProcess) {
        String requestMappingPath = mockProcess.getRequestMappingPath();
        RequestMappingInfo.Builder builder = RequestMappingInfo.paths(requestMappingPath);
        MockRequestMapping requestMapping = mockProcess.getMergeRequestMapping();
        if (ArrayUtil.isNotEmpty(requestMapping.getMethods())) {
            builder.methods(requestMapping.getMethods());
        }
        if (ArrayUtil.isNotEmpty(requestMapping.getHeaders())) {
            builder.headers(requestMapping.getHeaders());
        }
        if (ArrayUtil.isNotEmpty(requestMapping.getParams())) {
            builder.params(requestMapping.getParams());
        }
        if (ArrayUtil.isNotEmpty(requestMapping.getConsumes())) {
            builder.consumes(requestMapping.getConsumes());
        }
        if (ArrayUtil.isNotEmpty(requestMapping.getProduces())) {
            builder.produces(requestMapping.getProduces());
        }
        return builder.build();
    }

    private void unregisterMapping(RequestMappingInfo requestMappingInfo) {
        requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
    }

    public void registerMapping(MockProcess mockProcess) {
        RequestMappingInfo requestMappingInfo = packageRequestMappingInfo(mockProcess);
        if (processRequestMapping.containsKey(requestMappingInfo)) {
            MockProcess oldMockProcess = processRequestMapping.get(requestMappingInfo);
            log.warn("Mock处理器被覆盖：{}({}) - {}", oldMockProcess.getName(), oldMockProcess.getDisplayName(), oldMockProcess.getRequestMappingPath());
        }
        MockRequestMapping mergeRequestMapping = mockProcess.getMergeRequestMapping();
        ProcessController processController = ProcessController.builder()
                .mockProcess(mockProcess)
                .requestMappingInfo(requestMappingInfo)
                .requestMapping(mergeRequestMapping)
                .mockProcessRunner(mockProcessRunner)
                .build();
        if (mergeRequestMapping.getCors() != null && mergeRequestMapping.getCors()) {
            requestMappingHandlerMapping.registerMapping(requestMappingInfo, processController, commonProcessCrossOrigin);
        } else {
            requestMappingHandlerMapping.registerMapping(requestMappingInfo, processController, commonProcessMethod);
        }
        processRequestMapping.put(requestMappingInfo, mockProcess);
    }

    public void unregisterMapping(MockProcess mockProcess) {
        RequestMappingInfo requestMappingInfo = packageRequestMappingInfo(mockProcess);
        if (!processRequestMapping.containsKey(requestMappingInfo)) {
            return;
        }
        unregisterMapping(requestMappingInfo);
        processRequestMapping.remove(requestMappingInfo);
    }

    /**
     * 初始化请求映射
     *
     * @param mockModule 模块
     */
    public void initRequestMapping(MockModule mockModule) {
        if (mockModule == null) {
            return;
        }
        List<MockProcess> mockProcessList = mockModule.getEnableMockProcesses();
        for (MockProcess mockProcess : mockProcessList) {
            if (mockProcess.getMockProcessConfig() != null && !mockProcess.getMockProcessConfig().isEnable()) {
                return;
            }
            if (mockProcess.isStaticMode() && mockProcess.getParentGroup().isStaticMode() && mockConfig.isStaticModeResourceHandlerMappingProxy()) {
                continue;
            }
            registerMapping(mockProcess);
        }
    }

    /**
     * 清空请求映射
     */
    public void clearRequestMapping() {
        for (RequestMappingInfo requestMappingInfo : processRequestMapping.keySet()) {
            unregisterMapping(requestMappingInfo);
        }
        processRequestMapping.clear();
    }

    /**
     * 重载请求映射
     */
    public void reloadRequestMapping(MockModule mockModule) {
        clearRequestMapping();
        initRequestMapping(mockModule);
    }
}
