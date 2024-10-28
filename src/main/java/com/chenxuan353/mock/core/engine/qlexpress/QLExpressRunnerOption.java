package com.chenxuan353.mock.core.engine.qlexpress;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Getter
public class QLExpressRunnerOption {
    private final QLExpressLogger logger;
    private final List<Class<?>> accessClasses;
    private final Map<String, Object> putMembers;

    public static Builder builder(QLExpressLogger logger) {
        return new Builder(logger);
    }

    public static final class Builder {
        private final QLExpressLogger logger;
        private List<Class<?>> accessClasses;
        private Map<String, Object> putMembers;

        private Builder(QLExpressLogger logger) {
            this.logger = logger;
        }

        public Builder accessClasses(List<Class<?>> accessClasses) {
            this.accessClasses = accessClasses;
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

        public Builder putMember(String key, Object val) {
            this.putMembers.put(key, val);
            return this;
        }


        public QLExpressRunnerOption build() {
            return new QLExpressRunnerOption(logger, accessClasses, putMembers);
        }
    }
}
