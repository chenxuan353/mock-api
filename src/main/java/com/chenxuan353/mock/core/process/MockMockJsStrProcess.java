package com.chenxuan353.mock.core.process;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.ContentType;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.engine.CommonRespHelper;
import com.chenxuan353.mock.core.engine.graalvm.GraalJsUtil;
import com.chenxuan353.mock.core.engine.graalvm.GraalvmLogger;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import com.chenxuan353.mock.core.exception.MockEngineException;
import com.chenxuan353.mock.core.util.StrTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MockJsStr执行处理器
 */
@Slf4j
@Component
public class MockMockJsStrProcess extends MockProcessAbstract {

    @Override
    public boolean accessType(MockProcessType type) {
        return MockProcessType.MockJsStr == type;
    }

    @Override
    public ResponseEntity<?> execute(MockEngineProcessData mockEngineProcessData) throws MockEngineException {
        Map<String, String> envResources = mockEngineProcessData.getEnvResources();
        MockProcess mockProcess = mockEngineProcessData.getMockProcess();
        GraalvmLogger logger = new GraalvmLogger("MockJsStrProcess-" + mockProcess.getDisplayName());
        CommonRespHelper.CommonRespHelperBuilder respBuilder = packageRespBuilder(mockEngineProcessData);
        String mockData = respBuilder.getBody();
        if (envResources.containsKey("script")) {
            mockData = envResources.get("script");
        } else if (envResources.containsKey("json")) {
            mockData = envResources.get("json");
        }
        if (mockData == null) {
            log.warn("MockJsStr执行处理器未解析到Mock数据 MockProcess: {} | 路径: {}", mockProcess.getDisplayName(), mockEngineProcessData.getRequest().getRequestURI());
            return respBuilder.build().toResponseEntity();
        }
        respBuilder.contentType(ContentType.JSON.getValue());
        String body = GraalJsUtil.mock(logger, mockData);
        if (StrUtil.isNotEmpty(body)) {
            body = StrTemplate.replaceVar(mockEngineProcessData.getStrTemplateMap(), body);
        }
        respBuilder.body(body);
        return respBuilder.build().toResponseEntity();
    }
}
