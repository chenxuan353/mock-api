package com.chenxuan353.mock.core.config;

import com.chenxuan353.mock.core.consts.MockProcessType;
import lombok.Data;

import java.util.Map;

/**
 * 处理器响应配置
 */
@Data
public class MockResponseConfig {
    /**
     * 响应头
     */
    private Map<String, String> headers;
    /**
     * 响应类型，不会被响应头配置覆盖
     */
    private String contentType;
    /**
     * 脚本路径（响应体内容会作为脚本环境变量传入）
     */
    private String scriptPath;
    /**
     * 脚本内容（脚本路径优先）
     */
    private String scriptContent;
    /**
     * 脚本引擎
     */
    private MockProcessType processType;
    /**
     * 响应体内容
     */
    private String body;
    /**
     * 文件路径(仅适用于 processType=File)
     */
    private String filePath;
    /**
     * 响应类型自适应 (仅适用于 processType=File)
     */
    private Boolean fileContentTypeAuto;
    /**
     * 下载文件名(仅适用于 processType=File)
     */
    private String downloadFileName;
    /**
     * 脚本外部文本资源加载列表（键为写入的变量名，值为文件路径）
     */
    private Map<String, String> externalTextResources;
    /**
     * 脚本外部文件列表（只有该列表中的文件允许在响应中以文件形式返回）
     */
    private Map<String, String> externalFileResources;
    /**
     * 脚本环境变量
     */
    private Map<String, String> envionment;
    /**
     * JS类型是否加载axios
     */
    private Boolean jsLoadAxios;
}
