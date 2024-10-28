package com.chenxuan353.mock.core.process;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.core.config.MockResponseConfig;
import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.engine.CommonRespHelper;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import com.chenxuan353.mock.core.exception.MockEngineException;
import org.springframework.http.ResponseEntity;

/**
 * Mock处理器接口
 */
public abstract class MockProcessAbstract {

    /**
     * 是否支持此类型的脚本处理
     *
     * @param type 脚本引擎类型
     * @return 是否支持
     */
    public abstract boolean accessType(MockProcessType type);

    protected CommonRespHelper.CommonRespHelperBuilder packageRespBuilder(MockEngineProcessData mockEngineProcessData) {
        CommonRespHelper.CommonRespHelperBuilder respBuilder = CommonRespHelper.builder();
        MockResponseConfig responseConfig = mockEngineProcessData.getResponseConfig();
        respBuilder.body(mockEngineProcessData.getResourceBody());
        if (CollUtil.isNotEmpty(responseConfig.getHeaders())) {
            respBuilder.headers(responseConfig.getHeaders());
        }
        if (StrUtil.isNotEmpty(responseConfig.getContentType())) {
            respBuilder.header("Content-Type", responseConfig.getContentType());
        }
        return respBuilder;
    }

    /**
     * 执行脚本，并生成响应
     *
     * @param mockEngineProcessData 引擎所需的过程数据
     * @return 响应
     * @throws MockEngineException 引擎异常
     */
    public abstract ResponseEntity<?> execute(MockEngineProcessData mockEngineProcessData) throws MockEngineException;

    /**
     * 执行脚本，包裹所有异常，并生成响应
     *
     * @param mockEngineProcessData 引擎所需的过程数据
     * @return 响应
     * @throws MockEngineException 引擎异常
     */
    public ResponseEntity<?> executeCausePackage(MockEngineProcessData mockEngineProcessData) throws MockEngineException {
        try {
            return execute(mockEngineProcessData);
        } catch (MockEngineException e) {
            throw e;
        } catch (Exception e) {
            throw new MockEngineException(e);
        }
    }
}
