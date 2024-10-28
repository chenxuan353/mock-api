package com.chenxuan353.mock.core.engine.qlexpress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class QLExpressLogger {
    public final Logger log;

    public QLExpressLogger(String name) {
        log = LoggerFactory.getLogger("QLExpress-" + name.trim());
    }

    public QLExpressLogger(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    public void debug(String var1, Object... var2) {
        log.debug(var1, var2);
    }


    public void info(String var1, Object... var2) {
        log.info(var1, var2);
    }


    public void warn(String var1, Object... var2) {
        log.warn(var1, var2);
    }


    public void error(String var1, Object... var2) {
        log.error(var1, var2);
    }
}
