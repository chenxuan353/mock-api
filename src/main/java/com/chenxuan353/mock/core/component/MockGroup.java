package com.chenxuan353.mock.core.component;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.core.config.MockConfigInterface;
import com.chenxuan353.mock.core.config.MockGroupConfig;
import com.chenxuan353.mock.core.config.MockLogConfig;
import com.chenxuan353.mock.core.exception.MockNotDirectoryException;
import com.chenxuan353.mock.core.exception.MockRootGroupSetParentException;
import lombok.Data;
import lombok.ToString;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Mock响应组
 */
@Data
public class MockGroup implements MockInfoInterface {
    /**
     * 响应组关联路径
     */
    private String dependentPath;
    /**
     * 响应组名称
     */
    private String name;
    /**
     * 响应组配置
     */
    private MockGroupConfig mockGroupConfig;
    /**
     * 是否属于根响应组，属于根响应组时无法设置父响应组
     */
    private boolean isRoot;
    /**
     * 父响应组
     */
    @ToString.Exclude
    private MockGroup parentGroup;
    /**
     * 响应组列表
     */
    private List<MockGroup> groups = new ArrayList<>();
    /**
     * 处理器列表
     */
    private List<MockProcess> processes = new ArrayList<>();
    /**
     * 运行时共享变量
     */
    private final Map<String, Object> runtimeShareVar = new HashMap<>();
    private boolean staticMode = false;

    public MockGroup(File directory, boolean isRoot) {
        if (!directory.isDirectory()) {
            throw new MockNotDirectoryException();
        }
        this.name = directory.getName();
        this.dependentPath = FileUtil.getAbsolutePath(directory);
        this.isRoot = isRoot;
    }

    public MockGroup(File directory) {
        this(directory, false);
    }

    public String getDisplayName() {
        if (mockGroupConfig != null && StrUtil.isNotEmpty(mockGroupConfig.getName())) {
            return mockGroupConfig.getName();
        }
        return name;
    }

    @Override
    public String getDisplayDes() {
        if (mockGroupConfig != null && StrUtil.isNotEmpty(mockGroupConfig.getDes())) {
            return mockGroupConfig.getDes();
        }
        return "";
    }

    @Override
    public MockConfigInterface getProcessConfig() {
        return mockGroupConfig;
    }

    public void setParentGroup(MockGroup parentGroup) {
        if (isRoot) {
            throw new MockRootGroupSetParentException();
        }
        this.parentGroup = parentGroup;
    }


    @Override
    public List<MockProcess> getMockProcesses() {
        List<MockProcess> mockProcesses = new ArrayList<>();
        for (MockGroup group : groups) {
            mockProcesses.addAll(group.getMockProcesses());
        }
        mockProcesses.addAll(processes);
        return mockProcesses;
    }

    @Override
    public List<MockProcess> getEnableMockProcesses() {
        if (mockGroupConfig != null && !mockGroupConfig.isEnable()) {
            return List.of();
        }
        List<MockProcess> mockProcesses = new ArrayList<>();
        for (MockGroup group : groups) {
            mockProcesses.addAll(group.getEnableMockProcesses());
        }
        for (MockProcess process : processes) {
            if (process.getMockProcessConfig() != null && !process.getMockProcessConfig().isEnable()) {
                continue;
            }
            mockProcesses.add(process);
        }
        return mockProcesses;
    }

    @Override
    public List<MockGroup> getStaticMockGroups() {
        List<MockGroup> mockStaticGroups = new ArrayList<>(3);
        for (MockGroup group : getGroups()) {
            if (group.isStaticMode()) {
                mockStaticGroups.add(group);
            } else {
                List<MockGroup> subMockStaticGroups = group.getStaticMockGroups();
                mockStaticGroups.addAll(subMockStaticGroups);
            }
        }
        return mockStaticGroups;
    }

    @Override
    public List<MockGroup> getEnableStaticMockGroups() {
        List<MockGroup> mockStaticGroups = new ArrayList<>(3);
        if (mockGroupConfig != null && !mockGroupConfig.isEnable()) {
            return List.of();
        }
        for (MockGroup group : groups) {
            if (group.getMockGroupConfig() != null && !group.getMockGroupConfig().isEnable()) {
                continue;
            }
            if (group.isStaticMode()) {
                mockStaticGroups.add(group);
            } else {
                List<MockGroup> subMockStaticGroups = group.getStaticMockGroups();
                mockStaticGroups.addAll(subMockStaticGroups);
            }
        }
        return mockStaticGroups;
    }

    @Override
    public MockLogConfig getMockLogConfig() {
        if(mockGroupConfig != null && mockGroupConfig.getLog() != null){
            return mockGroupConfig.getLog();
        }
        if(getParentGroup() != null){
            return getParentGroup().getMockLogConfig();
        }
        return null;
    }
}
