package com.chenxuan353.mock.core.engine;

import com.chenxuan353.mock.core.consts.MockProcessConsts;
import org.graalvm.polyglot.HostAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CommonShareVarHelper {
    private final Map<String, Object> runtimeShareVar;

    public CommonShareVarHelper(Map<String, Object> runtimeShareVar) {
        this.runtimeShareVar = runtimeShareVar;
    }

    @HostAccess.Export
    public Object get(String key) {
        if (runtimeShareVar.containsKey(key)) {
            return runtimeShareVar.get(key);
        }
        return null;
    }

    @HostAccess.Export
    public void set(String key, Object value) {
        if (!MockProcessConsts.varIsSafeTpye(value)) {
            throw new RuntimeException("设置共享变量时失败，设置的值非法！");
        }
        runtimeShareVar.put(key, value);
    }

    @HostAccess.Export
    public void remove(String key) {
        runtimeShareVar.remove(key);
    }

    @HostAccess.Export
    public void rm(String key) {
        remove(key);
    }

    @HostAccess.Export
    public void delete(String key) {
        remove(key);
    }

    @HostAccess.Export
    public void del(String key) {
        remove(key);
    }

    @HostAccess.Export
    public int size() {
        return runtimeShareVar.size();
    }

    @HostAccess.Export
    public int length() {
        return runtimeShareVar.size();
    }

    @HostAccess.Export
    public void clear() {
        runtimeShareVar.clear();
    }

    @HostAccess.Export
    public List<String> keys() {
        Set<String> strings = runtimeShareVar.keySet();
        return new ArrayList<>(strings);
    }

    @HostAccess.Export
    public List<Object> values() {
        Collection<Object> values = runtimeShareVar.values();
        return new ArrayList<>(values);
    }
}
