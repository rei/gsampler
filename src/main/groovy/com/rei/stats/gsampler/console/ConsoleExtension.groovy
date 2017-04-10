package com.rei.stats.gsampler.console

import com.rei.stats.gsampler.Extension
import com.rei.stats.gsampler.StatValue
import com.rei.stats.gsampler.StatsWriter

class ConsoleExtension implements Extension {
    final String name = 'console'
    
    StatsWriter consoleWriter() { new ConsoleWriter() }
}

class ConsoleWriter implements StatsWriter {
    @Override
    public void writeStats(Map<String, StatValue> stats) {
        stats?.each { println it }
    }
}
