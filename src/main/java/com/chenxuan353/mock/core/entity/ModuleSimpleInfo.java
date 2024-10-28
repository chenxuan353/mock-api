package com.chenxuan353.mock.core.entity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModuleSimpleInfo {
    private String name;
    private String displayName;
    private String des;
    private String path;
    private boolean active;
}
