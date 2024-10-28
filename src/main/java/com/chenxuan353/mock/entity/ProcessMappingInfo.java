package com.chenxuan353.mock.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public final class ProcessMappingInfo {
    String path;
    String processDisplayName;
    String processName;
    String relativePath;
}