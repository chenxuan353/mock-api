package com.chenxuan353.mock.core.component;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.core.config.MockConfigInterface;
import com.chenxuan353.mock.core.config.MockLogConfig;
import com.chenxuan353.mock.core.config.MockRequestMapping;
import com.chenxuan353.mock.core.config.MockResponseConfig;
import com.chenxuan353.mock.core.exception.MockRootGroutNotFoundException;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chenxuan353.mock.core.consts.MockProcessConsts.URL_SPLIT;

public interface MockInfoInterface {
    /**
     * 获取标识名称
     *
     * @return 标识名称
     */
    String getName();

    /**
     * 获取依赖路径
     *
     * @return 依赖路径
     */
    String getDependentPath();

    /**
     * 获取展示名称
     *
     * @return 展示名称
     */
    String getDisplayName();

    /**
     * 获取展示描述
     *
     * @return 展示描述
     */
    String getDisplayDes();

    /**
     * 获取父响应组
     *
     * @return 响应组
     */
    MockGroup getParentGroup();

    /**
     * 获取关联的处理器配置
     *
     * @return 处理器配置
     */
    MockConfigInterface getProcessConfig();

    /**
     * 获取处理器列表
     *
     * @return 处理器列表
     */
    List<MockProcess> getMockProcesses();

    /**
     * 获取启用的处理器列表
     *
     * @return 处理器列表
     */
    List<MockProcess> getEnableMockProcesses();

    /**
     * 获取静态响应组列表
     *
     * @return 静态响应组列表
     */
    List<MockGroup> getStaticMockGroups();

    /**
     * 获取启用的静态响应组列表
     *
     * @return 静态响应组列表
     */
    List<MockGroup> getEnableStaticMockGroups();

    Map<String, Object> getRuntimeShareVar();
    MockLogConfig getMockLogConfig();

    default Map<String, Object> getRuntimeParentShareVar() {
        MockGroup parentGroup = getParentGroup();
        if (parentGroup == null) {
            return getRuntimeShareVar();
        }
        return parentGroup.getRuntimeShareVar();
    }

    default Map<String, Object> getRuntimeGlobalShareVar() {
        MockGroup rootGroup = getRootGroup();
        if (rootGroup == null) {
            return getRuntimeShareVar();
        }
        return rootGroup.getRuntimeShareVar();
    }

    default MockGroup getRootGroup() {
        MockGroup parentGroup = getParentGroup();
        if (parentGroup == null) {
            if (this instanceof MockGroup mockGroup) {
                return mockGroup;
            }
            throw new MockRootGroutNotFoundException();
        }
        while (!parentGroup.isRoot() && parentGroup.getRootGroup() != null) {
            parentGroup = parentGroup.getRootGroup();
            if (parentGroup == parentGroup.getRootGroup()) {
                return parentGroup;
            }
        }
        return parentGroup;
    }

    /**
     * 获取当前配置的请求路径
     *
     * @return 请求路径
     */
    default String getRequestPath() {
        if (getProcessConfig() != null && getProcessConfig().getRequest() != null) {
            String configRequestPath = getProcessConfig().getRequest().getPath();
            if (StrUtil.isNotEmpty(configRequestPath)) {
                if (configRequestPath.endsWith(URL_SPLIT)) {
                    configRequestPath = configRequestPath.substring(0, configRequestPath.length() - 1);
                }
                return configRequestPath;
            }
        }
        // 根响应组默认为""
        if (getParentGroup() == null) {
            return "/";
        }
        MockRequestMapping mergeRequestMapping = getMergeRequestMapping();
        Boolean disablePathConvert = mergeRequestMapping.getDisablePathConvert();
        if (disablePathConvert != null && disablePathConvert) {
            return getName();
        }
        return getName().replace(".", URL_SPLIT).replace("//", ".");
    }

    /**
     * 获取完整请求路径
     *
     * @return 请求路径
     */
    default String getRequestMappingPath() {
        String requestPath = getRequestPath();
        // 以`/`起始时无视上级配置
        if (requestPath.startsWith(URL_SPLIT)) {
            return requestPath;
        }
        // 合并父路径(一定以`/`起始)
        if (getParentGroup() != null) {
            String requestMappingPath = getParentGroup().getRequestMappingPath();
            if (StrUtil.isNotEmpty(requestMappingPath) && !URL_SPLIT.equals(requestMappingPath)) {
                requestPath = requestMappingPath + URL_SPLIT + requestPath;
            }
        }
        // 返回前检查前缀与后缀
        if (!requestPath.startsWith(URL_SPLIT)) {
            requestPath = URL_SPLIT + requestPath;
        }
        if (requestPath.endsWith(URL_SPLIT)) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }
        return requestPath;
    }

    /**
     * 获取合并默认配置的响应配置
     *
     * @return 响应配置(不为空)
     */
    default MockResponseConfig getMergeResponseConfig() {
        MockResponseConfig nowResponseConfig = new MockResponseConfig();
        if (getParentGroup() != null) {
            nowResponseConfig = getParentGroup().getMergeResponseConfig();
        }
        if (getProcessConfig() != null && getProcessConfig().getResponse() != null) {
            // 合并数据时特殊处理
            MockResponseConfig response = getProcessConfig().getResponse();
            Map<String, String> externalTextResources = response.getExternalTextResources();
            if (externalTextResources != null) {
                // 对ExternalTextResources进行特殊处理(合并依赖路径)
                Map<String, String> dealExternalTextResources = new HashMap<>();
                for (Map.Entry<String, String> stringStringEntry : externalTextResources.entrySet()) {
                    String path = stringStringEntry.getValue();
                    if (!FileUtil.isAbsolutePath(path)) {
                        path = Paths.get(getDependentPath(), path).toString();
                    }
                    dealExternalTextResources.put(stringStringEntry.getKey(), path);
                }
                response.setExternalTextResources(dealExternalTextResources);
            }
            BeanUtil.copyProperties(response, nowResponseConfig, CopyOptions.create().ignoreNullValue());
        }
        return nowResponseConfig;
    }

    /**
     * 获取合并默认配置的请求配置
     *
     * @return 请求配置(不为空)
     */
    default MockRequestMapping getMergeRequestMapping() {
        MockRequestMapping nowRequestMapping = new MockRequestMapping();
        if (getParentGroup() != null) {
            nowRequestMapping = getParentGroup().getMergeRequestMapping();
        }
        if (getProcessConfig() != null && getProcessConfig().getRequest() != null) {
            BeanUtil.copyProperties(getProcessConfig().getRequest(), nowRequestMapping, CopyOptions.create().ignoreNullValue());
        }
        return nowRequestMapping;
    }

}
