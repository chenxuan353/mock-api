package com.chenxuan353.mock.core.process;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import cn.hutool.core.util.StrUtil;
import com.chenxuan353.mock.core.config.MockResponseConfig;
import com.chenxuan353.mock.core.consts.MockProcessType;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import com.chenxuan353.mock.core.exception.MockEngineException;
import com.chenxuan353.mock.util.RespUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

import static com.chenxuan353.mock.core.consts.MockRespError.MOCK_PROCESS_FILE_FILEPATH_NEED;
import static com.chenxuan353.mock.core.consts.MockRespError.MOCK_PROCESS_FILE_NOTFOUND;

/**
 * File执行处理器
 * 只允许通过`yml`配置文件主动配置，且必须配置文件路径。
 */
@Slf4j
@Component
public class MockFileProcess extends MockProcessAbstract {

    @Override
    public boolean accessType(MockProcessType type) {
        return MockProcessType.File == type;
    }

    @Override
    public ResponseEntity<?> execute(MockEngineProcessData mockEngineProcessData) throws MockEngineException {
        MockResponseConfig responseConfig = mockEngineProcessData.getResponseConfig();
        String filePath = responseConfig.getFilePath();
        if (StrUtil.isEmpty(filePath)) {
            log.error("文件类型的响应处理器必须设置filePath！MockProcess: {} | 访问路径: {}", mockEngineProcessData.getMockProcess().getDisplayName(), mockEngineProcessData.getRequest().getRequestURI());
            return RespUtil.error(MOCK_PROCESS_FILE_FILEPATH_NEED);
        }
        String resourcesExistPath = mockEngineProcessData.getMockProcess().getExternalTextResourcesExistPath(filePath);
        if (resourcesExistPath == null) {
            log.error("文件不存在！MockProcess: {} | 访问路径: {}", mockEngineProcessData.getMockProcess().getDisplayName(), mockEngineProcessData.getRequest().getRequestURI());
            return RespUtil.error(MOCK_PROCESS_FILE_NOTFOUND);
        }
        String downloadFileName = responseConfig.getDownloadFileName();
        if (downloadFileName == null) {
            downloadFileName = FileNameUtil.getName(resourcesExistPath);
        }
        File file = new File(resourcesExistPath);
        HttpHeaders headers = new HttpHeaders();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (StrUtil.isNotEmpty(responseConfig.getContentType())) {
            mediaType = MediaType.parseMediaType(responseConfig.getContentType());
        } else if (responseConfig.getFileContentTypeAuto() != null && responseConfig.getFileContentTypeAuto()) {
            Optional<MediaType> mediaTypeOpt = MediaTypeFactory.getMediaType(resourcesExistPath);
            if (mediaTypeOpt.isPresent()) {
                mediaType = mediaTypeOpt.get();
            }
        }

        if (mediaType == MediaType.APPLICATION_OCTET_STREAM) {
            headers.set("Cache-Control", "no-cache, no-store, must-revalidate");
            headers.set("Content-Disposition", "attachment; filename=" + downloadFileName);
            headers.set("Pragma", "no-cache");
            headers.set("Expires", "0");
            headers.set("Last-Modified", new Date().toString());
            headers.set("ETag", String.valueOf(System.currentTimeMillis()));
        }

        if (CollUtil.isNotEmpty(responseConfig.getHeaders())) {
            for (Map.Entry<String, String> entry : responseConfig.getHeaders().entrySet()) {
                headers.set(entry.getKey(), entry.getValue());
            }
        }

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(new FileSystemResource(file));
    }
}
