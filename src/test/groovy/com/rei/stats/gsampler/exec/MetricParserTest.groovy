package com.rei.stats.gsampler.exec;

import static org.junit.Assert.*

import org.junit.Test

class MetricParserTest {

    @Test
    public void canParseMetrics() {
        def parser = new MetricParser(/([\w.]+)\s+(\d*\.?\d*)/, null)
        def stats = parser.parse("""
my.metric.name 100
other.metric    10.0
""")
        
        assertEquals(2, stats.size())
        assertEquals(100, stats['my.metric.name'].value.intValue())
        assertEquals(10, stats['other.metric'].value.intValue())
    }
    
    @Test
    public void canParseMetricsWithValueOnlyRegex() {
        def parser = new MetricParser(/(\d+)/, 'my.metric')
        def stats = parser.parse("""
non matching line
the value is 100
other line
""")
        
        assertEquals(1, stats.size())
        assertEquals(100, stats['my.metric'].value.intValue())
    }

}
