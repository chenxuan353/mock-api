package com.chenxuan353.mock.controller;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.chenxuan353.mock.config.MockConfig;
import com.chenxuan353.mock.core.component.MockGroup;
import com.chenxuan353.mock.core.component.MockModule;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.consts.MockRespError;
import com.chenxuan353.mock.core.controller.ProcessController;
import com.chenxuan353.mock.core.entity.ModuleSimpleInfo;
import com.chenxuan353.mock.core.service.MockResourceMappingService;
import com.chenxuan353.mock.core.service.MockService;
import com.chenxuan353.mock.entity.ProcessMappingInfo;
import com.chenxuan353.mock.entity.ResourceMappingInfo;
import com.chenxuan353.mock.util.RespUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@RestController
@Slf4j
@RequestMapping(path = "/mock/module", headers = {"MockVersion=v1"})
public class ModuleController {
    private final RequestMappingHandlerMapping requestMappingHandlerMapping;
    private final MockResourceMappingService mockResourceMappingService;
    private final MockConfig mockConfig;
    private final MockService mockService;


    @GetMapping("/config")
    public Object config() {
        return RespUtil.success(JSON.toJSON(mockConfig));
    }

    @GetMapping("/modulesList")
    public Object modulesList() {
        return RespUtil.success(mockService.getModuleSimpleInfo());
    }


    @PostMapping("/reloadModules")
    public Object reloadModules() {
        log.info("正在重载模块...");
        mockService.reload();
        List<ModuleSimpleInfo> moduleSimpleInfo = mockService.getModuleSimpleInfo();
        log.info("模块重载完成，成功载入 {} 个模块。", moduleSimpleInfo.size());
        return RespUtil.success(moduleSimpleInfo);
    }

    @PostMapping("/activeModule")
    public Object activeModuleInfo(String moduleName) {
        mockService.activictMockModule(moduleName);
        return RespUtil.success();
    }

    @GetMapping("/showActiveModuleInfo")
    public Object showActiveModuleInfo(@RequestParam(required = false) boolean filterStaticFile) {
        MockModule activictMockModule = mockService.getActivictMockModule();
        if (activictMockModule == null) {
            return RespUtil.error(MockRespError.ACTIVACT_MOCK_MODULE_IS_EMPTY);
        }
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        List<ProcessMappingInfo> mappingInfos = new ArrayList<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            if (!(handlerMethod.getBean() instanceof ProcessController processController)) {
                continue;
            }
            RequestMappingInfo info = entry.getKey();
            if (info.getPathPatternsCondition() == null) {
                continue;
            }
            MockProcess mockProcess = processController.getMockProcess();
            if (filterStaticFile && mockProcess.isStaticMode()) {
                continue;
            }
            Set<PathPattern> pathSet = info.getPathPatternsCondition().getPatterns();
            String relativePath;
            if(mockProcess.getFiles().size() == 1){
                relativePath = FileUtil.subPath(new File(mockConfig.getDataPath()).getAbsolutePath(), mockProcess.getFiles().get(0));
            }else{
                relativePath = FileUtil.subPath(new File(mockConfig.getDataPath()).getAbsolutePath(), mockProcess.getDependentPath());
            }
            mappingInfos.addAll(pathSet
                    .stream()
                    .map(p -> new ProcessMappingInfo(p.getPatternString(),
                            mockProcess.getDisplayName(),
                            mockProcess.getName(),
                            relativePath
                    ))
                    .toList()
            );

        }

        Map<String, MockGroup> resourceMapping = mockResourceMappingService.getResourceMapping();
        List<ResourceMappingInfo> resourceMappingInfos = resourceMapping.entrySet().stream().map(
                entry -> new ResourceMappingInfo(
                        entry.getKey(),
                        entry.getValue().getDisplayName(),
                        entry.getValue().getName(),
                        FileUtil.subPath(new File(mockConfig.getDataPath()).getAbsolutePath(), entry.getValue().getDependentPath())
                )
        ).sorted(Comparator.comparing(ResourceMappingInfo::getPath)).toList();

        Map<String, Object> retData = new HashMap<>();
        retData.put("requestMappingInfos", mappingInfos
                .stream()
                .distinct()
                .sorted(Comparator.comparing(ProcessMappingInfo::getPath))
                .toList());
        retData.put("resourceMappingInfos", resourceMappingInfos);
        retData.put("dataPath", mockConfig.getDataPath());
        retData.put("activeModuleName", activictMockModule.getName());
        retData.put("activeModuleDisplayName", activictMockModule.getDisplayName());
        retData.put("activeModuleDisplayDes", activictMockModule.getDisplayDes());
        retData.put("activeModuleRequestPath", activictMockModule.getRequestPath());
        return RespUtil.success(retData);
    }
}
