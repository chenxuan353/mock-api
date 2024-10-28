package com.chenxuan353.mock.core.component;

import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.core.config.MockConfigInterface;
import com.chenxuan353.mock.core.config.MockLogConfig;
import com.chenxuan353.mock.core.config.MockModuleConfig;
import com.chenxuan353.mock.core.config.MockRequestMapping;
import com.chenxuan353.mock.core.config.MockResponseConfig;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Mock模块
 */
@Data
@Builder
public class MockModule implements MockInfoInterface {
    /**
     * 模块关联路径
     */
    private final String dependentPath;
    /**
     * 模块配置
     */
    private MockModuleConfig mockModuleConfig;
    /**
     * 模块根响应组
     */
    private final MockGroup rootGroup;

    public String getName() {
        return rootGroup.getName();
    }


    public String getDisplayName() {
        if (mockModuleConfig != null && StrUtil.isNotEmpty(mockModuleConfig.getName())) {
            return mockModuleConfig.getName();
        }
        return rootGroup.getDisplayName();
    }

    @Override
    public String getDisplayDes() {
        if (mockModuleConfig != null && StrUtil.isNotEmpty(mockModuleConfig.getDes())) {
            return mockModuleConfig.getDes();
        }
        return rootGroup.getDisplayDes();
    }

    @Override
    public MockConfigInterface getProcessConfig() {
        return rootGroup.getProcessConfig();
    }

    @Override
    public String getRequestPath() {
        return rootGroup.getRequestMappingPath();
    }

    @Override
    public String getRequestMappingPath() {
        return rootGroup.getRequestMappingPath();
    }

    @Override
    public MockGroup getParentGroup() {
        return null;
    }

    @Override
    public List<MockProcess> getMockProcesses() {
        return rootGroup.getMockProcesses();
    }

    @Override
    public List<MockProcess> getEnableMockProcesses() {
        return rootGroup.getEnableMockProcesses();
    }

    @Override
    public Map<String, Object> getRuntimeShareVar() {
        return rootGroup.getRuntimeShareVar();
    }

    @Override
    public Map<String, Object> getRuntimeParentShareVar() {
        return rootGroup.getRuntimeShareVar();
    }

    @Override
    public Map<String, Object> getRuntimeGlobalShareVar() {
        return rootGroup.getRuntimeShareVar();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MockModule mockModule)) {
            return false;
        }
        if (getName().equals(mockModule.getName())) {
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public MockResponseConfig getMergeResponseConfig() {
        return rootGroup.getMergeResponseConfig();
    }

    @Override
    public MockRequestMapping getMergeRequestMapping() {
        return rootGroup.getMergeRequestMapping();
    }

    @Override
    public List<MockGroup> getStaticMockGroups() {
        return rootGroup.getStaticMockGroups();
    }

    @Override
    public List<MockGroup> getEnableStaticMockGroups() {
        return rootGroup.getEnableStaticMockGroups();
    }

    @Override
    public MockLogConfig getMockLogConfig() {
        return rootGroup.getMockLogConfig();
    }
}
