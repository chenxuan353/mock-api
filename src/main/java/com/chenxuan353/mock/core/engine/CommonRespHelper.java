package com.chenxuan353.mock.core.engine;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.file.FileNameUtil;
import com.alibaba.fastjson.JSON;
import com.chenxuan353.mock.core.entity.MockEngineProcessData;
import lombok.Data;
import org.graalvm.polyglot.HostAccess;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
public class CommonRespHelper {
    private int code;
    private Object body;
    private Map<String, String> headers;
    private ResponseEntity<?> responseEntity;
    private String fileKey;
    private boolean fileDownload;

    private ResponseEntity<?> dealFileKey(MockEngineProcessData mockEngineProcessData, HttpHeaders httpHeaders) {
        Map<String, String> externalFileResources = mockEngineProcessData.getResponseConfig().getExternalFileResources();
        if (CollUtil.isEmpty(externalFileResources)) {
            throw new RuntimeException("资源文件Key `" + fileKey + "` 不存在！");
        }
        if (!externalFileResources.containsKey(fileKey)) {
            throw new RuntimeException("资源文件Key `" + fileKey + "` 不存在！");
        }
        String path = externalFileResources.get(fileKey);
        String resourcesExistPath = mockEngineProcessData.getMockProcess().getExternalTextResourcesExistPath(path);
        if (resourcesExistPath == null) {
            throw new RuntimeException("资源文件Key `" + fileKey + "` 对应的文件 `" + path + "` 不存在！");
        }
        MediaType mediaType = httpHeaders.getContentType();
        if (!this.fileDownload && mediaType == null) {
            Optional<MediaType> mediaTypeOpt = MediaTypeFactory.getMediaType(resourcesExistPath);
            if (mediaTypeOpt.isPresent()) {
                mediaType = mediaTypeOpt.get();
            }
        }

        if (mediaType == null) {
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
            httpHeaders.set("Cache-Control", "no-cache, no-store, must-revalidate");
            httpHeaders.set("Content-Disposition", "attachment; filename=" + FileNameUtil.getName(resourcesExistPath));
            httpHeaders.set("Pragma", "no-cache");
            httpHeaders.set("Expires", "0");
            httpHeaders.set("Last-Modified", new Date().toString());
            httpHeaders.set("ETag", String.valueOf(System.currentTimeMillis()));
        }
        File file = new File(resourcesExistPath);
        return ResponseEntity
                .ok()
                .headers(httpHeaders)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(new FileSystemResource(file));
    }

    public ResponseEntity<?> toResponseEntity(MockEngineProcessData mockEngineProcessData) {
        if (responseEntity != null) {
            return responseEntity;
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpHeaders.set(entry.getKey(), entry.getValue());
            }
        }

        // 尝试解析数据
        if (fileKey != null && mockEngineProcessData != null) {
            return dealFileKey(mockEngineProcessData, httpHeaders);
        }

        return new ResponseEntity<>(body, httpHeaders, code);
    }

    public ResponseEntity<?> toResponseEntity() {
        return toResponseEntity(null);
    }

    public static CommonRespHelperBuilder builder() {
        return new CommonRespHelperBuilder();
    }

    public static final class CommonRespHelperBuilder {
        private int code = 200;
        private String body;
        private Map<String, String> headers;
        private ResponseEntity<?> responseEntity;
        private String fileKey;
        private boolean fileDownload = false;

        private CommonRespHelperBuilder() {
        }

        @HostAccess.Export
        public CommonRespHelperBuilder code(int code) {
            this.code = code;
            return this;
        }

        @HostAccess.Export
        public CommonRespHelperBuilder contentType(String contentType) {
            header("Content-Type", contentType);
            return this;
        }

        @HostAccess.Export
        public CommonRespHelperBuilder body(Object body) {
            this.fileKey = null;
            if (body != null) {
                if (body instanceof String bodyStr) {
                    this.body = bodyStr;
                } else {
                    this.body = JSON.toJSONString(body);
                }
            }
            return this;
        }

        @HostAccess.Export
        public CommonRespHelperBuilder file(String fileKey) {
            return fileKey(fileKey);
        }

        @HostAccess.Export
        public CommonRespHelperBuilder fileKey(String fileKey) {
            this.body = null;
            this.fileKey = fileKey;
            return this;
        }

        @HostAccess.Export
        public CommonRespHelperBuilder download(String fileKey) {
            this.fileDownload = true;
            return fileKey(fileKey);
        }

        @HostAccess.Export
        public CommonRespHelperBuilder headers(Map<String, ?> headers) {
            Map<String, String> newHeaders = new HashMap<>();
            for (Map.Entry<String, ?> entry : headers.entrySet()) {
                newHeaders.put(entry.getKey(), entry.getValue().toString());
            }
            this.headers = newHeaders;
            return this;
        }

        @HostAccess.Export
        public CommonRespHelperBuilder header(String header, String value) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put(header, value);
            return this;
        }

        @HostAccess.Export
        public CommonRespHelperBuilder badRequest() {
            this.code = HttpStatus.BAD_REQUEST.value();
            return this;
        }

        @HostAccess.Export
        public CommonRespHelperBuilder notFound() {
            this.code = HttpStatus.NOT_FOUND.value();
            return this;
        }

        @HostAccess.Export
        public CommonRespHelperBuilder internalServerError() {
            this.code = HttpStatus.INTERNAL_SERVER_ERROR.value();
            return this;
        }

        /**
         * 302重定向
         */
        @HostAccess.Export
        public CommonRespHelperBuilder redirect(String path) throws URISyntaxException {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(new URI(path));
            responseEntity = new ResponseEntity<>(null, headers, HttpStatus.FOUND);
            return this;
        }

        @HostAccess.Export
        public int getCode() {
            return code;
        }

        @HostAccess.Export
        public String getBody() {
            return body;
        }

        @HostAccess.Export
        public String getFileKey() {
            return fileKey;
        }

        @HostAccess.Export
        public Map<String, String> getHeaders() {
            return headers;
        }

        @HostAccess.Export
        public String getHeader(String header) {
            if (headers == null) {
                return null;
            }
            return headers.get(header);
        }

        @HostAccess.Export
        public boolean isFileDownload() {
            return fileDownload;
        }

        public CommonRespHelper build() {
            CommonRespHelper graalJsRespHelper = new CommonRespHelper();
            graalJsRespHelper.code = this.code;
            graalJsRespHelper.body = this.body;
            graalJsRespHelper.headers = this.headers;
            graalJsRespHelper.responseEntity = this.responseEntity;
            graalJsRespHelper.fileKey = this.fileKey;
            graalJsRespHelper.fileDownload = this.fileDownload;
            return graalJsRespHelper;
        }
    }
}
