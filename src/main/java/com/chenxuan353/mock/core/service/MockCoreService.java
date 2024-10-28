package com.chenxuan353.mock.core.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.config.MockConfig;
import com.chenxuan353.mock.core.component.MockCoreData;
import com.chenxuan353.mock.core.component.MockModule;
import com.chenxuan353.mock.core.entity.ModuleSimpleInfo;
import com.chenxuan353.mock.core.exception.MockModuleNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Mock核心数据处理服务
 * 提供Mock核心数据处理功能
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MockCoreService {
    /**
     * Mock配置
     */
    private final MockConfig mockConfig;
    private final MockFileService mockFileService;
    /**
     * Mock数据
     */
    private MockCoreData mockCoreData;
    /**
     * 当前设置模块
     */
    private MockModule settingActivictMockModule;

    /**
     * 通过模块名称激活特点模块
     *
     * @param moduleName 模块名
     * @return 是否激活了新模块
     */
    public boolean activictMockModule(String moduleName) {
        Optional<MockModule> module = mockCoreData.getModuleList()
                .stream()
                .filter(m -> m.getName().equals(moduleName) || m.getDisplayName().equals(moduleName))
                .findFirst();
        if (module.isEmpty()) {
            throw new MockModuleNotFoundException("模块 " + moduleName + " 不存在！");
        }

        MockModule mockModule = module.get();
        if (getActivictMockModule() == mockModule) {
            return false;
        }
        settingActivictMockModule = mockModule;
        return true;
    }

    /**
     * 获取当前使用的模块
     *
     * @return 模块
     */
    public MockModule getActivictMockModule() {
        Objects.requireNonNull(mockCoreData);
        if (settingActivictMockModule == null) {
            String activeModule;
            if (mockCoreData.getMockGlobalConfig() != null) {
                activeModule = mockCoreData.getMockGlobalConfig().getActiveModule();
            } else {
                activeModule = mockConfig.getActiveModule();
            }
            if (StrUtil.isNotEmpty(activeModule)) {
                Optional<MockModule> firstMockModule = mockCoreData.getModuleList()
                        .stream()
                        .filter(e -> activeModule.equals(e.getName()) || activeModule.equals(e.getDisplayName()))
                        .findFirst();
                firstMockModule.ifPresent(mockModule -> settingActivictMockModule = mockModule);
            }
        }
        if (settingActivictMockModule == null && CollUtil.isNotEmpty(mockCoreData.getModuleList())) {
            settingActivictMockModule = mockCoreData.getModuleList().get(0);
        }
        return settingActivictMockModule;
    }

    public String getDataPath() {
        return mockConfig.getDataPath();
    }

    /**
     * 初始化模块
     */
    public void initModules() {
        mockCoreData = mockFileService.loadMockCoreData(getDataPath());
        settingActivictMockModule = null;
    }

    /**
     * 重载模块
     */
    public void reloadModules() {
        initModules();
    }

    /**
     * 重载指定模块
     *
     * @param mockModule 模块
     */
    public void reloadModule(MockModule mockModule) {
        List<MockModule> moduleList = mockCoreData.getModuleList();
        if (CollUtil.isEmpty(moduleList) || !moduleList.contains(mockModule)) {
            throw new MockModuleNotFoundException("重载模块失败，模块 " + mockModule.getName() + " 不存在");
        }
        moduleList.set(moduleList.indexOf(mockModule), mockModule);
        if (settingActivictMockModule.equals(mockModule)) {
            settingActivictMockModule = mockModule;
        }
    }

    public List<ModuleSimpleInfo> getModuleSimpleInfo() {
        MockModule activictMockModule = getActivictMockModule();
        List<MockModule> moduleList = mockCoreData.getModuleList();
        if (CollUtil.isEmpty(moduleList)) {
            return List.of();
        }
        List<ModuleSimpleInfo> moduleSimpleInfos = new ArrayList<>();
        for (MockModule mockModule : moduleList) {
            moduleSimpleInfos.add(ModuleSimpleInfo
                    .builder()
                    .name(mockModule.getName())
                    .displayName(mockModule.getDisplayName())
                    .des(mockModule.getDisplayDes())
                    .path(mockModule.getRequestPath())
                    .active(activictMockModule==mockModule)
                    .build()
            );
        }
        return moduleSimpleInfos;
    }
}
