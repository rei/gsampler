package com.rei.stats.gsampler.util

import java.nio.file.Path
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileWatcherTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()

    @Test
    void notifiesCallbackWhenFileChanged() {
        Path p = tmp.newFile().toPath()
        Files.write(p, "0".getBytes("UTF-8"))

        def latch = new CountDownLatch(1)
        def fw = new FileWatcher(p).onModified { latch.countDown() }
        try {
            fw.start()
            Thread.sleep(100)

            Files.write(
                    p,
                    "1".getBytes("UTF-8"),
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.SYNC
            )

            assert latch.await(5, TimeUnit.SECONDS) : "modify event not observed"
        } finally {
            fw.shutdown()
        }
    }

}
