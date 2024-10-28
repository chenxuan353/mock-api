package com.chenxuan353.mock.core.process;

import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.core.component.MockProcess;
import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.engine.CommonRespHelper;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import com.chenxuan353.mock.core.exception.MockEngineException;
import com.chenxuan353.mock.util.RespUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.net.URISyntaxException;

import static com.chenxuan353.mock.core.consts.MockRespError.MOCK_PROCESS_REDIRECT_BODY_EMPTY;
import static com.chenxuan353.mock.core.consts.MockRespError.MOCK_PROCESS_REDIRECT_URI_ERROR;

/**
 * File执行处理器
 * 只允许通过`yml`配置文件主动配置，且必须配置文件路径。
 */
@Slf4j
@Component
public class MockRedirect302Process extends MockProcessAbstract {

    @Override
    public boolean accessType(MockProcessType type) {
        return MockProcessType.Redirect302 == type;
    }

    @Override
    public ResponseEntity<?> execute(MockEngineProcessData mockEngineProcessData) throws MockEngineException {
        MockProcess mockProcess = mockEngineProcessData.getMockProcess();
        CommonRespHelper.CommonRespHelperBuilder respBuilder = packageRespBuilder(mockEngineProcessData);
        if (StrUtil.isEmpty(respBuilder.getBody())) {
            log.warn("MockRedirect302执行处理器未解析到跳转路径 MockProcess: {} | 路径: {}", mockProcess.getDisplayName(), mockEngineProcessData.getRequest().getRequestURI());
            return RespUtil.error(MOCK_PROCESS_REDIRECT_BODY_EMPTY);
        }
        try {
            respBuilder.redirect(respBuilder.getBody());
        } catch (URISyntaxException e) {
            log.error("重定向响应路径不合法！", e);
            return RespUtil.error(MOCK_PROCESS_REDIRECT_URI_ERROR);
        }

        return respBuilder.build().toResponseEntity(mockEngineProcessData);
    }
}
