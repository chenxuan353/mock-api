package com.chenxuan353.mock.core.engine;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.HostAccess;

@Slf4j
public class CommonJsonHelper {
    @HostAccess.Export
    public String toJsonString(Object obj) {
        try {
            return JSON.toJSONString(obj);
        } catch (Exception e) {
            log.warn("CommonJsonHelper 转为JSON失败", e);
            return null;
        }
    }

    @HostAccess.Export
    public <T> T parse(String json, Class<T> clazz) {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            log.warn("CommonJsonHelper 解析JSON失败", e);
            return null;
        }
    }
}
