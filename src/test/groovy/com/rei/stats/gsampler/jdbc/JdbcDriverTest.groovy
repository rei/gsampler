package com.rei.stats.gsampler.jdbc

import static org.junit.Assert.*

import org.junit.Test
import java.sql.DriverManager

class JdbcDriverTest {

    @Test
    void canCreateWithValidGAV() {
        def driver = new JdbcDriver('org.h2.Driver', 'com.h2database:h2:1.1.105', "https://repo1.maven.org/maven2")
        assertEquals('org.h2.Driver', driver.className)
        assertEquals('com.h2database', driver.driverArtifact.groupId)
        assertEquals('h2', driver.driverArtifact.artifactId)
        assertEquals('1.1.105', driver.driverArtifact.version)
    }

    @Test
    void canRegisterDriver() {
        new JdbcDriver('org.h2.Driver', 'com.h2database:h2:1.1.105', "https://repo1.maven.org/maven2").register()
        def drivers = DriverManager.drivers.toList()*.class.name
        assert drivers.any { it == 'org.h2.Driver' }
    }

    @Test(expected=IllegalArgumentException)
    void throwsIllegalArgumentIfInvalidGAV() {
        new JdbcDriver('foo.bar', 'group:version', "https://repo1.maven.org/maven2")
    }
}
