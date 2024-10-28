package com.chenxuan353.mock.core.service;

import cn.hutool.core.io.FileUtil;
import com.chenxuan353.mock.config.MockConfig;
import com.chenxuan353.mock.core.component.MockModule;
import com.chenxuan353.mock.core.entity.ModuleSimpleInfo;
import com.chenxuan353.mock.core.exception.MockNotDirectoryException;
import com.chenxuan353.mock.core.filewatch.FileChangeService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.List;

/**
 * Mock服务
 * 提供Mock功能
 */
@RequiredArgsConstructor
@Service
@Slf4j
public class MockService {
    private final MockConfig mockConfig;
    private final MockCoreService mockCoreService;
    private final MockRequestMappingService mockRequestMappingService;
    private final MockResourceMappingService mockResourceMappingService;
    private final FileChangeService fileChangeService;

    @PostConstruct
    protected void init() {
        // 初始化
        String dataPath = mockCoreService.getDataPath();
        File dataDir = new File(dataPath);
        if (!dataDir.isDirectory()) {
            if (MockConfig.DEFAULT_DATA_PATH.equals(dataPath) && !dataDir.exists()) {
                boolean ignore = dataDir.mkdirs();
                if (dataDir.isDirectory()) {
                    log.info("数据文件夹创建成功：{}", FileUtil.getAbsolutePath(dataDir));
                } else {
                    throw new MockNotDirectoryException("数据文件夹创建失败！当前值：" + dataPath + " | 绝对路径：" + FileUtil.getAbsolutePath(dataDir));
                }
            } else {
                throw new MockNotDirectoryException("数据文件夹必须是合法的目录！当前值：" + dataPath + " | 绝对路径：" + FileUtil.getAbsolutePath(dataDir));
            }
        }
        mockCoreService.initModules();
        MockModule activictMockModule = mockCoreService.getActivictMockModule();
        if (activictMockModule == null) {
            log.warn("mockModule 为空，跳过路径映射初始化！");
        } else {
            mockRequestMappingService.initRequestMapping(activictMockModule);
            mockResourceMappingService.initResourceMapping(activictMockModule);
            log.info("激活模块：{}", activictMockModule.getDisplayName());
        }

        Thread checkWatchChange = new Thread(() -> {
            for (; ; ) {
                try {
                    this.checkWatchChange();
                    if (mockConfig.getDirWatchCheckInterval() > 15) {
                        Thread.sleep(mockConfig.getDirWatchCheckInterval());
                    } else {
                        Thread.sleep(200);
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    log.error("checkWatchChangeError", e);
                }
            }
        });
        checkWatchChange.setName("checkWatchChange");
        checkWatchChange.setDaemon(true);
        checkWatchChange.start();
    }

    private int checkWatchNoChangeCount = 0;
    private boolean startCount = false;

    protected void checkWatchChange() {
        if (startCount) {
            if (fileChangeService.readAndResetChange()) {
                checkWatchNoChangeCount = 0;
                return;
            }
            checkWatchNoChangeCount++;
            if (checkWatchNoChangeCount > mockConfig.getDirWatchCheckMaxCount()) {
                log.info("检测到数据文件夹变化，重新加载Mock...");
                StopWatch sw = new StopWatch();
                sw.start();
                reload();
                sw.stop();
                log.info("重载已完成！耗时：{}s", String.format("%.2f", sw.lastTaskInfo().getTimeSeconds()));
                startCount = false;
                checkWatchNoChangeCount = 0;
            }
        } else {
            if (fileChangeService.readAndResetChange()) {
                startCount = true;
            }
        }

    }

    /**
     * 激活模块
     *
     * @param moduleName 模块名
     * @return 是否激活了新模块
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean activictMockModule(String moduleName) {
        if (mockCoreService.activictMockModule(moduleName)) {
            mockRequestMappingService.reloadRequestMapping(mockCoreService.getActivictMockModule());
            mockResourceMappingService.reloadResourceMapping(mockCoreService.getActivictMockModule());
            log.info("激活模块：{}", mockCoreService.getActivictMockModule().getDisplayName());
            return true;
        }
        return false;
    }

    public MockModule getActivictMockModule() {
        return mockCoreService.getActivictMockModule();
    }

    /**
     * 重新加载Mock
     */
    public void reload() {
        mockCoreService.reloadModules();
        MockModule activictMockModule = mockCoreService.getActivictMockModule();
        mockRequestMappingService.reloadRequestMapping(activictMockModule);
        mockResourceMappingService.reloadResourceMapping(activictMockModule);
        if (activictMockModule != null) {
            log.info("激活模块：{}", activictMockModule.getDisplayName());
        }
    }

    public List<ModuleSimpleInfo> getModuleSimpleInfo() {
        return mockCoreService.getModuleSimpleInfo();
    }


}
