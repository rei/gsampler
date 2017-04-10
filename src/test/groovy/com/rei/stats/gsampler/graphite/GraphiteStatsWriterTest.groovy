package com.rei.stats.gsampler.graphite;

import static org.junit.Assert.*

import org.junit.After
import org.junit.Test

import com.rei.stats.gsampler.StatValue

class GraphiteStatsWriterTest {

    StringBuilder readText = new StringBuilder()
    Process server
    
    @After
    void cleanup() {
        server?.destroy()
    }
    
    @Test(timeout=5000l)
    public void writeStatsToFakeGraphite() {
        def port = 22456
        startServer(port)
        
        def writer = new GraphiteStatsWriter('localhost', port)
        writer.writeStats(['test.stat': new StatValue(56), 
                           'test.stat2': new StatValue(25),
                           'stupid stat with space': new StatValue(87)])
        
        while (readText.readLines().size() < 3) {
            sleep(10)
        } 
        
        println readText
        assertTrue(readText.contains('stupid_stat_with_space'))
        readText.readLines().each { line -> 
            if (!line.trim().empty) {
                assertFalse(line.split(' ')[2].contains('.'))
            } 
            
        }
    }
    
    @Test(timeout=5000l)
    public void queuesStatsIfGraphiteDown() {
        if (!System.properties['os.name'].contains("Windows")) {
            return // for some stupid reason this won't run correctly on linux, skipping for now
        }
        def port = 22459
        startServer(port)
        
        def writer = new GraphiteStatsWriter('localhost', port)
        
        server.destroy()
        
        println "shut down server"
        Thread.sleep(100)
        
        writer.writeStats(['test.stat1': new StatValue(56)])
        
        println "restarting server"
        server = startServer(port)
                
        writer.writeStats(['test.stat2': new StatValue(78)])
        
        while (readText.readLines().size() < 2) {
            sleep(10)
        }
        
        println "READ: $readText"
        assertTrue(readText.contains('test.stat1'))
        assertTrue(readText.contains('test.stat2'))
        server.destroy()
    }

    def startServer(port) {
        if (!System.properties['os.name'].contains("Windows")) {
            Thread.start {
                server = new EchoServer().setOutput(readText).start(port)
            }
            return server// for some stupid reason this won't run correctly on linux, skipping for now
        }
        
        def p = "${System.properties['java.home']}/bin/java -cp ${System.properties['java.class.path']} com.rei.stats.gsampler.graphite.EchoServer $port".execute()
        p.consumeProcessOutputStream(readText)
        p.consumeProcessErrorStream(System.out)
        Thread.sleep(500) //wait for it to start
        Runtime.getRuntime().addShutdownHook(new Thread({ p.destroy() } as Runnable))
        server = p
        return p
    }
}
