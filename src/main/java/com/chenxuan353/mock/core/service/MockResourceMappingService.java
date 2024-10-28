package com.chenxuan353.mock.core.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.core.component.MockGroup;
import com.chenxuan353.mock.core.component.MockModule;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.util.UrlPathHelper;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock静态资源处理
 */
@RequiredArgsConstructor
@Service
public class MockResourceMappingService {
    private final ApplicationContext applicationContext;
    private final ServletContext servletContext;
    private UrlPathHelper mvcUrlPathHelper;
    private ContentNegotiationManager mvcContentNegotiationManager;
    private HandlerMapping resourceHandlerMapping;
    private Map<String, MockGroup> nowResourceMapping = new HashMap<>();

    @Resource(name = "mvcUrlPathHelper")
    public void setMvcUrlPathHelper(UrlPathHelper mvcUrlPathHelper) {
        this.mvcUrlPathHelper = mvcUrlPathHelper;
    }

    @Resource(name = "mvcContentNegotiationManager")
    public void setMvcContentNegotiationManager(ContentNegotiationManager mvcContentNegotiationManager) {
        this.mvcContentNegotiationManager = mvcContentNegotiationManager;
    }

    @Resource(name = "resourceHandlerMapping")
    public void setResourceHandlerMapping(HandlerMapping resourceHandlerMapping) {
        this.resourceHandlerMapping = resourceHandlerMapping;
    }

    /**
     * 更新静态资源表
     *
     * @param resourceMapping 新的静态资源表
     */
    private void flushStatisResource(Map<String, MockGroup> resourceMapping) {
        // 这里存放的是springmvc已经建立好的映射处理
        @SuppressWarnings("unchecked") final Map<String, Object> handlerMap = (Map<String, Object>) ReflectUtil.getFieldValue(resourceHandlerMapping,
                "handlerMap");

        final ResourceHandlerRegistry resourceHandlerRegistry = new ResourceHandlerRegistry(applicationContext,
                servletContext, mvcContentNegotiationManager, mvcUrlPathHelper);

        // 移除曾经定义的静态资源
        for (String urlPath : nowResourceMapping.keySet()) {
            final String urlPathDealed = StrUtil.appendIfMissing(urlPath, "/**");
            handlerMap.remove(urlPathDealed);
        }

        // 注册新的静态资源
        for (Map.Entry<String, MockGroup> entry : resourceMapping.entrySet()) {
            String urlPath = entry.getKey();
            MockGroup staticMockGroup = entry.getValue();
            final String urlPathDealed = StrUtil.appendIfMissing(urlPath, "/**");
            final String dependentPath = staticMockGroup.getDependentPath();
            final String absoluteDependentPath = FileUtil.getAbsolutePath(new File(dependentPath));
            final String resourceLocationsDealed = StrUtil.appendIfMissing(absoluteDependentPath, "/");
            handlerMap.remove(urlPathDealed); // 以防万一，还是清理一下
            resourceHandlerRegistry.addResourceHandler(urlPathDealed)
                    .addResourceLocations("file:" + resourceLocationsDealed);
        }

        final Map<String, ?> additionalUrlMap = ReflectUtil
                .<SimpleUrlHandlerMapping>invoke(resourceHandlerRegistry, "getHandlerMapping").getUrlMap();

        ReflectUtil.<Void>invoke(resourceHandlerMapping, "registerHandlers", additionalUrlMap);
        nowResourceMapping = resourceMapping;
    }

    public Map<String, MockGroup> getResourceMapping() {
        return nowResourceMapping;
    }

    public void initResourceMapping(MockModule mockModule) {
        if (mockModule == null) {
            return;
        }
        Map<String, MockGroup> resourceMapping = new HashMap<>();
        for (MockGroup staticMockGroup : mockModule.getEnableStaticMockGroups()) {
            String requestMappingPath = staticMockGroup.getRequestMappingPath();
            resourceMapping.put(requestMappingPath, staticMockGroup);
        }
        flushStatisResource(resourceMapping);
    }

    /**
     * 清空资源映射
     */
    public void clearResourceMapping() {
        flushStatisResource(new HashMap<>());
    }

    /**
     * 重载资源映射
     */
    public void reloadResourceMapping(MockModule mockModule) {
        if (mockModule == null) {
            clearResourceMapping();
            return;
        }
        initResourceMapping(mockModule);
    }

}
