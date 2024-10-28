package com.chenxuan353.mock.core.engine.graalvm;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class GraalJsContextOption {
    private final GraalvmLogger logger;
    private final boolean loadMockJs;
    private final boolean loadAxiosJs;
    private final boolean parseRequestBody;
    private final boolean secureMode;
    private final List<Class<?>> accessClasses;
    private final Map<String, Object> putMembers;

    public static Builder builder(GraalvmLogger logger) {
        return new Builder(logger);
    }

    @SuppressWarnings("unused")
    public static final class Builder {
        private final GraalvmLogger logger;
        private boolean loadMockJs = false;
        private boolean loadAxiosJs = false;
        private boolean parseRequestBody = false;
        private boolean secureMode = true;
        private List<Class<?>> accessClasses = new ArrayList<>();
        private Map<String, Object> putMembers = new HashMap<>();

        private Builder(GraalvmLogger logger) {
            this.logger = logger;
        }

        public Builder loadMockJs(boolean loadMockJs) {
            this.loadMockJs = loadMockJs;
            return this;
        }

        public Builder loadAxiosJs(boolean loadAxiosJs) {
            this.loadAxiosJs = loadAxiosJs;
            return this;
        }

        public Builder accessClass(List<Class<?>> accessClass) {
            this.accessClasses = accessClass;
            return this;
        }

        public Builder accessClass(Set<Class<?>> accessClass) {
            this.accessClasses = new ArrayList<>();
            this.accessClasses.addAll(accessClass);
            return this;
        }

        public Builder accessClass(Class<?>... classes) {
            this.accessClasses.addAll(Arrays.asList(classes));
            return this;
        }

        public Builder putMembers(Map<String, Object> putMembers) {
            this.putMembers = putMembers;
            return this;
        }

        public Builder parseRequestBody(boolean parseRequestBody) {
            this.parseRequestBody = parseRequestBody;
            return this;
        }

        public Builder putMember(String id, Object value) {
            putMembers.put(id, value);
            return this;
        }

        public Builder secureMode(boolean secureMode) {
            this.secureMode = secureMode;
            return this;
        }

        public GraalJsContextOption build() {
            return new GraalJsContextOption(
                    logger,
                    loadMockJs,
                    loadAxiosJs,
                    parseRequestBody,
                    secureMode,
                    accessClasses,
                    putMembers
            );
        }

        public Context buildContext() {
            GraalJsContextOption option = build();
            return GraalJsUtil.getContext(option);
        }
    }
}
