package com.rei.stats.gsampler;

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GSamplerEngineIT {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()
    
    GSamplerEngine sampler
    File configFile
    
    @Before
    public void setup() {
        tmp.newFolder('config')
        configFile = tmp.newFile('config/gsampler.groovy')
        configFile.text = getConfig()
    }
    
    @Test
    public void testSamplerEngine() {
        sampler = new GSamplerEngine(configFile.toPath())
        sampler.test()        
    }
    
    def getConfig() {
        return """
exec {
      def p = parser(/Average = (\\d+)ms/, 'ping.average')
      sampler('exec', command('ping 127.0.0.1 -n 1', 1000, p), 'net', 1, MINUTES)
}

elasticsearch {
    def idx = index('http://cobpels01:9200')
    def esQueries = ['posts': 'type:apacheAccess AND verb:POST AND urlPath:(-\"/rest/log\" -\"/rest/user/guest/v2.json\")',
                     'other': 'type:apacheAccess AND verb:POST']    

    sampler('es', queries(idx, esQueries, null, 5, MINUTES), 'prefix', 5, MINUTES)
	sampler('es1', query(idx, 'topIP.Name', 'type:jbossREI AND message:requestUri* AND !remoteIp:\"127.0.0.1\"', 'remoteIp', 5, MINUTES), 'prefix', 5, MINUTES)
}

console {
        writer(consoleWriter())
}
"""
    }
}
