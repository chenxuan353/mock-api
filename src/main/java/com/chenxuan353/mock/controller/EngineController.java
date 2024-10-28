package com.chenxuan353.mock.controller;

import com.chenxuan353.mock.core.engine.CommonRespHelper;
import com.chenxuan353.mock.core.engine.graalvm.GraalJsUtil;
import com.chenxuan353.mock.core.engine.graalvm.GraalvmLogger;
import com.chenxuan353.mock.core.exception.MockEngineScriptException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@Slf4j
@RequestMapping(path = "/mock/engine", headers = {"MockVersion=v1"})
public class EngineController {
    private static final GraalvmLogger graalvmLogger = new GraalvmLogger(EngineController.class);

    @RequestMapping(value = {"/js"}, method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public Object js(@RequestBody String script) {
        try (Context context = GraalJsUtil.getContext(graalvmLogger)) {
            Value ret = context.eval("js", script);
            return ret.toString();
        }
    }

    @RequestMapping(value = {"/jsProcess"}, method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public ResponseEntity<?> jsProcess(@RequestBody String script) throws MockEngineScriptException {
        CommonRespHelper.CommonRespHelperBuilder builder = CommonRespHelper.builder();
        try (Context context = GraalJsUtil.getProcess(graalvmLogger)) {
            Value js = context.getBindings("js");
            js.putMember("respBuilder", builder);
            String mergeScript = "(function (){\n" + script + "\n})()";
            Value ret = context.eval("js", mergeScript);
            if (builder.getBody() == null) {
                builder.body(ret.toString());
            }
            return builder.build().toResponseEntity();
        }
    }

    @RequestMapping(value = {"/jsMock"}, method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public String jsMock(@RequestBody String mockJson) {
        return GraalJsUtil.mock(graalvmLogger, mockJson);
    }

    @RequestMapping(value = {"/jsMockJsObject"}, method = {RequestMethod.POST}, produces = "application/json;charset=UTF-8")
    public String jsMockJsObject(@RequestBody String jsObject) {
        return GraalJsUtil.mockJsObject(graalvmLogger, jsObject);
    }
}
