package com.chenxuan353.mock.core.consts;

public enum MockRespError {
    ACTIVACT_MOCK_MODULE_IS_EMPTY("M00001", "不存在已激活模块！"),
    MOCK_PROCESS_RUNNER_EXCEPTION("MP00001", "响应执行器异常！"),
    MOCK_PROCESS_RUNTIME_EXCEPTION("MP00002", "响应处理器运行时异常！"),
    MOCK_PROCESS_UNKNOW_PROCESS_TYPE("MP00003", "响应器处理类型识别失败！"),
    MOCK_PROCESS_NOT_FOUND("MP00004", "未找到可供运行的响应处理器！"),
    MOCK_PROCESS_STATIC_FILE_MUTIL("MP00005", "静态文件处理器错误，在静态文件响应处理时发现文件数不为一！"),
    MOCK_PROCESS_FILE_FILEPATH_NEED("MP00006", "文件类型的响应处理器必须设置 filePath ！"),
    MOCK_PROCESS_FILE_NOTFOUND("MP00007", "文件不存在！"),
    MOCK_PROCESS_REDIRECT_BODY_EMPTY("MP00008", "重定向类型必须设置响应体！"),
    MOCK_PROCESS_REDIRECT_URI_ERROR("MP00009", "重定向响应路径不合法！"),
    ;
    private final String code;
    private final String msg;

    MockRespError(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
