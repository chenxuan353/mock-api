package com.chenxuan353.mock.core.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.chenxuan353.mock.config.MockConfig;
import com.chenxuan353.mock.core.component.MockCoreData;
import com.chenxuan353.mock.core.component.MockGroup;
import com.chenxuan353.mock.core.component.MockModule;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.config.MockGlobalConfig;
import com.chenxuan353.mock.core.config.MockGroupConfig;
import com.chenxuan353.mock.core.config.MockModuleConfig;
import com.chenxuan353.mock.core.config.MockProcessConfig;
import com.chenxuan353.mock.core.consts.MockProcessConsts;
import com.chenxuan353.mock.core.util.YamlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 负责对文件进行处理
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MockFileService {
    public static final String MOCK_GLOBAL_CONFIG_NAME = "global-mock";
    public static final String MOCK_MODULE_CONFIG_NAME = "module-mock";
    public static final String MOCK_GROUP_CONFIG_NAME = "group-mock";

    private final MockConfig mockConfig;

    /**
     * 加载Mock核心数据
     *
     * @param path 路径
     * @return Mock核心数据
     */
    public MockCoreData loadMockCoreData(String path) {
        MockCoreData mockCoreData = new MockCoreData();
        File directory = new File(path);
        File config = Paths.get(path, MOCK_GLOBAL_CONFIG_NAME + ".yml").toFile();
        if (!config.exists()) {
            config = Paths.get(path, MOCK_GLOBAL_CONFIG_NAME + ".yaml").toFile();
        }
        if (config.exists()) {
            try {
                mockCoreData.setMockGlobalConfig(YamlUtil.loadYamlToType(config, new MockGlobalConfig()));
            } catch (Exception e) {
                log.error("Mock核心配置读取失败 file: " + FileUtil.getAbsolutePath(config), e);
            }
        }
        List<MockModule> mockModules = loadMockModules(directory);
        mockCoreData.setModuleList(mockModules);
        return mockCoreData;
    }

    /**
     * 从指定目录加载模块列表
     *
     * @param directory 目录
     * @return 模块列表
     */
    public List<MockModule> loadMockModules(File directory) {
        List<MockModule> mockModules = new ArrayList<>();
        File[] files = directory.listFiles(File::isDirectory);
        if (files == null) {
            return new ArrayList<>();
        }
        for (File file : files) {
            MockModule mockModule = loadMockModule(file);
            mockModules.add(mockModule);
        }
        return mockModules;
    }

    /**
     * 从指定目录加载模块
     *
     * @param directory 目录
     * @return 模块
     */
    public MockModule loadMockModule(File directory) {
        File[] files = directory.listFiles(sf -> sf.isDirectory() || MockProcessConsts.processFileExtMatch(sf));
        if (files == null) {
            return MockModule.builder()
                    .rootGroup(new MockGroup(directory, true))
                    .dependentPath(directory.getAbsolutePath())
                    .build();
        }
        return loadMockModule(directory, List.of(files));
    }

    /**
     * 从文件列表加载模块
     *
     * @param directory   模块路径
     * @param originFiles 模块文件列表
     * @return 模块
     */
    public MockModule loadMockModule(File directory, List<File> originFiles) {
        List<File> files = originFiles
                .stream()
                .filter(sf -> sf.isDirectory() || MockProcessConsts.processFileExtMatch(sf))
                .toList();
        List<File> rootGroupFiles = new ArrayList<>();
        MockModuleConfig mockModuleConfig = null;
        File mockGroupConfigFile = null;
        for (File file : files) {
            if (file.isDirectory()) {
                rootGroupFiles.add(file);
                continue;
            }

            String fileName = file.getName();
            if (
                    (MOCK_MODULE_CONFIG_NAME + ".yml").equals(fileName) ||
                            (MOCK_MODULE_CONFIG_NAME + ".yaml").equals(fileName)) {
                if (mockModuleConfig == null) {
                    try {
                        mockModuleConfig = YamlUtil.loadYamlToType(file, new MockModuleConfig());
                    } catch (Exception e) {
                        log.error("Mock模块配置读取失败 file: " + FileUtil.getAbsolutePath(file), e);
                    }
                    if (mockGroupConfigFile == null) {
                        mockGroupConfigFile = file;
                    }
                }
                continue;
            }
            if (
                    (MOCK_GROUP_CONFIG_NAME + ".yml").equals(fileName) ||
                            (MOCK_GROUP_CONFIG_NAME + ".yaml").equals(fileName)) {
                if (mockGroupConfigFile == null) {
                    mockGroupConfigFile = file;
                }
                continue;
            }
            rootGroupFiles.add(file);
        }
        MockGroupConfig mockGroupConfig = null;
        if (mockGroupConfigFile != null) {
            try {
                mockGroupConfig = YamlUtil.loadYamlToType(mockGroupConfigFile, new MockGroupConfig());
            } catch (Exception e) {
                log.error("Mock模块配置读取失败 file: " + FileUtil.getAbsolutePath(mockGroupConfigFile), e);
            }
        }
        boolean staticMode = mockGroupConfig != null && mockGroupConfig.getRequest() != null && mockGroupConfig.getRequest().getStaticMode() != null && mockGroupConfig.getRequest().getStaticMode();
        if (staticMode) {
            mockGroupConfig.getRequest().setDisablePathConvert(true);
        }
        MockGroup rootMockGroup = loadMockGroup(directory, rootGroupFiles, staticMode);
        rootMockGroup.setRoot(true);

        // 配置合并
        if (mockGroupConfig != null) {
            try {
                if (rootMockGroup.getMockGroupConfig() == null) {
                    rootMockGroup.setMockGroupConfig(mockGroupConfig);
                } else {
                    BeanUtil.copyProperties(mockGroupConfig, rootMockGroup.getMockGroupConfig(), CopyOptions.create().ignoreNullValue());
                }
            } catch (Exception e) {
                log.error("Mock模块配置读取失败 file: " + FileUtil.getAbsolutePath(mockGroupConfigFile), e);
            }
        }

        return MockModule.builder()
                .dependentPath(directory.getAbsolutePath())
                .mockModuleConfig(mockModuleConfig)
                .rootGroup(rootMockGroup)
                .build();
    }

    /**
     * 从文件夹加载响应组
     *
     * @param directory  目录
     * @param staticMode 静态模式
     * @return 响应组
     */
    public MockGroup loadMockGroup(File directory, boolean staticMode) {
        File[] files = directory.listFiles(sf -> staticMode || sf.isDirectory() || MockProcessConsts.processFileExtMatch(sf));
        if (files == null) {
            return new MockGroup(directory);
        }
        return loadMockGroup(directory, List.of(files), staticMode);
    }

    /**
     * 通过文件列表加载响应组
     *
     * @param directory   响应组关联目录
     * @param originFiles 文件列表
     * @param staticMode  静态模式
     * @return 响应组
     */
    public MockGroup loadMockGroup(File directory, List<File> originFiles, boolean staticMode) {
        MockGroup mockGroup = new MockGroup(directory, false);
        if (staticMode && !mockConfig.isStaticModeLoadFiles()) {
            mockGroup.setStaticMode(true);
            return mockGroup;
        }
        List<File> files = originFiles
                .stream()
                .filter(sf -> staticMode || sf.isDirectory() || MockProcessConsts.processFileExtMatch(sf))
                .toList();
        List<File> directorys = new ArrayList<>();
        Map<String, List<File>> processMap = new HashMap<>();
        boolean subStaticMode = staticMode;
        for (File file : files) {
            if (file.isDirectory()) {
                directorys.add(file);
                continue;
            }

            String fileName = file.getName();

            if (staticMode) {
                processMap.put(fileName, List.of(file));
                continue;
            }

            if (
                    (MOCK_GROUP_CONFIG_NAME + ".yml").equals(fileName) ||
                            (MOCK_GROUP_CONFIG_NAME + ".yaml").equals(fileName)) {
                if (mockGroup.getMockGroupConfig() == null) {
                    try {
                        MockGroupConfig config = YamlUtil.loadYamlToType(file, new MockGroupConfig());
                        mockGroup.setMockGroupConfig(config);
                        if (config != null && config.getRequest() != null && config.getRequest().getStaticMode() != null && config.getRequest().getStaticMode()) {
                            mockGroup.setStaticMode(true);
                            subStaticMode = true;
                            config.getRequest().setDisablePathConvert(true);
                        }
                    } catch (Exception e) {
                        log.error("Mock响应组配置读取失败 file: " + FileUtil.getAbsolutePath(file), e);
                    }
                }
                continue;
            }

            String mainName = FileNameUtil.mainName(fileName);

            if (!processMap.containsKey(mainName)) {
                processMap.put(mainName, new ArrayList<>());
            }
            processMap.get(mainName).add(file);
        }

        if (subStaticMode && !mockConfig.isStaticModeLoadFiles()) {
            mockGroup.setStaticMode(true);
            return mockGroup;
        }

        for (String groupName : processMap.keySet()) {
            MockProcess mockProcess = loadMockProcess(groupName, processMap.get(groupName), subStaticMode);
            mockProcess.setParentGroup(mockGroup);
            mockGroup.getProcesses().add(mockProcess);
        }

        for (File directoryTmp : directorys) {
            MockGroup loadMockGroup = loadMockGroup(directoryTmp, subStaticMode);
            loadMockGroup.setParentGroup(mockGroup);
            mockGroup.getGroups().add(loadMockGroup);
        }
        return mockGroup;
    }

    /**
     * 通过文件列表加载处理器
     *
     * @param name        处理器名称
     * @param originFiles 文件列表
     * @param staticMode  静态模式
     * @return 处理器
     */
    public MockProcess loadMockProcess(String name, List<File> originFiles, boolean staticMode) {
        MockProcess mockProcess = new MockProcess(name, originFiles, staticMode);
        if (staticMode) {
            return mockProcess;
        }
        for (File originFile : originFiles) {
            String extName = FileNameUtil.extName(originFile);
            if ("yml".equals(extName) || "yaml".equals(extName)) {
                try {
                    MockProcessConfig config = YamlUtil.loadYamlToType(originFile, new MockProcessConfig());
                    mockProcess.setMockProcessConfig(config);
                } catch (Exception e) {
                    log.error("Mock处理器配置读取失败 file: " + FileUtil.getAbsolutePath(originFile), e);
                }
            }
        }
        return mockProcess;
    }
}
