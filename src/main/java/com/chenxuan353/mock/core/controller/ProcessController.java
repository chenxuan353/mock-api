package com.chenxuan353.mock.core.controller;

import cn.hutool.core.io.FileUtil;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.config.MockRequestMapping;
import com.chenxuan353.mock.core.consts.MockRespError;
import com.chenxuan353.mock.core.process.MockProcessRunner;
import com.chenxuan353.mock.util.RespUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 处理器映射到的控制器
 */
@Builder
@Slf4j
public class ProcessController {
    /**
     * 控制器绑定的处理单元
     */
    private final MockProcess mockProcess;
    /**
     * 控制器绑定的requestMapping
     */
    private final MockRequestMapping requestMapping;
    /**
     * 控制器绑定的requestMappingInfo
     */
    private final RequestMappingInfo requestMappingInfo;
    private final MockProcessRunner mockProcessRunner;

    public MockProcess getMockProcess() {
        return mockProcess;
    }

    private Throwable getRootCause(Throwable e) {
        Throwable cause = null;
        Throwable result = e;

        while (null != (cause = result.getCause()) && (result != cause)) {
            result = cause;
        }
        return result;
    }

    public ResponseEntity<?> commonProcessInternal(HttpServletRequest request, HttpServletResponse response, String requestBody) {
        try {
            return mockProcessRunner.process(mockProcess, request, response, requestBody);
        } catch (Exception e) {
            String dependentAbsolutePath = FileUtil.getAbsolutePath(new File(mockProcess.getDependentPath()));
            log.error("执行响应处理器 `{}` 时异常，关联路径：{}", mockProcess.getName(), dependentAbsolutePath);
            log.error("异常堆栈：", e);
            Map<String, String> exceptionInfo = new HashMap<>();
            exceptionInfo.put("processName", mockProcess.getName());
            exceptionInfo.put("dependentPath", dependentAbsolutePath);
            exceptionInfo.put("exceptionMsg", e.getMessage());
            exceptionInfo.put("exceptionRootCauseMsg", getRootCause(e).getMessage());
            return RespUtil.error(MockRespError.MOCK_PROCESS_RUNNER_EXCEPTION, exceptionInfo);
        }
    }

    public ResponseEntity<?> commonProcess(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) String requestBody) {
        return commonProcessInternal(request, response, requestBody);
    }

    @CrossOrigin(originPatterns = "*", allowCredentials = "true")
    public ResponseEntity<?> commonProcessCrossOrigin(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = false) String requestBody) {
        return commonProcessInternal(request, response, requestBody);
    }

}
