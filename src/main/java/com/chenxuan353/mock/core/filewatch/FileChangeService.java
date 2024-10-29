package com.chenxuan353.mock.core.filewatch;

import cn.hutool.core.io.FileUtil;
import com.chenxuan353.mock.config.MockConfig;
import com.chenxuan353.mock.core.component.MockGroup;
import com.chenxuan353.mock.core.exception.MockNotDirectoryException;
import com.chenxuan353.mock.core.service.MockCoreService;
import com.chenxuan353.mock.core.service.MockResourceMappingService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 文件变化监听
 */
@RequiredArgsConstructor
@Component
@Slf4j
public class FileChangeService {
    private final MockConfig mockConfig;
    private final MockCoreService mockCoreService;
    private final MockResourceMappingService mockResourceMappingService;
    private final AtomicBoolean hasChange = new AtomicBoolean(false);
    private Map<String, MockGroup> resourceMapping;
    private List<String> resourceAbsPathList;

    /**
     * 检查文件是否在静态资源路径下
     *
     * @param file 文件
     * @return 是否在
     */
    protected boolean matchResourcePath(File file) {
        if (mockConfig.isStaticModeLoadFiles()) {
            return false;
        }
        Map<String, MockGroup> nowResourceMapping = mockResourceMappingService.getResourceMapping();
        if (resourceMapping != nowResourceMapping || resourceAbsPathList == null) {
            resourceMapping = nowResourceMapping;
            resourceAbsPathList = new ArrayList<>(nowResourceMapping.size());
            for (MockGroup mockGroup : nowResourceMapping.values()) {
                String dependentPath = mockGroup.getDependentPath();
                String absoluteDependentPath = FileUtil.getAbsolutePath(new File(dependentPath));
                resourceAbsPathList.add(absoluteDependentPath);
            }
        }
        String absolutePath = FileUtil.getAbsolutePath(file);
        for (String resourcePath : resourceAbsPathList) {
            if (absolutePath.startsWith(resourcePath)) {
                return true;
            }
        }
        return false;
    }


    @PostConstruct
    protected void init() {
        if (!mockConfig.isEnableDirWatch()) {
            log.info("数据目录变化监听未开启！");
            return;
        }
        final String dataPath = mockCoreService.getDataPath();
        final File dataDir = new File(mockCoreService.getDataPath());
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
        if (!dataDir.isDirectory()) {
            log.warn("数据目录变化监听失败，数据目录不合法！");
            return;
        }
        Thread fileChangeServiceWatch = new Thread(() -> {
            try {
                new WatchDir(dataDir, true, new FileActionCallbackImpl(hasChange, this));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        fileChangeServiceWatch.setName("FileChangeServiceWatch");
        fileChangeServiceWatch.setDaemon(true);
        fileChangeServiceWatch.start();
        log.info("正在监听数据目录变化: " + FileUtil.getAbsolutePath(dataDir));
    }

    public boolean readAndResetChange() {
        boolean b = readChange();
        resetChange();
        return b;
    }

    public boolean readChange() {
        return hasChange.get();
    }

    public void resetChange() {
        hasChange.set(false);
    }
}
