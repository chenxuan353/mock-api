package com.chenxuan353.mock.core.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 请求匹配参数
 */
@Data
@EqualsAndHashCode
public class MockRequestMapping {
    private String path;
    private RequestMethod[] methods;
    private String[] headers;
    private String[] params;
    private String[] consumes;
    private String[] produces;
    /**
     * 允许跨域
     */
    private Boolean cors;
    /**
     * 关闭路径转换(`.`->`/`)
     */
    private Boolean disablePathConvert;
    /**
     * 静态文件模式，开启后将尽可能保证文件作为静态文件对待，且强制关闭路径转换。
     * **该选项具有强制传播性**
     */
    private Boolean staticMode;
}
