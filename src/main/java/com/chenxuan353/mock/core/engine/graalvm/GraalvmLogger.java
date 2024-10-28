package com.chenxuan353.mock.core.engine.graalvm;

import org.graalvm.polyglot.HostAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraalvmLogger {
    @HostAccess.Export
    public final Logger log;

    public GraalvmLogger(String name) {
        log = LoggerFactory.getLogger("Graavm-" + name.trim());
    }

    public GraalvmLogger(Class<?> clazz) {
        this(clazz.getSimpleName());
    }

    @HostAccess.Export
    public void debug(String var1, Object... var2) {
        log.debug(var1, var2);
    }

    @HostAccess.Export
    public void info(String var1, Object... var2) {
        log.info(var1, var2);
    }

    @HostAccess.Export
    public void warn(String var1, Object... var2) {
        log.warn(var1, var2);
    }

    @HostAccess.Export
    public void error(String var1, Object... var2) {
        log.error(var1, var2);
    }
}
