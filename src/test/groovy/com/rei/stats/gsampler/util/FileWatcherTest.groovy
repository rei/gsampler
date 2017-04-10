package com.rei.stats.gsampler.util;

import static org.junit.Assert.*

import java.util.concurrent.atomic.AtomicLong

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileWatcherTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()
    
    @Test(timeout=2000L)
    public void notifiesCallbackWhenFileChanged() {
        def f = tmp.newFile()
        f.text = '0'
        
        def notifications = new AtomicLong(0)
        new FileWatcher(f.toPath()).onModified { notifications.incrementAndGet() }.start();
        
        f.text = '1'
        
        while (notifications.get() == 0) {
            sleep(100)
        }
    }

}
