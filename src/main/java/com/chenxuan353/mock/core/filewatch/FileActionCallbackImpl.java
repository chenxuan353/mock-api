package com.chenxuan353.mock.core.filewatch;

import cn.hutool.core.io.file.FileNameUtil;
import com.chenxuan353.mock.core.consts.MockProcessConsts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@RequiredArgsConstructor
public class FileActionCallbackImpl implements FileActionCallback {
    private final AtomicBoolean hasChange;
    private final FileChangeService fileChangeService;

    private void hasChange(File file) {
        if (fileChangeService.matchResourcePath(file)) {
            return;
        }
        hasChange.set(true);
    }

    @Override
    public void directoryDelete(File file) {
        hasChange(file);
    }

    @Override
    public void directoryModify(File file) {
        hasChange(file);
    }

    @Override
    public void directoryCreate(File file) {
        // hasChange.set(true);
    }

    @Override
    public void fileDelete(File file) {
        hasChange(file);
    }

    @Override
    public void fileModify(File file) {
        String extName = FileNameUtil.extName(file);
        if (MockProcessConsts.strIgnoreCaseExist(extName, MockProcessConsts.CONFIG_EXT)) {
            hasChange(file);
        }
    }

    @Override
    public void fileCreate(File file) {
        hasChange(file);
    }
}
