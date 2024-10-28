package com.chenxuan353.mock.core.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.core.config.MockConfigInterface;
import com.chenxuan353.mock.core.config.MockLogConfig;
import com.chenxuan353.mock.core.config.MockProcessConfig;
import com.chenxuan353.mock.core.config.MockRequestMapping;
import com.chenxuan353.mock.core.config.MockResponseConfig;
import com.chenxuan353.mock.core.consts.MockProcessConsts;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.chenxuan353.mock.core.consts.MockProcessConsts.INTERNAL_RESOURCE_EXT;

/**
 * Mock处理器
 */
@Slf4j
@Data
public class MockProcess implements MockInfoInterface {

    /**
     * 处理器名称
     */
    private final String name;
    /**
     * 处理器配置
     */
    private MockProcessConfig mockProcessConfig;
    /**
     * 所属响应组
     */
    @ToString.Exclude
    private MockGroup parentGroup;
    /**
     * 处理器关联文件列表
     * yml、js、body、json、data、text
     */
    private final List<File> files;
    private final Map<String, File> internalResourceMap;
    private final Map<String, Object> runtimeShareVar = new HashMap<>();

    private MockRequestMapping mergeRequestMapping;
    private final boolean staticMode;

    public MockProcess(String name, List<File> files, boolean staticMode) {
        this.name = name;
        this.files = files;
        this.staticMode = staticMode;
        internalResourceMap = new HashMap<>();
        // 收集内部资源Map
        for (File file : files) {
            for (String ext : INTERNAL_RESOURCE_EXT) {
                if ((name + "." + ext).equalsIgnoreCase(file.getName())) {
                    internalResourceMap.put(ext, file);
                    break;
                }
            }
        }
    }

    @Override
    public String getDependentPath() {
        if (parentGroup == null) {
            return null;
        }
        return parentGroup.getDependentPath();
    }

    /**
     * 通过相对路径信息获取可用的资源文件
     *
     * @param resourcesPath 外部资源路径
     * @return 资源文件路径
     */
    public String getExternalTextResourcesExistPath(String resourcesPath) {
        if (FileUtil.isAbsolutePath(resourcesPath)) {
            return resourcesPath;
        }
        File resourcesFile = Paths.get(getDependentPath(), resourcesPath).toFile();
        if (!FileUtil.isFile(resourcesFile)) {
            resourcesFile = new File(resourcesPath);
        }
        if (FileUtil.isFile(resourcesFile)) {
            return resourcesFile.getAbsolutePath();
        }
        return null;
    }

    /**
     * 从路径中以UTF-8编码读取外部资源 非绝对路径时，优先以关联路径为基础路径查找
     *
     * @param resourcesPath 外部资源路径
     * @return 资源文本
     */
    public String readExternalTextResourcesFromPath(String resourcesPath) {
        String resourcesExistPath = getExternalTextResourcesExistPath(resourcesPath);
        if (resourcesExistPath != null) {
            File file = new File(resourcesExistPath);
            long length = file.length();
            if (length > MockProcessConsts.SCRIPT_FILE_SIZE_LIMIT) {
                log.warn("资源文件过大，已跳过加载：{} | MockProcess: {} | DependentPath: {}", resourcesExistPath, this.getName(), this.getDependentPath());
                return null;
            }
            return FileUtil.readString(resourcesExistPath, StandardCharsets.UTF_8);
        }
        return null;
    }

    /**
     * 读取body
     *
     * @return body数据
     */
    public String getResourceBody() {
        if (internalResourceMap.containsKey("body")) {
            return FileUtil.readString(internalResourceMap.get("body"), StandardCharsets.UTF_8);
        }
        MockResponseConfig mergeResponseConfig = getMergeResponseConfig();
        if (mergeResponseConfig.getBody() != null) {
            return mergeResponseConfig.getBody();
        }
        return null;
    }

    /**
     * 读取所有关联的资源
     *
     * @return 资源地图
     */
    public Map<String, String> getResourceMap() {
        Map<String, String> resourceMap = new HashMap<>();
        // 加载内部资源
        for (Map.Entry<String, File> entry : internalResourceMap.entrySet()) {
            resourceMap.put(entry.getKey(), FileUtil.readString(entry.getValue(), StandardCharsets.UTF_8));
        }
        // 加载外部资源
        MockResponseConfig mergeResponseConfig = getMergeResponseConfig();
        if (!resourceMap.containsKey("body") && mergeResponseConfig.getBody() != null) {
            resourceMap.put("body", mergeResponseConfig.getBody());
        }
        if (!resourceMap.containsKey("script") && StrUtil.isNotEmpty(mergeResponseConfig.getScriptPath())) {
            resourceMap.put("script", readExternalTextResourcesFromPath(mergeResponseConfig.getScriptPath()));
        }
        if (!resourceMap.containsKey("script") && StrUtil.isNotEmpty(mergeResponseConfig.getScriptContent())) {
            resourceMap.put("script", mergeResponseConfig.getScriptContent());
        }
        Map<String, String> externalTextResources = mergeResponseConfig.getExternalTextResources();
        if (externalTextResources != null) {
            for (Map.Entry<String, String> entry : externalTextResources.entrySet()) {
                String key = entry.getKey();
                if (resourceMap.containsKey(key)) {
                    continue;
                }
                String value = entry.getValue();
                String resourcesData = readExternalTextResourcesFromPath(value);
                if (resourcesData != null) {
                    resourceMap.put(key, resourcesData);
                }
            }
        }
        return resourceMap;
    }

    public String getDisplayName() {
        if (mockProcessConfig != null && StrUtil.isNotEmpty(mockProcessConfig.getName())) {
            return mockProcessConfig.getName();
        }
        return name;
    }

    @Override
    public String getDisplayDes() {
        if (mockProcessConfig != null && StrUtil.isNotEmpty(mockProcessConfig.getDes())) {
            return mockProcessConfig.getDes();
        }
        return "";
    }

    @Override
    public MockConfigInterface getProcessConfig() {
        return mockProcessConfig;
    }

    @Override
    public List<MockProcess> getMockProcesses() {
        return List.of(this);
    }

    @Override
    public List<MockProcess> getEnableMockProcesses() {
        if (mockProcessConfig != null && mockProcessConfig.isEnable()) {
            return List.of();
        }
        return List.of(this);
    }

    @Override
    public MockRequestMapping getMergeRequestMapping() {
        if (mergeRequestMapping == null) {
            mergeRequestMapping = MockInfoInterface.super.getMergeRequestMapping();
        }
        return mergeRequestMapping;
    }

    @SuppressWarnings("unused")
    public void setMergeRequestMapping(MockRequestMapping mergeRequestMapping) {
        throw new RuntimeException("Unable to set mergeRequestMapping!");
    }

    @Override
    public List<MockGroup> getStaticMockGroups() {
        return List.of();
    }

    @Override
    public List<MockGroup> getEnableStaticMockGroups() {
        return List.of();
    }

    @Override
    public MockLogConfig getMockLogConfig(){
        if(mockProcessConfig != null && mockProcessConfig.getLog() != null){
            return mockProcessConfig.getLog();
        }
        MockLogConfig parentMockLogConfig = parentGroup.getMockLogConfig();
        if(parentMockLogConfig != null){
            return parentMockLogConfig;
        }
        return new MockLogConfig();
    }
}
