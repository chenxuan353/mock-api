package com.chenxuan353.mock.core.process;

import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.engine.CommonRespHelper;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import com.chenxuan353.mock.core.exception.MockEngineException;
import com.chenxuan353.mock.core.util.StrTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Map;

/**
 * GraalJs执行处理器
 */
@Slf4j
@Component
public class MockTextProcess extends MockProcessAbstract {

    @Override
    public boolean accessType(MockProcessType type) {
        if (MockProcessType.JSON == type) {
            return true;
        }
        if (MockProcessType.Text == type) {
            return true;
        }
        if (MockProcessType.TextTemplate == type) {
            return true;
        }
        if (MockProcessType.Html == type) {
            return true;
        }
        if (MockProcessType.JS_Resource == type) {
            return true;
        }
        if (MockProcessType.CSS == type) {
            return true;
        }
        return MockProcessType.Xml == type;
    }

    @Override
    public ResponseEntity<?> execute(MockEngineProcessData mockEngineProcessData) throws MockEngineException {
        Map<String, File> internalResourceMap = mockEngineProcessData.getMockProcess().getInternalResourceMap();
        CommonRespHelper.CommonRespHelperBuilder respBuilder = packageRespBuilder(mockEngineProcessData);
        MockProcessType engineType = mockEngineProcessData.getProcessType();
        if (MockProcessType.JSON == engineType) {
            respBuilder.contentType("application/json");
            if (respBuilder.getBody() == null && internalResourceMap.containsKey("json")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("json"));
            }
        } else if (MockProcessType.JSONTemplate == engineType) {
            respBuilder.contentType("application/json");
            if (respBuilder.getBody() == null && internalResourceMap.containsKey("json")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("json"));
            }
            if (respBuilder.getBody() != null) {
                respBuilder.body(StrTemplate.replaceVar(mockEngineProcessData.getStrTemplateMap(), respBuilder.getBody()));
            }
        } else if (MockProcessType.Text == engineType) {
            respBuilder.contentType("text/plain; charset=utf-8");
            if (respBuilder.getBody() == null && internalResourceMap.containsKey("txt")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("txt"));
            } else if (respBuilder.getBody() == null && internalResourceMap.containsKey("text")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("text"));
            }
        } else if (MockProcessType.TextTemplate == engineType) {
            respBuilder.contentType("text/plain; charset=utf-8");
            if (respBuilder.getBody() == null && internalResourceMap.containsKey("txt")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("txt"));
            } else if (respBuilder.getBody() == null && internalResourceMap.containsKey("text")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("text"));
            }
            if (respBuilder.getBody() != null) {
                respBuilder.body(StrTemplate.replaceVar(mockEngineProcessData.getStrTemplateMap(), respBuilder.getBody()));
            }
        } else if (MockProcessType.Html == engineType) {
            respBuilder.contentType("text/html; charset=utf-8");
            if (respBuilder.getBody() == null && internalResourceMap.containsKey("html")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("html"));
            } else if (respBuilder.getBody() == null && internalResourceMap.containsKey("htm")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("htm"));
            }
        } else if (MockProcessType.Xml == engineType) {
            respBuilder.contentType("text/xml; charset=utf-8");
            if (respBuilder.getBody() == null && internalResourceMap.containsKey("xml")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("xml"));
            }
        } else if (MockProcessType.CSS == engineType) {
            respBuilder.contentType("text/css");
            if (respBuilder.getBody() == null && internalResourceMap.containsKey("css")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("css"));
            }
        } else if (MockProcessType.JS_Resource == engineType) {
            respBuilder.contentType("text/javascript");
            if (respBuilder.getBody() == null && internalResourceMap.containsKey("js")) {
                respBuilder.body(mockEngineProcessData.getEnvResources().get("js"));
            }
        }
        return respBuilder.build().toResponseEntity();
    }
}
