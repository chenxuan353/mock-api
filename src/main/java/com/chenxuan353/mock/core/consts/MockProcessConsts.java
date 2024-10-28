package com.chenxuan353.mock.core.consts;

import cn.hutool.core.io.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MockProcessConsts {
    /**
     * 脚本相关资源大小限制（15Mb）
     */
    public static final int SCRIPT_FILE_SIZE_LIMIT = 15 * 1024 * 1024;
    /**
     * 请求被Mock标识
     */
    public static final String MOCK_REQUEST_MARK = "MOCK_REQUEST_MARK";
    public static final String URL_SPLIT = "/";
    /**
     * 配置文件后缀名
     */
    public static final String[] CONFIG_EXT = {
            "yml", "yaml"
    };
    /**
     * 内部资源后缀名
     */
    public static final String[] INTERNAL_RESOURCE_EXT = {
            "js", "qle",
            "text", "json", "html", "data", "htm", "txt", "body", "css", "xml"
    };

    private static final Class<?>[] scriptVarSafeTpye = new Class[]{
            HashMap.class,
            ArrayList.class,
            HashSet.class
    };

    /**
     * 从多个字符数组中不区分大小写的查找匹配项
     *
     * @param originStr 源字符串
     * @param strss     匹配来源字符串数组
     * @return 是否存在匹配
     */
    public static boolean strIgnoreCaseExist(String originStr, String[]... strss) {
        for (String[] strings : strss) {
            for (String string : strings) {
                if (string.equalsIgnoreCase(originStr)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean processFileExtMatch(File file) {
        return processFileExtMatch(FileUtil.getSuffix(file));
    }

    public static boolean processFileExtMatch(String originStr) {
        return strIgnoreCaseExist(originStr, CONFIG_EXT, INTERNAL_RESOURCE_EXT);
    }

    /**
     * 判断提供的类型是否安全
     * 默认允许基本数据类型以及null
     *
     * @param obj 数据
     * @return 是否是安全数据
     */
    public static boolean varIsSafeTpye(Object obj) {
        if (obj == null) {
            return true;
        }
        Class<?> clazz = obj.getClass();
        if (clazz.isPrimitive()) {
            return true;
        }
        for (Class<?> safeClazz : scriptVarSafeTpye) {
            if (safeClazz == clazz) {
                return true;
            }
        }
        return false;
    }
}
