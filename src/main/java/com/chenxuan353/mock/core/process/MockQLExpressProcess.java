package com.chenxuan353.mock.core.process;


import cn.hutool.core.collection.CollUtil;
import com.chenxuan353.mock.config.MockConfig;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.consts.MockEngineType;
import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.engine.CommonJsonHelper;
import com.chenxuan353.mock.core.engine.CommonRespHelper;
import com.chenxuan353.mock.core.engine.CommonSafeSessionHelper;
import com.chenxuan353.mock.core.engine.CommonShareVarHelper;
import com.chenxuan353.mock.core.engine.qlexpress.QLExpressLogger;
import com.chenxuan353.mock.core.engine.qlexpress.QLExpressUtil;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import com.chenxuan353.mock.core.exception.MockEngineException;
import com.chenxuan353.mock.core.exception.MockEngineScriptException;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockQLExpressProcess extends MockProcessAbstract {
    private final MockConfig mockConfig;

    @PostConstruct
    protected void init() {
        if (!mockConfig.getQleEngine().isSecureMode() && mockConfig.getEnableEngineList().stream().anyMatch(engineType -> MockEngineType.QLExpress == engineType)) {
            log.warn("QLExpress引擎未在安全模式下运行！");
        }
    }

    @Override
    public boolean accessType(MockProcessType type) {
        if (CollUtil.isEmpty(mockConfig.getEnableEngineList())) {
            return false;
        }
        // 引擎未启用时不处理任何数据
        if (mockConfig.getEnableEngineList().stream().noneMatch(engineType -> MockEngineType.QLExpress == engineType)) {
            return false;
        }
        return MockProcessType.QLExpress == type;
    }

    @Override
    public ResponseEntity<?> execute(MockEngineProcessData mockEngineProcessData) throws MockEngineException {
        MockProcess mockProcess = mockEngineProcessData.getMockProcess();
        QLExpressLogger logger = new QLExpressLogger("Process-" + mockProcess.getDisplayName());
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
        CommonRespHelper.CommonRespHelperBuilder respBuilder = packageRespBuilder(mockEngineProcessData);

        if (mockConfig.getQleEngine().isExportRequest()) {
            QLExpressUtil.addSecuryClass(request.getClass());
        }
        if (mockConfig.getQleEngine().isExportResponse()) {
            QLExpressUtil.addSecuryClass(response.getClass());
        }
        if (!mockConfig.getQleEngine().isExportSafeSession()) {
            QLExpressUtil.addSecuryClass(session.getClass());
        } else {
            QLExpressUtil.addSecuryClass(CommonSafeSessionHelper.class);
        }
        QLExpressUtil.addSecuryClass(parameterMap.getClass());
        QLExpressUtil.addSecuryClass(pathVariable.getClass());
        QLExpressUtil.addSecuryClass(headers.getClass());
        QLExpressUtil.addSecuryClass(envResources.getClass());
        QLExpressUtil.addSecuryClass(respBuilder.getClass());
        QLExpressUtil.addSecuryClass(logger.getClass());
        QLExpressUtil.addSecuryClass(CommonShareVarHelper.class);
        QLExpressUtil.addSecuryClass(CommonJsonHelper.class);


        DefaultContext<String, Object> context = new DefaultContext<>();
        if (mockConfig.getQleEngine().isExportRequest()) {
            context.put("request", request);
        }
        if (mockConfig.getQleEngine().isExportResponse()) {
            context.put("response", response);
        }
        if (!mockConfig.getQleEngine().isExportSafeSession()) {
            context.put("session", session);
        } else {
            context.put("session", new CommonSafeSessionHelper(session));
        }
        context.put("envionment", envionment);
        context.put("params", parameterMap);
        context.put("pathVar", pathVariable);
        context.put("pathVariable", pathVariable);
        context.put("headers", headers);
        context.put("resources", envResources);
        context.put("respHelper", respBuilder);
        context.put("requestBody", mockEngineProcessData.getRequestBody());
        context.put("logger", logger);
        context.put("log", logger);
        context.put("externalFileResources", new ArrayList<>(externalFileResources.keySet()));
        context.put("JsonHelper", new CommonJsonHelper());

        if (mockConfig.getQleEngine().isExportShareVar()) {
            CommonShareVarHelper shareVarHelper = new CommonShareVarHelper(mockProcess.getRuntimeShareVar());
            context.put("shareVar", shareVarHelper);
            context.put("environment", shareVarHelper);
            context.put("env", shareVarHelper);
        }

        if (mockConfig.getQleEngine().isExportParentShareVar()) {
            CommonShareVarHelper shareVarHelper = new CommonShareVarHelper(mockProcess.getRuntimeParentShareVar());
            context.put("parentShareVar", shareVarHelper);
        }

        if (mockConfig.getQleEngine().isExportGlobalShareVar()) {
            CommonShareVarHelper shareVarHelper = new CommonShareVarHelper(mockProcess.getRuntimeGlobalShareVar());
            context.put("globalShareVar", shareVarHelper);
        }

        String runScript = null;
        if (envResources.containsKey("script")) {
            runScript = envResources.get("script");
        } else if (envResources.containsKey("qle")) {
            runScript = envResources.get("qle");
        }

        if (runScript == null) {
            log.warn("QLExpress执行处理器未解析到脚本数据 MockProcess: {} | 访问路径: {}", mockProcess.getDisplayName(), request.getRequestURI());
            return respBuilder.build().toResponseEntity();
        }

        try {
            ExpressRunner runner = new ExpressRunner();
            Object ret = runner.execute(runScript, context, null, true, false, mockConfig.getQleEngine().getRunningTimeLimit());
            if (respBuilder.getBody() == null) {
                if (ret != null) {
                    respBuilder.body(ret.toString());
                }
            }
            if(!respBuilder.isFileDownload() && respBuilder.getHeader("Content-Type") == null){
                respBuilder.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
            }
            return respBuilder.build().toResponseEntity(mockEngineProcessData);
        } catch (Exception e) {
            throw new MockEngineScriptException("QLExpress脚本执行异常", e);
        }
    }
}
