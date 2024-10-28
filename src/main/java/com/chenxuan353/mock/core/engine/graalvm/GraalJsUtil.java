package com.chenxuan353.mock.core.engine.graalvm;

import cn.hutool.core.collection.CollUtil;
import com.chenxuan353.mock.config.MockConfig;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Component
@Slf4j
public class GraalJsUtil {
    private static Source mockJsSource;
    private static Source axiosJsSource;
    private static Source mockDataSource;
    private static Source proxyLogSource;
    private static Source requestBodyDealSource;
    private static MockConfig mockConfig;

    @Autowired
    public void setMockConfig(MockConfig mockConfig) {
        GraalJsUtil.mockConfig = mockConfig;
    }

    @PostConstruct
    protected void utilInit() throws IOException {
        try (Context context = Context.create("js")) {
            context.initialize("js");
        }
        Reader stream = new InputStreamReader(
                Objects.requireNonNull(GraalJsUtil.class.getResourceAsStream("/static/js/mock-min.js"))
        );
        Source.Builder sourceBuilder = Source.newBuilder("js", stream, "mock-min.js");
        mockJsSource = sourceBuilder.build();
        mockDataSource = Source.newBuilder("js", "JSON.stringify(Mock.mock(JSON.parse(jsonData)))", "mockData.js").buildLiteral();
        proxyLogSource = Source.newBuilder("js", """
                    function proxyLog(){
                        let logBk = console.log;
                        return function(){
                            let outStr = "";
                            for (var i = 0; i < arguments.length; i++) {
                                outStr += "{} ";
                            }
                            log.info(outStr.trimEnd(), arguments)
                        }
                    };
                    console.log = proxyLog()
                """, "proxyLog.js").buildLiteral();
        requestBodyDealSource = Source.newBuilder("js", """
                    if(requestBody){
                        try{
                            var requestJson = JSON.parse(requestBody);
                        }catch(e){}
                    }
                """, "requestBodyDeal.js").buildLiteral();
    }

    public static Source getAxiosJsSource() {
        if (axiosJsSource == null) {
            Reader stream = new InputStreamReader(
                    Objects.requireNonNull(GraalJsUtil.class.getResourceAsStream("/static/js/axios.min.js"))
            );
            Source.Builder sourceBuilder = Source.newBuilder("js", stream, "axios.min.js");
            try {
                axiosJsSource = sourceBuilder.build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return axiosJsSource;
    }

    /**
     * 授权访问指定class中所有公共方法
     *
     * @param accessBuild 授权构造器
     * @param clazz       类型
     */
    private static void allowAccessClass(HostAccess.Builder accessBuild, Class<?> clazz) {
        for (Method method : clazz.getMethods()) {
            accessBuild.allowAccess(method);
        }
    }

    public static Context getContext(GraalvmLogger logger, Class<?>... accessClass) {
        return getContext(logger, false, accessClass);
    }

    public static Context getContext(GraalvmLogger logger, boolean loadMockJs, Class<?>... accessClass) {
        return getContext(GraalJsContextOption
                .builder(logger)
                .accessClass(accessClass)
                .loadMockJs(loadMockJs)
                .build()
        );
    }

    public static Context getContext(GraalJsContextOption option) {
        Context.Builder ctxBuilder = Context
                .newBuilder("js");
        if (option.isSecureMode()) {
            HostAccess.Builder accessBuild = HostAccess.newBuilder();
            accessBuild.allowAccessAnnotatedBy(HostAccess.Export.class)
                    .allowImplementationsAnnotatedBy(HostAccess.Implementable.class)
                    .allowImplementationsAnnotatedBy(FunctionalInterface.class);
            List<Class<?>> accessClasses = option.getAccessClasses();
            if (!accessClasses.isEmpty()) {
                accessClasses.stream().distinct().forEach(accessClass -> {
                    allowAccessClass(accessBuild, accessClass);
                });
            }
            ctxBuilder.allowHostAccess(accessBuild.build());
            ctxBuilder.allowCreateThread(false);
            ctxBuilder.allowCreateProcess(false);
            ctxBuilder.allowHostClassLoading(false);
            ctxBuilder.allowHostClassLoading(false);
        }

        if (!mockConfig.getJsEngine().isUseConsoleLog()) {
            ctxBuilder.out(OutputStream.nullOutputStream());
            ctxBuilder.err(OutputStream.nullOutputStream());
        }

        Context context = ctxBuilder.build();

        if (!mockConfig.getJsEngine().isUseConsoleLog()) {
            context.eval(proxyLogSource);
        }
        Value js = context.getBindings("js");
        js.putMember("log", option.getLogger());

        // 初始化变量
        Map<String, Object> putMembers = option.getPutMembers();
        if (CollUtil.isNotEmpty(putMembers)) {
            for (Map.Entry<String, Object> entry : putMembers.entrySet()) {
                js.putMember(entry.getKey(), entry.getValue());
            }
        }

        // 加载资源
        if (option.isLoadMockJs()) {
            context.eval(mockJsSource);
        }
        if (option.isLoadAxiosJs()) {
            context.eval(getAxiosJsSource());
        }
        if (option.isParseRequestBody()) {
            context.eval(requestBodyDealSource);
        }

        return context;
    }

    public static Context getProcess(GraalvmLogger logger, Class<?>... accessClass) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();
        HttpServletResponse response = requestAttributes.getResponse();
        HttpSession session = request.getSession();
        Map<String, String[]> parameterMap = new HashMap<>(request.getParameterMap());
        @SuppressWarnings("unchecked")
        Map<String, String> pathVariable = new HashMap<>((Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE));
        Map<String, String> headers = new HashMap<>();
        for (Iterator<String> it = request.getHeaderNames().asIterator(); it.hasNext(); ) {
            String header = it.next();
            headers.put(header, request.getHeader(header));
        }

        assert response != null;
        GraalJsContextOption.Builder contextBuild = GraalJsContextOption
                .builder(logger)
                .loadMockJs(true);

        contextBuild.accessClass(accessClass);
        contextBuild.accessClass(request.getClass());
        contextBuild.accessClass(response.getClass());
        contextBuild.accessClass(session.getClass());
        contextBuild.accessClass(parameterMap.getClass());
        contextBuild.accessClass(pathVariable.getClass());
        contextBuild.accessClass(headers.getClass());

        contextBuild.putMember("request", request);
        contextBuild.putMember("response", response);
        contextBuild.putMember("session", session);
        contextBuild.putMember("params", parameterMap);
        contextBuild.putMember("pathVariable", pathVariable);
        contextBuild.putMember("headers", headers);

        return getContext(contextBuild.build());
    }

    /**
     * 通过mock字符串直接生成mock数据
     *
     * @param json json数据
     * @return 生成的mock数据
     */
    public static String mock(GraalvmLogger logger, String json) {
        GraalJsContextOption.Builder contextBuild = GraalJsContextOption
                .builder(logger)
                .loadMockJs(true)
                .putMember("jsonData", json);
        try (Context context = getContext(contextBuild.build())) {
            Value ret = context.eval(mockDataSource);
            return ret.toString();
        }
    }

    /**
     * 通过mock字符串直接生成mock数据
     *
     * @param jsObject js对象
     * @return 生成的mock数据
     */
    public static String mockJsObject(GraalvmLogger logger, String jsObject) {
        try (Context context = getProcess(logger)) {
            Value ret = context.eval("js", "JSON.stringify(Mock.mock(" + jsObject + "))");
            return ret.toString();
        }
    }
}
