package com.chenxuan353.mock.core.process;

import cn.hutool.core.collection.CollUtil;
import com.chenxuan353.mock.config.MockConfig;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.config.MockResponseConfig;
import com.chenxuan353.mock.core.consts.MockEngineType;
import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.engine.CommonJsonHelper;
import com.chenxuan353.mock.core.engine.CommonRespHelper;
import com.chenxuan353.mock.core.engine.CommonSafeSessionHelper;
import com.chenxuan353.mock.core.engine.CommonShareVarHelper;
import com.chenxuan353.mock.core.engine.graalvm.GraalJsContextOption;
import com.chenxuan353.mock.core.engine.graalvm.GraalvmLogger;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import com.chenxuan353.mock.core.exception.MockEngineException;
import com.chenxuan353.mock.core.exception.MockEngineScriptException;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

/**
 * GraalJs执行处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MockGraalJsProcess extends MockProcessAbstract {
    private final MockConfig mockConfig;

    @PostConstruct
    protected void init() {
        if (!mockConfig.getJsEngine().isSecureMode() && mockConfig.getEnableEngineList().stream().anyMatch(engineType -> MockEngineType.GraalJs == engineType)) {
            log.warn("GraalJs引擎未在安全模式下运行！");
        }
    }

    @Override
    public boolean accessType(MockProcessType type) {
        if (CollUtil.isEmpty(mockConfig.getEnableEngineList())) {
            return false;
        }
        // 引擎未启用时不处理任何数据
        if (mockConfig.getEnableEngineList().stream().noneMatch(engineType -> MockEngineType.GraalJs == engineType)) {
            return false;
        }
        if (MockProcessType.GraalJs == type) {
            return true;
        }
        if (MockProcessType.JS == type) {
            return true;
        }
        return MockProcessType.Javascript == type;
    }

    @Override
    public ResponseEntity<?> execute(MockEngineProcessData mockEngineProcessData) throws MockEngineException {
        MockProcess mockProcess = mockEngineProcessData.getMockProcess();
        GraalvmLogger logger = new GraalvmLogger("JsProcess-" + mockProcess.getDisplayName());
        HttpServletRequest request = mockEngineProcessData.getRequest();
        HttpServletResponse response = mockEngineProcessData.getResponse();
        HttpSession session = mockEngineProcessData.getSession();
        Map<String, String[]> parameterMap = mockEngineProcessData.getParameterMap();
        Map<String, String> pathVariable = mockEngineProcessData.getPathVariable();
        Map<String, String> headers = mockEngineProcessData.getHeaders();
        Map<String, String> envResources = mockEngineProcessData.getEnvResources();
        Map<String, String> envionment = mockEngineProcessData.getResponseConfig().getEnvionment();
        Map<String, String> externalFileResources = mockEngineProcessData.getResponseConfig().getExternalFileResources();
        if (externalFileResources == null) {
            externalFileResources = Map.of();
        }
        MockResponseConfig responseConfig = mockEngineProcessData.getResponseConfig();

        CommonRespHelper.CommonRespHelperBuilder respBuilder = packageRespBuilder(mockEngineProcessData);
        GraalJsContextOption.Builder contextBuild = GraalJsContextOption
                .builder(logger)
                .loadMockJs(true)
                .loadAxiosJs(responseConfig.getJsLoadAxios() != null && responseConfig.getJsLoadAxios())
                .parseRequestBody(true)
                .secureMode(mockConfig.getJsEngine().isSecureMode());

        if (mockConfig.getJsEngine().isExportRequest()) {
            contextBuild.accessClass(request.getClass());
            contextBuild.putMember("request", request);
        }
        if (mockConfig.getJsEngine().isExportResponse()) {
            contextBuild.accessClass(response.getClass());
            contextBuild.putMember("response", response);
        }
        if (!mockConfig.getJsEngine().isExportSafeSession()) {
            contextBuild.accessClass(session.getClass());
        }
        contextBuild.accessClass(parameterMap.getClass());
        contextBuild.accessClass(pathVariable.getClass());
        contextBuild.accessClass(headers.getClass());
        contextBuild.accessClass(envResources.getClass());
        contextBuild.accessClass(CommonShareVarHelper.class);
        contextBuild.accessClass(CommonJsonHelper.class);

        if (!mockConfig.getJsEngine().isExportSafeSession()) {
            contextBuild.putMember("session", session);
        } else {
            contextBuild.putMember("session", new CommonSafeSessionHelper(session));
        }
        contextBuild.putMember("envionment", envionment);
        contextBuild.putMember("params", parameterMap);
        contextBuild.putMember("pathVar", pathVariable);
        contextBuild.putMember("pathVariable", pathVariable);
        contextBuild.putMember("headers", headers);
        contextBuild.putMember("resources", envResources);
        contextBuild.putMember("respHelper", respBuilder);
        contextBuild.putMember("logger", logger);
        contextBuild.putMember("requestBody", mockEngineProcessData.getRequestBody());
        contextBuild.putMember("externalFileResources", new ArrayList<>(externalFileResources.keySet()));
        contextBuild.putMember("JsonHelper", new CommonJsonHelper());

        if (mockConfig.getJsEngine().isExportShareVar()) {
            CommonShareVarHelper shareVarHelper = new CommonShareVarHelper(mockProcess.getRuntimeShareVar());
            contextBuild.putMember("shareVar", shareVarHelper);
            contextBuild.putMember("environment", shareVarHelper);
            contextBuild.putMember("env", shareVarHelper);
        }

        if (mockConfig.getJsEngine().isExportParentShareVar()) {
            CommonShareVarHelper shareVarHelper = new CommonShareVarHelper(mockProcess.getRuntimeParentShareVar());
            contextBuild.putMember("parentShareVar", shareVarHelper);
        }

        if (mockConfig.getJsEngine().isExportGlobalShareVar()) {
            CommonShareVarHelper shareVarHelper = new CommonShareVarHelper(mockProcess.getRuntimeGlobalShareVar());
            contextBuild.putMember("globalShareVar", shareVarHelper);
        }

        try (Context context = contextBuild.buildContext()) {
            String runScript = null;
            if (envResources.containsKey("script")) {
                runScript = envResources.get("script");
            } else if (envResources.containsKey("js")) {
                runScript = envResources.get("js");
            }

            if (runScript == null) {
                log.warn("GraalJs执行处理器未解析到脚本数据 MockProcess: {} | 访问路径: {}", mockProcess.getDisplayName(), request.getRequestURI());
                return respBuilder.build().toResponseEntity();
            }

            boolean mockJsObject = runScript.startsWith("({") && runScript.endsWith("})");
            String mergeScript;
            if (mockJsObject) {
                mergeScript = "(function (){respHelper.body(Mock.mock" + runScript + ")})()";
            } else {
                mergeScript = "(function (){" + runScript + "})()";
            }
            try {
                Value ret = context.eval("js", mergeScript);
                if (respBuilder.getBody() == null) {
                    if (!ret.isNull()) {
                        if (ret.isString()) {
                            respBuilder.body(ret.toString());
                        }else{
                            respBuilder.body(ret.as(Object.class));
                        }
                    }
                }
                if(!respBuilder.isFileDownload() && respBuilder.getHeader("Content-Type") == null){
                    respBuilder.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
                }
                return respBuilder.build().toResponseEntity(mockEngineProcessData);
            } catch (Exception e) {
                throw new MockEngineScriptException("GraalJs脚本执行异常", e);
            }

        }
    }
}
