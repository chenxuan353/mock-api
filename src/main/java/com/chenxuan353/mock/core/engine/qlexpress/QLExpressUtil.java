package com.chenxuan353.mock.core.engine.qlexpress;

import com.chenxuan353.mock.config.MockConfig;
import com.ql.util.express.config.QLExpressRunStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@Component
@Slf4j
public class QLExpressUtil {
    public static final Set<Class<?>> runtimeSecuryClass = new HashSet<>();
    private static MockConfig mockConfig;

    @Autowired
    public void init(MockConfig mockConfig) {
        QLExpressUtil.mockConfig = mockConfig;
        if (!mockConfig.getQleEngine().isSecureMode()) {
            return;
        }
        QLExpressRunStrategy.setForbidInvokeSecurityRiskMethods(true);
        QLExpressRunStrategy.setSecureMethods(new HashSet<>(mockConfig.getQleEngine().getSecureMethods()));

    }

    public static void addSecuryClass(Class<?> clazz) {
        if (!mockConfig.getQleEngine().isSecureMode()) {
            return;
        }
        if (runtimeSecuryClass.contains(clazz)) {
            return;
        }
        runtimeSecuryClass.add(clazz);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            QLExpressRunStrategy.addSecureMethod(clazz, method.getName());
        }
    }
}
