package com.chenxuan353.mock.core.component;

import com.chenxuan353.mock.core.config.MockGlobalConfig;
import lombok.Data;

import java.util.List;


@Data
public class MockCoreData {
    private MockGlobalConfig mockGlobalConfig;
    private List<MockModule> moduleList;
}
