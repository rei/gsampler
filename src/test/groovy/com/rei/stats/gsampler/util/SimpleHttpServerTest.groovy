package com.rei.stats.gsampler.util;

import static org.junit.Assert.*

import org.junit.Test

class SimpleHttpServerTest {

    @Test
    public void canHandleSimpleRequests() {
        def server = new SimpleHttpServer(22457)
        
        def requestsHandled = 0
        
        server.register('GET', '/some/path') {
            println "got request!"
            requestsHandled += 1
            return [contentType: 'text/plain', body: "sweet it worked!"]
        }
        
        server.start()
        
        println new URL("http://localhost:${server.port}/some/path").text
        
        server.stop()
        assertTrue(requestsHandled != 0)
    }

}
