package com.rei.stats.gsampler;

import static org.junit.Assert.*
import groovy.json.JsonSlurper

import java.util.concurrent.TimeUnit

import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GSamplerEngineTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()
    
    GSamplerEngine sampler
    File configFile
    
    @Before
    public void setup() {
        tmp.newFolder('config')
        configFile = tmp.newFile('config/gsampler.groovy')
    }
    
    @After
    public void cleanup() {
        sampler?.stop()
    }
    
    @Test
    public void runSamplerEngine() {
        configFile.text = getScriptText(1)
        
        sampler = new GSamplerEngine(configFile.toPath())
        sampler.start()
        
        def lastReload = sampler.selfStats.lastConfigReload.get()
        assertTrue(lastReload > 0)
        
        while (sampler.selfStats.samplesTaken.get() == 0) {
            sleep(100)
        }
        
        configFile.text = getScriptText(2)
        
        while (sampler.selfStats.lastConfigReload.get() > lastReload) {
            sleep(10)
        }
        
        println new URL("http://localhost:${sampler.server.port}/self-stats").text
        sampler.printSelfStats()
        //sleep(60000)
    }
    
    @Test
    public void testConfig() {
        def configFile = tmp.newFile()
        configFile.text = getScriptText(1)
        
        def sampler = new GSamplerEngine(configFile.toPath())
        println sampler.dataDir
        sampler.test()
    }
    
    @Test
    void handlesSamplingErrors() {
        configFile.text = '''groovy { sampler('fail-script', scriptText('1/0'), 'fail', 1) }
                             console { writer(consoleWriter()) }'''
        
         sampler = new GSamplerEngine(configFile.toPath())
         sampler.start()
         
         while (sampler.selfStats.failedSamples.get() == 0) {
            sleep(100)
         }
         
         def response = new JsonSlurper().parseText(new URL("http://localhost:${sampler.server.port}/errors").text)
         println response
         assertEquals('Division by zero', response['fail-script'].msg)
    } 
    

    @Test
    public void continuesUsingExistingConfigIfReplacementConfigIsInvalid() {
        def configFile = tmp.newFile()
        configFile.text = getScriptText(1)
        
        def sampler = new GSamplerEngine(configFile.toPath())
        sampler.start()
        
        def lastReload = sampler.selfStats.lastConfigReload.get()
        assertTrue(lastReload > 0)
        
        while (sampler.selfStats.samplesTaken.get() == 0) {
            sleep(100)
        }
        
        def samplesTaken = sampler.selfStats.samplesTaken.get()
        
        configFile.text = getScriptText('[') //should be invalid
        
        sleep(1200)
        
        assertTrue(samplesTaken < sampler.selfStats.samplesTaken.get()) //continues sampling
        assertEquals(lastReload, sampler.selfStats.lastConfigReload.get())
    }
    
    @Test
    public void delaysFirstExecution() {
        configFile.text = getScriptText(1)
        
        sampler = new GSamplerEngine(configFile.toPath())
        sampler.recordLastRun('jdbc')
        sampler.start()
        
        while (sampler.selfStats.samplesTaken.get() == 0) {
            sleep(100)
        }
        
        sampler.printSelfStats()
        assertEquals(1, sampler.selfStats.lastSampled.size())
        
    }
    
    @Test
    public void calculatesInitialDelay() {
        def engine = new GSamplerEngine(configFile.toPath())
        def sampler = new Sampler(id:'test', interval: 1, unit: TimeUnit.MINUTES)
        
        assertEquals(0, engine.getInitialDelay(sampler)) // lastRun file yet
        engine.recordLastRun(sampler.id) // should set
        assertTrue(engine.readLastRun('test') > 0)
        
        def delay = engine.getInitialDelay(sampler) 
        println delay
        assertTrue(delay > 50000) // should be pretty close to a minute of waiting
    }
    
    def getScriptText(arg) {
        return """
groovy {
        sampler('groovy', scriptText('[groovyStat: $arg]'), 'my.stats', 1)        
}

jdbc {
    driver('org.h2.Driver', 'com.h2database:h2:1.1.105')
    def cf = connectionFactory('jdbc:h2:mem:', 'sa', '')
    def queries = ["select 'stat.name', 20"]
    sampler('jdbc', jdbcReader(cf, queries), 'sql.stats', 5, MINUTES)
}

console {
        writer(consoleWriter())
}
"""
    }
}
