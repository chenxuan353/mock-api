package com.chenxuan353.mock.core.engine;

import com.chenxuan353.mock.core.consts.MockProcessConsts;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.HostAccess;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@RequiredArgsConstructor
public class CommonSafeSessionHelper {
    private final HttpSession session;

    @HostAccess.Export
    public long getCreationTime() {
        return session.getCreationTime();
    }

    @HostAccess.Export
    public String getId() {
        return session.getId();
    }

    @HostAccess.Export
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }


    @HostAccess.Export
    public void setMaxInactiveInterval(int i) {
        session.setMaxInactiveInterval(i);
    }

    @HostAccess.Export
    public int getMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }

    @HostAccess.Export
    public Object getAttribute(String s) {
        Object attribute = session.getAttribute(s);
        if (!MockProcessConsts.varIsSafeTpye(attribute)) {
            throw new RuntimeException("在安全Session中读取属性失败，该属性非基本数据类型！");
        }
        return attribute;
    }

    @HostAccess.Export
    public List<String> getAttributeNames() {
        Enumeration<String> attributeNames = session.getAttributeNames();
        List<String> list = new ArrayList<>();
        attributeNames.asIterator().forEachRemaining(key -> {
            Object attribute = session.getAttribute(key);
            if (MockProcessConsts.varIsSafeTpye(attribute)) {
                list.add(key);
            }
        });
        return list;
    }

    @HostAccess.Export
    public void setAttribute(String s, Object o) {
        Object attribute = session.getAttribute(s);
        if (!MockProcessConsts.varIsSafeTpye(attribute)) {
            throw new RuntimeException("在安全Session中设置属性失败，该属性已被设置非基本数据类型的数据！");
        }
        if (!MockProcessConsts.varIsSafeTpye(o)) {
            throw new RuntimeException("在安全Session中设置属性失败，设置的值非法！");
        }
        session.setAttribute(s, o);
    }

    @HostAccess.Export
    public void removeAttribute(String s) {
        Object attribute = session.getAttribute(s);
        if (!MockProcessConsts.varIsSafeTpye(attribute)) {
            throw new RuntimeException("在安全Session中移除属性失败，该属性已被设置非基本数据类型的数据！");
        }
        session.removeAttribute(s);
    }

    @HostAccess.Export
    public void invalidate() {
        session.invalidate();
    }

    @HostAccess.Export
    public boolean isNew() {
        return session.isNew();
    }
}
