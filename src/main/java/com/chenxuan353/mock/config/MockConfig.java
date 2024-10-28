package com.chenxuan353.mock.config;

import com.chenxuan353.mock.core.consts.MockEngineType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "mock")
@Data
public class MockConfig {
    public static final String DEFAULT_DATA_PATH = "./data";
    private boolean enableEngineDebug = false;
    /**
     * 数据文件夹位置
     */
    private String dataPath = DEFAULT_DATA_PATH;
    /**
     * 默认激活模块
     */
    private String activeModule;
    /**
     * 静态文件模式的响应组是否加载文件列表
     */
    private boolean staticModeLoadFiles = false;
    /**
     * 静态文件模式通过ResourceHandlerMapping代理
     */
    private boolean staticModeResourceHandlerMappingProxy = true;
    /**
     * 构建信息
     */
    private BuildInfo build;
    /**
     * 启用引擎列表
     */
    private List<MockEngineType> enableEngineList = List.of(MockEngineType.GraalJs, MockEngineType.QLExpress);
    /**
     * 启用文件夹监听
     */
    private boolean enableDirWatch = true;
    /**
     * 文件变化计数间隔（毫秒） 必须>15ms
     */
    private int dirWatchCheckInterval = 200;
    /**
     * 文件变化重加载检查计数（文件变化后，无变化超过计数则执行重载）
     */
    private int dirWatchCheckMaxCount = 10;
    /**
     * JS引擎配置
     */
    private JSConfig jsEngine = new JSConfig();
    private QLExpressEngine qleEngine = new QLExpressEngine();

    @Data
    public static class BuildInfo {
        private String version;
        private String time;
    }

    @Data
    public static class JSConfig {
        private boolean secureMode = true;
        private boolean useConsoleLog = false;
        private boolean exportRequest = false;
        private boolean exportResponse = false;
        private boolean exportSafeSession = true;
        private boolean exportShareVar = true;
        private boolean exportParentShareVar = true;
        private boolean exportGlobalShareVar = true;
    }

    @Data
    public static class QLExpressEngine {
        private Set<String> secureMethods = Set.of(
                "java.lang.String.length",
                "java.lang.Integer.valueOf"
        );
        private boolean secureMode = true;
        private int runningTimeLimit = 10000;
        private boolean exportRequest = false;
        private boolean exportResponse = false;
        private boolean exportSafeSession = true;
        private boolean exportShareVar = true;
        private boolean exportParentShareVar = true;
        private boolean exportGlobalShareVar = true;
    }
}
