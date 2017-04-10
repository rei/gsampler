package com.rei.stats.gsampler.util

import java.nio.file.FileSystems
import java.nio.file.WatchService
import static java.nio.file.StandardWatchEventKinds.*

import java.nio.file.ClosedWatchServiceException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class FileWatcher {
    private WatchService watcher = FileSystems.getDefault().newWatchService()
    private boolean shutdown
    private Path file
    private Closure onModifiedCallback
    private ExecutorService watchExec = Executors.newSingleThreadExecutor()
    
    FileWatcher(Path file) {
        if (!Files.isRegularFile(file)) {
            throw new IllegalArgumentException("$file must be a regular file!")
        }
        this.file = file
    }
    
    FileWatcher onModified (Closure onModifiedCallback) {
        this.onModifiedCallback = onModifiedCallback
        return this
    }
    
    void shutdown() {
        this.shutdown = true
        watchExec.shutdownNow()
    }
    
    void start() {
        file.parent.register(watcher, ENTRY_MODIFY)
        watchExec.execute( {
            while (!shutdown) {
                WatchKey key;
                try {
                    key = watcher.take();
                } catch (InterruptedException | ClosedWatchServiceException x) { return; }
                
                for (WatchEvent<Path> e in key.pollEvents()) {
                    if (e.kind() == OVERFLOW) { continue }
                    if (e.context().endsWith(file.fileName)) {
                        onModifiedCallback()
                    }
                }
                
                boolean valid = key.reset();
                if (!valid) { break }
            }
        } as Runnable) 
    }
}
