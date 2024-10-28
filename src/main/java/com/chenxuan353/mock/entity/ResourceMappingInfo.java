package com.chenxuan353.mock.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public final class ResourceMappingInfo {
    String path;
    String groupDisplayName;
    String groupName;
    String relativePath;
}