package com.chenxuan353.mock.controller;

import com.alibaba.fastjson.JSON;
import com.chenxuan353.mock.config.MockConfig;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@Slf4j
@RequestMapping(path = "/mock/meta", headers = {"MockVersion=v1"})
public class MetaController {
    private final MockConfig mockConfig;

    @RequestMapping("/config")
    public Object config() {
        return JSON.toJSON(mockConfig);
    }
}
