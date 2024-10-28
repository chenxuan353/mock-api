package com.chenxuan353.mock.core.filewatch;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

@Slf4j
public class WatchDir {

    private final WatchService watcher;
    private final Map<WatchKey, Path> keys;
    private final boolean subDir;

    /**
     * 构造方法
     *
     * @param file 文件目录，不可以是文件
     */
    public WatchDir(File file, boolean subDir, FileActionCallback callback) throws Exception {
        if (!file.isDirectory()) {
            throw new Exception(file.getAbsolutePath() + "不是文件夹!");
        }
        this.watcher = FileSystems.getDefault().newWatchService();
        this.keys = new HashMap<>();
        this.subDir = subDir;

        Path dir = Paths.get(file.getAbsolutePath());

        if (subDir) {
            registerAll(dir);
        } else {
            register(dir);
        }
        processEvents(callback);
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>) event;
    }

    /**
     * 观察指定的目录
     */
    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        keys.put(key, dir);
    }

    /**
     * 观察指定的目录，并且包括子目录
     */
    private void registerAll(final Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 发生文件变化的回调函数
     */
    void processEvents(FileActionCallback callback) {
        for (; ; ) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }
            Path dir = keys.get(key);
            if (dir == null) {
                log.error("操作未识别");
                continue;
            }
            List<WatchEvent<?>> watchEvents = key.pollEvents();
            for (WatchEvent<?> event : watchEvents) {
                WatchEvent.Kind<?> kind = event.kind();
                // 事件可能丢失或遗弃
                if (kind == StandardWatchEventKinds.OVERFLOW) {
                    continue;
                }
                // 目录内的变化可能是文件或者目录
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);
                File file = child.toFile();
                if (kind.name().equals(StandardWatchEventKinds.ENTRY_DELETE.name())) {
                    callback.anyOperate(file);
                    if (file.isDirectory()) {
                        callback.directoryDelete(file);
                    } else {
                        callback.fileDelete(file);
                    }
                } else if (kind.name().equals(StandardWatchEventKinds.ENTRY_CREATE.name())) {
                    callback.anyOperate(file);
                    if (file.isDirectory()) {
                        callback.directoryCreate(file);
                    } else {
                        callback.fileCreate(file);
                    }
                } else if (kind.name().equals(StandardWatchEventKinds.ENTRY_MODIFY.name())) {
                    callback.anyOperate(file);
                    if (file.isDirectory()) {
                        callback.directoryModify(file);
                    } else {
                        callback.fileModify(file);
                    }
                } else {
                    continue;
                }

                if (subDir && (kind == StandardWatchEventKinds.ENTRY_CREATE)) {
                    if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
                        try {
                            registerAll(child);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            boolean valid = key.reset();
            if (!valid) {
                // 移除不可访问的目录
                // 因为有可能目录被移除，就会无法访问
                keys.remove(key);
                // 如果待监控的目录都不存在了，就中断执行
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }
}