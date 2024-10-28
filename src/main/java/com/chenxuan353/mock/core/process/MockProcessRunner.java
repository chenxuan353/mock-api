package com.chenxuan353.mock.core.process;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.config.MockLogConfig;
import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.consts.MockRespError;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import com.chenxuan353.mock.core.exception.MockEngineException;
import com.chenxuan353.mock.util.RespUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import org.springframework.web.servlet.HandlerMapping;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.chenxuan353.mock.core.consts.MockProcessConsts.MOCK_REQUEST_MARK;
import static com.chenxuan353.mock.core.consts.MockRespError.MOCK_PROCESS_UNKNOW_PROCESS_TYPE;

/**
 * 处理器运行器，用于运行相关处理
 */
@Slf4j
@Component
public class MockProcessRunner implements ApplicationContextAware {

    /**
     * 用于保存接口实现类名及对应的类
     */
    private Map<String, ? extends MockProcessAbstract> processMap;

    /**
     * 获取应用上下文并获取相应的接口实现类
     *
     * @param applicationContext 应用上下文
     * @throws BeansException 异常
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        //根据接口类型返回相应的所有bean
        processMap = applicationContext.getBeansOfType(MockProcessAbstract.class);
    }

    private MockEngineProcessData packageData(MockProcess mockProcess, HttpServletRequest request, HttpServletResponse response, String requestBody) {
        HttpSession session = request.getSession();
        Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariable = new HashMap<>((Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
        Map<String, String> headers = new HashMap<>();
        for (Iterator<String> it = request.getHeaderNames().asIterator(); it.hasNext(); ) {
            String header = it.next();
            headers.put(header, request.getHeader(header));
        }
        MockEngineProcessData.MockEngineProcessDataBuilder dataBuilder = MockEngineProcessData.builder();
        return dataBuilder.mockProcess(mockProcess)
                .request(request)
                .response(response)
                .headers(headers)
                .pathVariable(pathVariable)
                .parameterMap(parameterMap)
                .session(session)
                .requestBody(requestBody)
                .build();
    }

    private void logConfigDeal(MockEngineProcessData mockEngineProcessData, ResponseEntity<?> responseEntity, double processTimeSec) {
        MockProcess mockProcess = mockEngineProcessData.getMockProcess();
        MockLogConfig logConfig = mockProcess.getMockLogConfig();
        if (!logConfig.isEnable()) {
            return;
        }
        HttpServletRequest request = mockEngineProcessData.getRequest();
        StringBuilder sb = new StringBuilder();
        sb.append("======MockProcess Log======\n");
        sb.append("name: ").append(mockProcess.getDisplayName()).append("\n");
        sb.append("method: ").append(request.getMethod()).append("\n");
        sb.append("path: ").append(request.getRequestURI()).append("\n");
        sb.append("processType: ").append(mockEngineProcessData.getProcessType()).append("\n");
        if (logConfig.isPrintProcessTime()) {
            sb.append("processTime: ").append(String.format("%.2f", processTimeSec)).append("s\n");
        }
        // 输出请求
        if (logConfig.isPrintRequest()) {
            sb.append("---Request---\n");
            if (logConfig.isPrintHeaders()) {
                Map<String, String> headers = mockEngineProcessData.getHeaders();
                if (!headers.isEmpty()) {
                    sb.append("headers: ").append(JSON.toJSONString(headers)).append("\n");
                }
            }
            if (logConfig.isPrintUriVars()) {
                Map<String, String> pathVariable = mockEngineProcessData.getPathVariable();
                if (!pathVariable.isEmpty()) {
                    sb.append("pathVariable: ").append(JSON.toJSONString(pathVariable)).append("\n");
                }
            }
            if (logConfig.isPrintParam()) {
                Map<String, String[]> parameterMap = mockEngineProcessData.getParameterMap();
                if (!parameterMap.isEmpty()) {
                    sb.append("parameterMap: ").append(JSON.toJSONString(parameterMap)).append("\n");
                }
            }
            if (logConfig.isPrintSession()) {
                List<String> printSessionKey = logConfig.getPrintSessionKey();
                if (CollUtil.isNotEmpty(printSessionKey)) {
                    HttpSession session = mockEngineProcessData.getSession();
                    Map<String, String> sessionKeys = new HashMap<>();
                    for (String sessionKey : printSessionKey) {
                        Object attribute = session.getAttribute(sessionKey);
                        if (attribute != null) {
                            sessionKeys.put(sessionKey, attribute.toString());
                        }
                    }
                    if (!sessionKeys.isEmpty()) {
                        sb.append("sessionKeys: ").append(JSON.toJSONString(sessionKeys)).append("\n");
                    }
                }
            }
            if (logConfig.isPrintRequestBody()) {
                String requestBody = mockEngineProcessData.getRequestBody();
                if (StrUtil.isNotEmpty(requestBody)) {
                    sb.append("requestBody: ").append(JSON.toJSONString(requestBody)).append("\n");
                }
            }
        }

        // 输出响应
        if (logConfig.isPrintResponse() && responseEntity != null) {
            sb.append("---Response---\n");
            if (logConfig.isPrintResponseHeaders()) {
                HttpHeaders headers = responseEntity.getHeaders();
                Map<String, String> singleValueMap = headers.toSingleValueMap();
                if (!singleValueMap.isEmpty()) {
                    sb.append("headers: ").append(JSON.toJSONString(singleValueMap)).append("\n");
                }
            }
            if (logConfig.isPrintResponseBody()) {
                Object body = responseEntity.getBody();
                if (body instanceof String) {
                    sb.append("body: ").append(body).append("\n");
                } else {
                    try {
                        sb.append("body: ").append(JSON.toJSONString(body)).append("\n");
                    } catch (Exception e) {
                        log.info("响应的body转换失败，异常信息摘要: {}", e.getMessage());
                        sb.append("body: ").append(body).append("\n");
                    }
                }
            }
        }
        log.info(sb.substring(0, sb.length() - 1));
    }

    public static MockProcessType getProcessType(MockProcessType processType, String scriptPath, Map<String, File> internalResourceMap) {
        if (StrUtil.isNotEmpty(scriptPath)) {
            processType = switch (FileNameUtil.extName(scriptPath).toLowerCase()) {
                case "js" -> MockProcessType.JS;
                case "json" -> MockProcessType.MockJsStr;
                case "qle" -> MockProcessType.QLExpress;
                default -> null;
            };
        }
        if (processType != null) {
            return processType;
        }

        if (internalResourceMap.containsKey("qle")) {
            processType = MockProcessType.QLExpress;
        } else if (internalResourceMap.containsKey("js")) {
            processType = MockProcessType.JS;
        } else if (internalResourceMap.containsKey("json")) {
            processType = MockProcessType.MockJsStr;
        } else if (internalResourceMap.containsKey("html")) {
            processType = MockProcessType.Html;
        } else if (internalResourceMap.containsKey("xml")) {
            processType = MockProcessType.Xml;
        } else if (internalResourceMap.containsKey("css")) {
            processType = MockProcessType.CSS;
        } else if (internalResourceMap.containsKey("txt") || internalResourceMap.containsKey("text")) {
            processType = MockProcessType.TextTemplate;
        }
        return processType;
    }

    private Throwable getRootCause(Throwable e) {
        Throwable cause = null;
        Throwable result = e;

        while (null != (cause = result.getCause()) && (result != cause)) {
            result = cause;
        }
        return result;
    }

    /**
     * 运行响应处理器
     *
     * @param mockProcess 关联响应处理器配置
     * @param request     请求
     * @param response    响应
     * @return 响应体
     */
    public ResponseEntity<?> process(MockProcess mockProcess, HttpServletRequest request, HttpServletResponse response, String requestBody) {
        request.setAttribute(MOCK_REQUEST_MARK, true);
        MockEngineProcessData mockEngineProcessData = packageData(mockProcess, request, response, requestBody);
        if (mockEngineProcessData.getProcessType() == null) {
            log.warn("响应器处理类型识别失败！ MockProcess: {} | 路径: {}", mockProcess.getDisplayName(), mockEngineProcessData.getRequest().getRequestURI());
            return RespUtil.error(MOCK_PROCESS_UNKNOW_PROCESS_TYPE);
        }
        StopWatch sw = new StopWatch("process");
        sw.start();
        for (MockProcessAbstract process : processMap.values()) {
            if (process.accessType(mockEngineProcessData.getProcessType())) {
                ResponseEntity<?> ret;
                try {
                    ret = process.execute(mockEngineProcessData);
                } catch (MockEngineException e) {
                    log.error("运行响应处理器时异常！MockProcess: " + mockProcess.getDisplayName() + " | 路径: " + request.getRequestURI(), e);
                    String dependentAbsolutePath = FileUtil.getAbsolutePath(new File(mockProcess.getDependentPath()));
                    Map<String, String> exceptionInfo = new HashMap<>();
                    exceptionInfo.put("processName", mockProcess.getName());
                    exceptionInfo.put("dependentPath", dependentAbsolutePath);
                    exceptionInfo.put("exceptionMsg", e.getMessage());
                    exceptionInfo.put("exceptionRootCauseMsg", getRootCause(e).getMessage());
                    ret = RespUtil.error(MockRespError.MOCK_PROCESS_RUNTIME_EXCEPTION, exceptionInfo);
                }
                sw.stop();
                logConfigDeal(mockEngineProcessData, ret, sw.lastTaskInfo().getTimeSeconds());
                return ret;
            }
        }
        log.warn("{} 未找到可供运行的响应处理器！ MockProcess: {} | 路径: {}", mockEngineProcessData.getProcessType(), mockProcess.getDisplayName(), mockEngineProcessData.getRequest().getRequestURI());
        return RespUtil.error(MockRespError.MOCK_PROCESS_NOT_FOUND);
    }
}
