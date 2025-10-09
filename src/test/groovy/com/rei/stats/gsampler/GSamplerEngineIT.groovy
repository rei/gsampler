package com.rei.stats.gsampler

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GSamplerEngineIT {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()

    GSamplerEngine sampler
    File configFile
    File homeDir

    @Before
    void setup() {
        tmp.newFolder('config')
        homeDir = tmp.newFolder('home')
        configFile = tmp.newFile('config/gsampler.groovy')
        configFile.text = getConfig()
    }

    @Test
    void testSamplerEngine() {
        sampler = new GSamplerEngine(new FilesystemConfigurationProvider(configFile.toPath()), homeDir.toPath())
        sampler.test()
    }

    static def getConfig() {
        return """
exec {
      def p = parser(/Average = (\\d+)ms/, 'ping.average')
      sampler('exec', command('ping 127.0.0.1 -n 1', 1000, p), 'net', 1, MINUTES)
}

console {
        writer(consoleWriter())
}
"""
    }
}
