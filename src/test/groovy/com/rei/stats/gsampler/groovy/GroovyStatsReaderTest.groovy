package com.rei.stats.gsampler.groovy;

import static org.junit.Assert.*

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.rei.stats.gsampler.StatValue
import com.rei.stats.gsampler.groovy.GroovyStatsReader

class GroovyStatsReaderTest {
    @Rule
    public TemporaryFolder tmp = new TemporaryFolder()
    
    @Test
    public void canReadFromScriptText() {
        def stats = new GroovyStatsReader('[groovyStat: val(args[0] as int)]', ['25']).read()
        assertEquals(1, stats.size())
        assertTrue(stats['groovyStat'] instanceof StatValue)
        assertEquals(25, stats['groovyStat'].value)
        assertNotNull(stats['groovyStat'].timestamp)
    }

    @Test
    public void canReadFromScriptFile() {
        def scriptFile = tmp.newFile()
        scriptFile.text = '[fileStat: 25]'
        def stats = new GroovyStatsReader(scriptFile, []).read()
        assertEquals(1, stats.size())
        assertTrue(stats['fileStat'] instanceof StatValue)
        assertEquals(25, stats['fileStat'].value)
        assertNotNull(stats['fileStat'].timestamp)
    }
}
