package com.chenxuan353.mock.core.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.core.io.FileSystemResource;

import java.io.File;

/***
 * Yaml工具类
 */
public class YamlUtil {
    public static <T> T loadYamlToType(String filePath, T ins) {
        return loadYamlToType(new File(filePath), ins);
    }

    public static <T> T loadYamlToType(File file, T ins) {
        FileSystemResource fileSystemResource = new FileSystemResource(file);
        YamlMapFactoryBean yamlMapFactoryBean = new YamlMapFactoryBean();
        yamlMapFactoryBean.setResources(fileSystemResource);
        return BeanUtil.fillBeanWithMap(yamlMapFactoryBean.getObject(), ins, CopyOptions.create());
    }
}
