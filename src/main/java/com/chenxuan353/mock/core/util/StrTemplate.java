package com.chenxuan353.mock.core.util;

import org.springframework.util.PropertyPlaceholderHelper;

import java.util.Map;

public class StrTemplate {
    // 占位符前缀
    private static final String PREFIX = "${";
    // 占位符后缀
    private static final String SUFFIX = "}";

    /*
     * commons-text
     * */
    public static String replaceVar(Map<String, String> vars, String template) {
        //定义${开头 ，}结尾的占位符
        PropertyPlaceholderHelper propertyPlaceholderHelper = new PropertyPlaceholderHelper(PREFIX, SUFFIX);
        //调用替换
        return propertyPlaceholderHelper.replacePlaceholders(template, vars::get);
    }
}
