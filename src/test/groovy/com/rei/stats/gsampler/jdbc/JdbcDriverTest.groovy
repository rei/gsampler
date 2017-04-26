package com.rei.stats.gsampler.jdbc;

import static org.junit.Assert.*

import org.junit.Test

class JdbcDriverTest {

    @Test
    void canCreateWithValidGAV() {
        def driver = new JdbcDriver('org.h2.Driver', 'com.h2database:h2:1.1.105', "http://repo1.maven.org/maven2")
        assertEquals('org.h2.Driver', driver.className)
        assertEquals('com.h2database', driver.driverArtifact.groupId)
        assertEquals('h2', driver.driverArtifact.artifactId)
        assertEquals('1.1.105', driver.driverArtifact.version)
    }

    @Test
    void canRegisterDriver() {
        new JdbcDriver('org.h2.Driver', 'com.h2database:h2:1.1.105', "http://repo1.maven.org/maven2").register()
        println ClassLoader.systemClassLoader.URLs
        def urls = ClassLoader.systemClassLoader.URLs.collect { it.toString() }
        assertTrue(urls.any { it.contains('h2database') && it.contains('1.1.105') })
    }
    
    @Test(expected=IllegalArgumentException)
    void throwsIllegalArgumentIfInvalidGAV() {
        new JdbcDriver('foo.bar', 'group:version', "http://repo1.maven.org/maven2")
    }
}
