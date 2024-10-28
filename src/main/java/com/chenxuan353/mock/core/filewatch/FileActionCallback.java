package com.chenxuan353.mock.core.filewatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public interface FileActionCallback {
    Logger callbackLogger = LoggerFactory.getLogger(FileActionCallback.class);

    default void anyOperate(File file) {
    }

    default void directoryDelete(File file) {
        callbackLogger.debug("目录已删除 " + file.getAbsolutePath());
    }

    default void directoryModify(File file) {
        callbackLogger.debug("目录已修改 " + file.getAbsolutePath());
    }

    default void directoryCreate(File file) {
        callbackLogger.debug("目录已创建 " + file.getAbsolutePath());
    }


    default void fileDelete(File file) {
        callbackLogger.debug("文件已删除 " + file.getAbsolutePath());
    }

    default void fileModify(File file) {
        callbackLogger.debug("文件已修改 " + file.getAbsolutePath());
    }

    default void fileCreate(File file) {
        callbackLogger.debug("文件已创建 " + file.getAbsolutePath());
    }
}
