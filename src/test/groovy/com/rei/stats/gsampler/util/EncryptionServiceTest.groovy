package com.rei.stats.gsampler.util

import static groovy.util.GroovyTestCase.assertEquals

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class EncryptionServiceTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()

    @Test
    void canEncryptAndDecrypt() {
        def service = new EncryptionService(tmp.getRoot().toPath().resolve("key"))
        def encrypted = service.encrypt("topSecret")
        println encrypted
        println service.decrypt(encrypted)
        assertEquals("topSecret", service.decrypt(encrypted))

    }
}
