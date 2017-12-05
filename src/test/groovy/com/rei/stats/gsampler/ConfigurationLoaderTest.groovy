package com.rei.stats.gsampler;

import static org.junit.Assert.*

import java.util.concurrent.TimeUnit

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.rei.stats.gsampler.util.EncryptionService

class ConfigurationLoaderTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()

    EncryptionService encryptionService

    @Before
    void init() {
        encryptionService = new EncryptionService(tmp.root.toPath().resolve("key"))
    }
    
    @Test
    void canLoadSamplerFromExtensions() {
        def config = loadConfig('''
globalPrefix 'some.prefix' 

fake {
   def reader = fakeReader()
   sampler('id', reader, 'my.stats', 10)        
}
        ''')
        
        verifySamplers(config.samplers, 10, TimeUnit.SECONDS)
        assertEquals('some.prefix', config.globalPrefix)
    }

    @Test 
    void canLoadSamplerWithTimeUnitInterval() {
        def config = loadConfig("fake { sampler('f1', fakeReader(), 'millisecond.stat', 100, MILLISECONDS) }")
        verifySamplers(config.samplers, 100, TimeUnit.MILLISECONDS)
        assertEquals('sampler', config.globalPrefix)
    }

    @Test
    void canDecryptConfiguredData() {
        def encrypted = encryptionService.encrypt('millisecond.stat')
        def config = loadConfig("fake { sampler('f1', fakeReader(), decrypt('${encrypted}'), 10, MILLISECONDS) }")
        verifySamplers(config.samplers, 10, TimeUnit.MILLISECONDS)
        assertEquals('sampler', config.globalPrefix)
    }

    Configuration loadConfig(text) {
        def configFile = tmp.newFile()
        configFile.text = text
        return new ConfigurationLoader().loadConfiguration(configFile, encryptionService)
    }
        
    private void verifySamplers(samplers, interval, unit) {
        assertEquals(1, samplers.size())
        println samplers[0]
        
        assertEquals(interval, samplers[0].interval)
        assertEquals(unit, samplers[0].unit)
    }
    
}
